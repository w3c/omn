#!/bin/sh
if [ -z $@ ]; then
	ARGS="mvn clean install"	
else
	ARGS=$@
fi

M2_CACHE="-v ${PWD}:/root/.m2"
CMD_ENV="-e CI_DEPLOY_USERNAME=foo -e CI_DEPLOY_PASSWORD=bar"
CMD_WORK="-v $PWD:/usr/src/omnlib ${M2_CACHE} -w /usr/src/omnlib"
CMD1="docker run -it --rm --name omnlib"
CMD2="maven:3-jdk-8"
CMD="${CMD1} ${CMD_WORK} ${M2_CACHE} ${CMD2} ${ARGS}"

echo $CMD
$CMD
RET=$?

echo "***"
echo "** Note: for deployment you need to provide CI_DEPLOY_USERNAME & CI_DEPLOY_PASSWORD envoriment vars!"
#echo "** e.g. ${CMD1} ${CMD_WORK} ${CMD_ENV} ${M2_CACHE} ${CMD2} mvn deploy -DskipTests --settings .travis/settings.xml"
echo "** or provide a custom settings.xml to this script"
echo "** e.g. $0 mvn deploy -DskipTests --settings .travis/settings-secret.xml"
echo "***"

exit $RET