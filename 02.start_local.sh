#!/bin/bash

version="$1"
env="$2"
# set version variable
if [ -z "${version}" ] && [ -f "version.txt" ] ;
then
  version=$(cat version.txt)
  printf "set version from file\n"
fi

if [ -z "${version}" ] ;
then
  version="1.0"
  printf "set version from default\n"
fi

# set env variable
if [ -z "${env}" ] && [ -f "env.txt" ] ;
then
  env=$(cat env.txt)
  printf "set env from file\n"
fi

if [ -z "${env}" ] ;
then
  env="dev"
  printf "set env from default\n"
fi



printf "version=%s\n" "${version}"
printf "env=%s\n" "${env}"
if [ -f org.pierce.LocalServer.pid ] ;
then
awk '{printf "kill -9 %s\n",$0}' org.pierce.LocalServer.pid|bash
fi

echo java -jar -Denv="${env}" -Ddebug=false local-server/target/local-server-"${version}".jar

nohup java -jar -Denv="${env}" -Ddebug=false local-server/target/local-server-"${version}".jar>/dev/null &
tail -f /tmp/logs/local-server.log





