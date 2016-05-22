#!/usr/bin/env bash

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

#
# If this scripted is run out of /usr/bin or some other system bin directory
# it should be linked to and not copied. Things like java jar files are found
# relative to the canonical path of this script.
#



# use POSTIX interface, symlink is followed automatically
MPBIN="${BASH_SOURCE-$0}"
MPBIN="$(dirname "${MPBIN}")"
MPBINDIR="$(cd "${MPBIN}"; pwd)"

if [ -e "$MPBIN/../libexec/mp-env.sh" ]; then
  . "$MPBINDIR/../libexec/mp-env.sh"
else
  . "$MPBINDIR/mp-env.sh"
fi

# See the following page for extensive details on setting
# up the JVM to accept JMX remote management:
# http://java.sun.com/javase/6/docs/technotes/guides/management/agent.html
# by default we allow local JMX connections
if [ "x$JMXLOCALONLY" = "x" ]
then
    JMXLOCALONLY=false
fi

if [ "x$JMXDISABLE" = "x" ] || [ "$JMXDISABLE" = 'false' ]
then
  echo "MPush JMX enabled by default" >&2
  if [ "x$JMXPORT" = "x" ]
  then
    # for some reason these two options are necessary on jdk6 on Ubuntu
    #   accord to the docs they are not necessary, but otw jconsole cannot
    #   do a local attach
    MPMAIN="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.local.only=$JMXLOCALONLY"
  else
    if [ "x$JMXAUTH" = "x" ]
    then
      JMXAUTH=false
    fi
    if [ "x$JMXSSL" = "x" ]
    then
      JMXSSL=false
    fi
    if [ "x$JMXLOG4J" = "x" ]
    then
      JMXLOG4J=true
    fi
    echo "MPush remote JMX Port set to $JMXPORT" >&2
    echo "MPush remote JMX authenticate set to $JMXAUTH" >&2
    echo "MPush remote JMX ssl set to $JMXSSL" >&2
    echo "MPush remote JMX log4j set to $JMXLOG4J" >&2
    MPMAIN="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=$JMXPORT -Dcom.sun.management.jmxremote.authenticate=$JMXAUTH -Dcom.sun.management.jmxremote.ssl=$JMXSSL -Dmpush.jmx.log4j.disable=$JMXLOG4J"
  fi
else
    echo "JMX disabled by user request" >&2
    MPMAIN=""
fi

MPMAIN="$MPMAIN -jar $MPUSH_PREFIX/boot.jar"

if [ "x$SERVER_JVMFLAGS"  != "x" ]
then
    JVMFLAGS="$SERVER_JVMFLAGS $JVMFLAGS"
fi

if [ "x$2" != "x" ]
then
    MPCFG="$MPCFGDIR/$2"
fi

# if we give a more complicated path to the config, don't screw around in $MPCFGDIR
if [ "x$(dirname "$MPCFG")" != "x$MPCFGDIR" ]
then
    MPCFG="$2"
fi

if $cygwin
then
    MPCFG=`cygpath -wp "$MPCFG"`
    # cygwin has a "kill" in the shell itself, gets confused
    KILL=/bin/kill
else
    KILL=kill
fi

echo "Using config: $MPCFG" >&2

case "$OSTYPE" in
*solaris*)
  GREP=/usr/xpg4/bin/grep
  ;;
*)
  GREP=grep
  ;;
esac
if [ -z "$MPPIDFILE" ]; then
#    MP_DATADIR="$($GREP "^[[:space:]]*dataDir" "$MPCFG" | sed -e 's/.*=//')"
    if [ ! -d "$MP_DATADIR" ]; then
        mkdir -p "$MP_DATADIR"
    fi
    MPPIDFILE="$MP_DATADIR/mpush_server.pid"
else
    # ensure it exists, otw stop will fail
    mkdir -p "$(dirname "$MPPIDFILE")"
fi

if [ ! -w "$MP_LOG_DIR" ] ; then
echo $MP_LOG_DIR
mkdir -p "$MP_LOG_DIR"
fi

_MP_DAEMON_OUT="$MP_LOG_DIR/mpush.out"

