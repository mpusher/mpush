#!/bin/sh

ENV=daily

base_dir=`pwd`

echo "start assembly lib..."

rm -rf $base_dir/target

mvn clean install  assembly:assembly -P $ENV

echo "start tar mpush..."
cd $base_dir/target
tar -xzvf ./mpush-jar-with-dependency.tar.gz
echo "start start mpush..."

java -Dio.netty.leakDetectionLevel=advanced -jar $base_dir/target/mpush/mpush-cs.jar &


echo "end start mpush..."
