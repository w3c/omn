#!/usr/bin/env bash
mvn package -DskipTests deploy:deploy-file -DrepositoryId=ossrh -Dfile=target/omnlib.jar -Durl=https://oss.sonatype.org/content/repositories/snapshots -DgroupId=info.open-multinet -DartifactId=omnlib -Dversion=0.0.1-SNAPSHOT
