#!/bin/bash

version="$1"

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

printf "version=%s\n" "${version}"

mvn clean && mvn -Djproxy.version="${version}" install && mvn -Djproxy.version="${version}" package

