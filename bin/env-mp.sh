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

# We use MP_CFG_DIR if defined,
# otherwise we use /etc/mp
# or the conf directory that is
# a sibling of this script's directory

MP_BIN_DIR="${MP_BIN_DIR:-/usr/bin}"
MPUSH_PREFIX="${MP_BIN_DIR}/.."
MPUSH_HOME=$MPUSH_PREFIX

if [ "x$MP_CFG_DIR" = "x" ]
then
  if [ -e "${MPUSH_PREFIX}/conf" ]; then
    MP_CFG_DIR="$MP_BIN_DIR/../conf"
  else
    MP_CFG_DIR="$MP_BIN_DIR/../etc/mpush"
  fi
fi

if [ "x${MP_DATA_DIR}" = "x" ]
then
    MP_DATA_DIR="${MPUSH_PREFIX}/tmp"
fi

if [ "x${MP_LOG_DIR}" = "x" ]
then
    MP_LOG_DIR="${MPUSH_PREFIX}/logs"
fi

if [ -f "${MP_BIN_DIR}/set-env.sh" ]; then
  . "${MP_BIN_DIR}/set-env.sh"
fi

if [ "x$MP_CFG" = "x" ]
then
    MP_CFG="mpush.conf"
fi

MP_CFG="$MP_CFG_DIR/$MP_CFG"

if [ -f "$MP_BIN_DIR/java.env" ]
then
    . "$MP_BIN_DIR/java.env"
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
CLASSPATH="$MP_CFG_DIR:$CLASSPATH"

#make it work in the binary package
#(use array for LIB_PATH to account for spaces within wildcard expansion)
if [ -e "${MPUSH_PREFIX}"/../lib/plugins/*.jar ]; then
  LIB_PATH=("${MPUSH_PREFIX}"/../lib/plugins/*.jar)
fi

for i in "${LIB_PATH[@]}"
do
    CLASSPATH="$i:$CLASSPATH"
done

case "`uname`" in
    CYGWIN*) cygwin=true ;;
    *) cygwin=false ;;
esac

if $cygwin
then
    CLASSPATH=`cygpath -wp "$CLASSPATH"`
fi
