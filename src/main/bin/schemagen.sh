#!/usr/bin/env bash
#TODO: use mvn schemagen:generate instead
for i in $(ls src/main/resources/*.ttl); do 
  schemagen --owl -o src/main/java/ -i $i --package "info.openmultinet.ontology.vocabulary" --ontology --inference;
done

