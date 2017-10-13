#!/usr/bin/env bash

dir="eyeball-2.3"
filename="${dir}.zip"
# url="http://garr.dl.sourceforge.net/project/jena/Archive/Eyeball/Eyeball%202.3/${filename}"
url="http://downloads.sourceforge.net/project/jena/Archive/Eyeball/Eyeball%202.3/eyeball-2.3.zip"

if [ ! -f ${dir}/lib/eyeball.jar ]; then

  command -v curl >/dev/null || { echo "ERROR: 'curl' not found." >&2; exit 1; }
  command -v ant >/dev/null || { echo "ERROR: 'ant' not found." >&2; exit 1; }
  
  echo "Downloading Apache Eyeball..."
  curl -sLO $url
  unzip $filename
  cd ${dir}  
  ant
  cd -
  rm $filename
fi

echo "Running Apache Eyeball..."
java -cp "${dir}/lib/*" jena.eyeball $*