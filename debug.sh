#!/bin/sh
echo "start assembly lib..."
mvn assembly:assembly
tar -xzvf ./target/mpush-jar-with-dependency.tar.gz
echo "start start mpush..."
java -jar ./target/mpush/mpush-cs.jar  -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n &

echo "end start mpush..."
