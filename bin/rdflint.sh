#!/usr/bin/env bash

dir="eyeball-2.3"
filename="${dir}.zip"
url="http://garr.dl.sourceforge.net/project/jena/Archive/Eyeball/Eyeball%202.3/${filename}"

if [ ! -f ${dir}/lib/eyeball.jar ]; then
  echo "Downloading Apache Eyeball..."
  curl -O $url
  unzip $filename
  cd ${dir}
  ant
  cd -
  rm $filename
fi

echo "Running Apache Eyeball..."
java -cp "${dir}/lib/*" jena.eyeball $*