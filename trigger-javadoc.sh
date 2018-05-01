#!/bin/bash
VERSION=${1:-}

if [[ -z "$1" ]]
then
  VERSION=$(git tag | grep '^v[0-9]\+[.][0-9]\+[.][0-9]\+' | sort -V | tail -n 1 | sed 's/^v//')
fi

for m in utils $(cat pom.xml | grep '<module>' | sed -e 's/.*<module>//' -e 's:</.*::' | sort)
do
  URL="http://www.javadoc.io/doc/net.morimekta.utils/$m/$VERSION"
  echo -e "\033[01m$URL\033[00m"
  curl -s ${URL} > /dev/null || exit 1
done
