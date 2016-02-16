#!/bin/sh

ENV=daily

base_dir=`pwd`

echo "start assembly lib..."
mvn clean assembly:assembly -P $ENV

echo "start tar mpush..."
cd $base_dir/target
tar -xzvf ./mpush-jar-with-dependency.tar.gz
echo "start start mpush..."

java -jar $base_dir/target/mpush/mpush-cs.jar &


echo "end start mpush..."
