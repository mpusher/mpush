#!/bin/sh

ENV=dev

base_dir=`pwd`

echo "start assembly lib..."
mvn clean assembly:assembly -P $ENV

echo "start tar mpush..."
cd $base_dir/target
tar -xzvf ./mpush-jar-with-dependency.tar.gz
echo "start start mpush..."

java -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=7998 -jar $base_dir/target/mpush/mpush-cs.jar &

echo "end start mpush..."
