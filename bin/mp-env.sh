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

# This script should be sourced into other mpush
# scripts to setup the env variables

# We use MPCFGDIR if defined,
# otherwise we use /etc/mp
# or the conf directory that is
# a sibling of this script's directory

MPBINDIR="${MPBINDIR:-/usr/bin}"
MPUSH_PREFIX="${MPBINDIR}/.."

if [ "x$MPCFGDIR" = "x" ]
then
  if [ -e "${MPUSH_PREFIX}/conf" ]; then
    MPCFGDIR="$MPBINDIR/../conf"
  else
    MPCFGDIR="$MPBINDIR/../etc/mpush"
  fi
fi

if [ -f "${MPBINDIR}/set-env.sh" ]; then
  . "${MPBINDIR}/set-env.sh"
fi

if [ "x$MPCFG" = "x" ]
then
    MPCFG="mpush.conf"
fi

MPCFG="$MPCFGDIR/$MPCFG"

if [ -f "$MPBINDIR/java.env" ]
then
    . "$MPBINDIR/java.env"
fi

if [ "x${MP_DATADIR}" = "x" ]
then
    MP_DATADIR="${MPUSH_PREFIX}/tmp"
fi

if [ "x${MP_LOG_DIR}" = "x" ]
then
    MP_LOG_DIR="${MPUSH_PREFIX}/logs"
fi

if [ "x${MP_LOG4J_PROP}" = "x" ]
then
    MP_LOG4J_PROP="INFO,CONSOLE"
fi

if [ "$JAVA_HOME" != "" ]; then
  JAVA="$JAVA_HOME/bin/java"
else
  JAVA=java
fi


#add the conf dir to classpath
CLASSPATH="$MPCFGDIR:$CLASSPATH"

for i in "$MPBINDIR"/../src/java/lib/*.jar
do
    CLASSPATH="$i:$CLASSPATH"
done

#make it work in the binary package
#(use array for LIBPATH to account for spaces within wildcard expansion)
if [ -e "${MPUSH_PREFIX}"/share/mpush/mpush-*.jar ]; then
  LIBPATH=("${MPUSH_PREFIX}"/share/mpush/*.jar)
else
  #release tarball format
  for i in "$MPBINDIR"/../mpush-*.jar
  do
    CLASSPATH="$i:$CLASSPATH"
  done
  LIBPATH=("${MPBINDIR}"/../lib/*.jar)
fi

for i in "${LIBPATH[@]}"
do
    CLASSPATH="$i:$CLASSPATH"
done

#make it work for developers
for d in "$MPBINDIR"/../build/lib/*.jar
do
   CLASSPATH="$d:$CLASSPATH"
done

#make it work for developers
CLASSPATH="$MPBINDIR/../build/classes:$CLASSPATH"


case "`uname`" in
    CYGWIN*) cygwin=true ;;
    *) cygwin=false ;;
esac

if $cygwin
then
    CLASSPATH=`cygpath -wp "$CLASSPATH"`
fi

#echo "CLASSPATH=$CLASSPATH"