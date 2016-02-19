#!/bin/sh

ENV=dev

base_dir=`pwd`

echo "start assembly lib..."
mvn clean install -P $ENV

echo "start tar mpush..."
cd $base_dir/target
tar -xzvf ./mpush-jar-with-dependency.tar.gz
echo "start start mpush..."

cd mpush/lib

java -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=7998 -Dio.netty.leakDetectionLevel=advanced   -jar $base_dir/target/mpush/mpush-cs.jar &

echo "end start mpush..."
