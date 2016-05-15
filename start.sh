#!/bin/sh

ENV=daily

base_dir=`pwd`

echo "start assembly lib..."

mvn clean
mvn package -P $ENV

echo "start tar mpush..."
cd $base_dir/target
tar -xzvf ./mpush-release.tar.gz
echo "start start mpush..."

java -Dio.netty.leakDetectionLevel=advanced -jar $base_dir/target/mpush/boot.jar &


echo "end start mpush..."
