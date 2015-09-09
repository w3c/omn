#!/bin/sh
if [ $# -ge 0 ]; then
	ARGS=$@
else
	ARGS="mvn clean install"	
fi

M2_CACHE="-v ${PWD}/m2:/root/.m2"
CMD_ENV="-e CI_DEPLOY_USERNAME=foo -e CI_DEPLOY_PASSWORD=bar"
CMD_WORK="-v $PWD:/usr/src/omnlib ${M2_CACHE} -w /usr/src/omnlib"
CMD1="docker run -it --rm --name omnlib"
CMD2="maven:3-jdk-8"
CMD="${CMD1} ${CMD_WORK} ${M2_CACHE} ${CMD2} ${ARGS}"

echo $CMD
$CMD
RET=$?

echo "**********************"
echo "** Note: for deployment you need to provide CI_DEPLOY_USERNAME & CI_DEPLOY_PASSWORD envoriment vars!"
#echo "** e.g. ${CMD1} ${CMD_WORK} ${CMD_ENV} ${M2_CACHE} ${CMD2} mvn deploy -DskipTests --settings .travis/settings.xml"
echo "** or provide a custom settings.xml to maven"
echo "** e.g. for WAR: $0 mvn deploy -DskipTests --settings .travis/settings-secret.xml"
echo "** e.g. for JAR: $0 mvn deploy:deploy-file -DrepositoryId=ossrh -Dfile=target/omnlib.jar -Durl=https://oss.sonatype.org/content/repositories/snapshots  -DgroupId=info.open-multinet  -DartifactId=omnlib -Dversion=0.0.1-SNAPSHOT -DskipTests --settings .travis/settings-secret.xml"
echo "**********************"

exit $RET
