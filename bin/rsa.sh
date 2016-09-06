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

# use POSTIX interface, symlink is followed automatically
MP_BIN="${BASH_SOURCE-$0}"
MP_BIN="$(dirname "${MP_BIN}")"
MP_BIN_DIR="$(cd "${MP_BIN}"; pwd)"

if [ -e "$MP_BIN/../libexec/env-mp.sh" ]; then
  . "$MP_BIN_DIR/../libexec/env-mp.sh"
else
  . "$MP_BIN_DIR/env-mp.sh"
fi

if [ $1 -gt 1024 ] ;then
    echo "use rsa key size $1"
    keySize = $1
else
    echo "use rsa key size 1024"
    keySize = 1024
fi

"$JAVA" -cp "$CLASSPATH" com.mpush.tools.crypto.RSAUtils $keySize