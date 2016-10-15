#!/usr/bin/env bash
JVM_FLAGS="-Dio.netty.leakDetectionLevel=advanced -Dio.netty.noKeySetOptimization=false"
JVM_FLAGS="$JVM_FLAGS -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8008"