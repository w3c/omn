#!/bin/sh
if [ ${TRAVIS_PULL_REQUEST} = 'false' ] && [ ${TRAVIS_BRANCH} = 'master' ]; then
	mvn deploy -DskipTests --settings .travis/settings.xml deploy:deploy-file -DrepositoryId=ossrh -Dfile=target/omnlib.jar -Durl=https://oss.sonatype.org/content/repositories/snapshots  -DgroupId=info.open-multinet  -DartifactId=omnlib -Dversion=0.0.1-SNAPSHOT
else
	true
fi