case $1 in
start)
    echo  -n "Starting mpush ... "
    if [ -f "$MPPIDFILE" ]; then
      if kill -0 `cat "$MPPIDFILE"` > /dev/null 2>&1; then
         echo $command already running as process `cat "$MPPIDFILE"`. 
         exit 0
      fi
    fi
    nohup "$JAVA" "-Dmp.conf=$MPCFG" "-Dmp.log.dir=${MP_LOG_DIR}" "-Dmp.root.logger=${MP_LOG4J_PROP}" \
    -cp "$CLASSPATH" $JVMFLAGS $MPMAIN > "$_MP_DAEMON_OUT" 2>&1 < /dev/null &
    if [ $? -eq 0 ]
    then
      case "$OSTYPE" in
      *solaris*)
        /bin/echo "${!}\\c" > "$MPPIDFILE"
        ;;
      *)
        /bin/echo -n $! > "$MPPIDFILE"
        ;;
      esac
      if [ $? -eq 0 ];
      then
        sleep 1
        echo STARTED
      else
        echo FAILED TO WRITE PID
        exit 1
      fi
    else
      echo SERVER DID NOT START
      exit 1
    fi
    ;;
start-foreground)
    MP_CMD=(exec "$JAVA")
    if [ "${MP_NOEXEC}" != "" ]; then
      MP_CMD=("$JAVA")
    fi
    "${MP_CMD[@]}" "-Dmp.log.dir=${MP_LOG_DIR}" "-Dmp.root.logger=${MP_LOG4J_PROP}" \
    -cp "$CLASSPATH" $JVMFLAGS $MPMAIN "-Dmp.conf=$MPCFG"
    ;;
print-cmd)
    echo "\"$JAVA\" -Dmp.log.dir=\"${MP_LOG_DIR}\" -Dmp.root.logger=\"${MP_LOG4J_PROP}\"
    echo -cp \"$CLASSPATH\" $JVMFLAGS $MPMAIN \"-Dmp.conf=$MPCFG\" > \"$_MP_DAEMON_OUT\" 2>&1 < /dev/null"
    ;;
stop)
    echo -n "Stopping mpush ... "
    if [ ! -f "$MPPIDFILE" ]
    then
      echo "no mpush to stop (could not find file $MPPIDFILE)"
    else
      $KILL -9 $(cat "$MPPIDFILE")
      rm "$MPPIDFILE"
      echo STOPPED
    fi
    exit 0
    ;;
upgrade)
    shift
    echo "upgrading the servers to 3.*"
    "$JAVA" "-Dmpush.log.dir=${MP_LOG_DIR}" "-Dmpush.root.logger=${MP_LOG4J_PROP}" \
    -cp "$CLASSPATH" $JVMFLAGS com.mpush.tools.upgrade.UpgradeMain ${@}
    echo "Upgrading ... "
    ;;
restart)
    shift
    "$0" stop ${@}
    sleep 3
    "$0" start ${@}
    ;;
status)
    # -q is necessary on some versions of linux where nc returns too quickly, and no stat result is output
    clientPortAddress=`$GREP "^[[:space:]]*clientPortAddress[^[:alpha:]]" "$MPCFG" | sed -e 's/.*=//'`
    if ! [ $clientPortAddress ]
    then
	clientPortAddress="localhost"
    fi
    clientPort=`$GREP "^[[:space:]]*clientPort[^[:alpha:]]" "$MPCFG" | sed -e 's/.*=//'`
    STAT=`"$JAVA" "-Dmp.log.dir=${MP_LOG_DIR}" "-Dmp.root.logger=${MP_LOG4J_PROP}" \
             -cp "$CLASSPATH" $JVMFLAGS org.apache.mpush.client.FourLetterWordMain \
             $clientPortAddress $clientPort srvr 2> /dev/null    \
          | $GREP Mode`
    if [ "x$STAT" = "x" ]
    then
        echo "Error contacting service. It is probably not running."
        exit 1
    else
        echo $STAT
        exit 0
    fi
    ;;
*)
    echo "Usage: $0 {start|start-foreground|stop|restart|status|upgrade|print-cmd}" >&2

esac
