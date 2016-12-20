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
MP_BIN="${BASH_SOURCE-$0}"
MP_BIN="$(dirname "${MP_BIN}")"
MP_BIN_DIR="$(cd "${MP_BIN}"; pwd)"

if [ -e "$MP_BIN/../libexec/env-mp.sh" ]; then
  . "$MP_BIN_DIR/../libexec/env-mp.sh"
else
  . "$MP_BIN_DIR/env-mp.sh"
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
    MP_MAIN="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.local.only=$JMXLOCALONLY"
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
    MP_MAIN="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=$JMXPORT -Dcom.sun.management.jmxremote.authenticate=$JMXAUTH -Dcom.sun.management.jmxremote.ssl=$JMXSSL -Dmpush.jmx.log4j.disable=$JMXLOG4J"
  fi
else
    echo "JMX disabled by user request" >&2
    MP_MAIN=""
fi

MP_MAIN="$MP_MAIN -jar $MP_BIN_DIR/bootstrap.jar"

if [ "x$SERVER_JVM_FLAGS"  != "x" ]
then
    JVM_FLAGS="$SERVER_JVM_FLAGS $JVM_FLAGS"
fi

if [ "x$2" != "x" ]
then
    MP_CFG="$MP_CFG_DIR/$2"
fi

# if we give a more complicated path to the config, don't screw around in $MP_CFG_DIR
if [ "x$(dirname "$MP_CFG")" != "x$MP_CFG_DIR" ]
then
    MP_CFG="$2"
fi

if $cygwin
then
    MP_CFG=`cygpath -wp "$MP_CFG"`
    # cygwin has a "kill" in the shell itself, gets confused
    KILL=/bin/kill
else
    KILL=kill
fi

echo "Using config: $MP_CFG" >&2

case "$OSTYPE" in
*solaris*)
  GREP=/usr/xpg4/bin/grep
  ;;
*)
  GREP=grep
  ;;
esac
if [ -z "$MP_PID_FILE" ]; then
#    MP_DATA_DIR="$($GREP "^[[:space:]]*dataDir" "$MP_CFG" | sed -e 's/.*=//')"
    if [ ! -d "$MP_DATA_DIR" ]; then
        mkdir -p "$MP_DATA_DIR"
    fi
    MP_PID_FILE="$MP_DATA_DIR/mpush_server.pid"
else
    # ensure it exists, otw stop will fail
    mkdir -p "$(dirname "$MP_PID_FILE")"
fi

if [ ! -w "$MP_LOG_DIR" ] ; then
mkdir -p "$MP_LOG_DIR"
fi

_MP_DAEMON_OUT="$MP_LOG_DIR/mpush.out"

case $1 in
start)
    echo  -n "Starting mpush ... "
    if [ -f "$MP_PID_FILE" ]; then
      if kill -0 `cat "$MP_PID_FILE"` > /dev/null 2>&1; then
         echo $command already running as process `cat "$MP_PID_FILE"`.
         exit 0
      fi
    fi
    nohup "$JAVA" "-Dmp.home=$MPUSH_HOME"  "-Dmp.conf=$MP_CFG" -cp "$CLASSPATH" $JVM_FLAGS $MP_MAIN > "$_MP_DAEMON_OUT" 2>&1 < /dev/null &
    if [ $? -eq 0 ]
    then
      case "$OSTYPE" in
      *solaris*)
        /bin/echo "${!}\\c" > "$MP_PID_FILE"
        ;;
      *)
        /bin/echo -n $! > "$MP_PID_FILE"
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
    "$JAVA" "-Dmp.home=$MPUSH_HOME" "-Dmp.conf=$MP_CFG" -cp "$CLASSPATH" $JVM_FLAGS $MP_MAIN
    ;;
print-cmd)
    echo "\"$JAVA\" $MP_MAIN "
    echo "\"-Dmp.home=$MPUSH_HOME  -Dmp.conf=$MP_CFG\" "
    echo "$JVM_FLAGS "
    echo "-cp \"$CLASSPATH\" "
    echo "> \"$_MP_DAEMON_OUT\" 2>&1 < /dev/null"
    ;;
stop)
    echo "Stopping mpush ... "
    if [ ! -f "$MP_PID_FILE" ]
    then
      echo "no mpush to stop (could not find file $MP_PID_FILE)"
    else
      $KILL -15 $(cat "$MP_PID_FILE")
      SLEEP=30
      SLEEP_COUNT=1
      while [ $SLEEP -ge 0 ]; do
        kill -0 $(cat "$MP_PID_FILE") >/dev/null 2>&1
        if [ $? -gt 0 ]; then
          rm -f "$MP_PID_FILE" >/dev/null 2>&1
          if [ $? != 0 ]; then
            if [ -w "$MP_PID_FILE" ]; then
              cat /dev/null > "$MP_PID_FILE"
            else
              echo "The PID file could not be removed or cleared."
            fi
          fi
          echo STOPPED
          break
        fi
        if [ $SLEEP -gt 0 ]; then
          echo "stopping ... $SLEEP_COUNT"
          sleep 1
        fi
        if [ $SLEEP -eq 0 ]; then
          echo "MPUSH did not stop in time."
          echo "To aid diagnostics a thread dump has been written to standard out."
          kill -3 `cat "$MP_PID_FILE"`
          echo "force stop MPUSH."
          kill -9 `cat "$MP_PID_FILE"`
          echo STOPPED
        fi
        SLEEP=`expr $SLEEP - 1`
        SLEEP_COUNT=`expr $SLEEP_COUNT + 1`
      done
    fi
    exit 0
    ;;
upgrade)
    shift
    echo "upgrading the servers to 3.*"
    "$JAVA" -cp "$CLASSPATH" $JVM_FLAGS com.mpush.tools.upgrade.UpgradeMain ${@}
    echo "Upgrading ... "
    ;;
restart)
    shift
    "$0" stop ${@}
    sleep 1
    "$0" start ${@}
    ;;
status)
    # -q is necessary on some versions of linux where nc returns too quickly, and no stat result is output
    clientPortAddress=`$GREP "^[[:space:]]*clientPortAddress[^[:alpha:]]" "$MP_CFG" | sed -e 's/.*=//'`
    if ! [ $clientPortAddress ]
    then
       	clientPortAddress="localhost"
    fi
    clientPort=`$GREP "^[[:space:]]*connect-server-port[^[:alpha:]]" "$MP_CFG" | sed -e 's/.*=//'`
    telnet 127.0.0.1 3002
    ;;
*)
    echo "Usage: $0 {start|start-foreground|stop|restart|status|upgrade|print-cmd}" >&2

esac