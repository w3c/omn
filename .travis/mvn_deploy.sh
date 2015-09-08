#!/bin/sh
if [ x"$1" = "x-f" ] || [ ${TRAVIS_PULL_REQUEST} = 'false' ] && [ ${TRAVIS_BRANCH} = 'master' ]; then
	mvn -DskipTests --settings .travis/settings.xml deploy
else
	true
fi
