#!/bin/sh

ENV=daily
cd `dirname $0`
cd ..

echo "start package project..."
mvn clean
mvn package -P $ENV

echo "start tar mpush..."
cd ./mpush-boot/target
tar -xzvf ./mpush-release.tar.gz

echo "start start mpush..."
cd mpush
java -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=7998 -Dio.netty.leakDetectionLevel=advanced   -jar boot.jar &

echo "end start mpush..."
