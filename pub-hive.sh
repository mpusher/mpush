#!/bin/sh

ENV=daily

base_dir=`pwd`

echo "start assembly lib..."

rm -rf $base_dir/target

mvn clean install  assembly:assembly -P $ENV

echo "start scp mpush..."

cd $base_dir/target

scp -P 9092 ./mpush-jar-with-dependency.tar.gz hive1_host:~/mpush



scp -P 9092 ./mpush-jar-with-dependency.tar.gz hive2_host:~/mpush
