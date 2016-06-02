#!/usr/bin/env bash

hash="88e58348fa69193ec5cfba17256294e2d5e30d43"

if [ ! -f pellet-${hash}/cli/target/pelletcli/bin/pellet ]; then
  echo "Getting pellint..."
  curl -LO https://github.com/janvlug/pellet/archive/${hash}.zip
  unzip ${hash}.zip
  rm ${hash}.zip
  cd pellet-${hash} && mvn install -DskipTests && chmod u+x cli/target/pelletcli/bin/*
  cd ..
fi

echo "Running pellint..."
pellet-${hash}/cli/target/pelletcli/bin/pellet lint $@