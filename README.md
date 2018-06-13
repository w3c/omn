# Federated Infrastructures Ontology

[![Build Status](https://travis-ci.org/w3c/omn.svg)](https://travis-ci.org/w3c/omn)
[![Coverage Status](https://coveralls.io/repos/w3c/omn/badge.svg)](https://coveralls.io/r/w3c/omn)

## Abstract

This repository contains the work conducted in the [W3C Federated Infrastructures Community Group](https://www.w3.org/community/omn/).
It includes a formal information model for federated infrastructures (omn)
and a Java library (omnlib) to translate between
our Open-Multinet Ontology,
[GENI v3 RSpecs](http://www.geni.net/resources/rspec/3/)
(plus  [extensions](http://www.geni.net/resources/rspec/ext/)),
[OASIS TOSCA](http://docs.oasis-open.org/tosca/TOSCA/v1.0/os/TOSCA-v1.0-os.html),
and the
[IETF NFV](https://tools.ietf.org/html/draft-xjz-nfv-model-datamodel-01)
models.

## Further information

* [W3C OMN Community Group](https://www.w3.org/community/omn/)
* [Upper Ontology Description](https://github.com/w3c/omn/blob/master/omnlib/ontologies/omn)
* [GENI RSpec Description](http://groups.geni.net/geni/wiki/GENIExperimenter/RSpecs)
* [GENI RSpec Examples](http://groups.geni.net/geni/browser/trunk)

## Java omnlib

The Java library helps developers to work with Open-Multinet related ontologies and includes a translator to convert between RDF, GENI RSpec XMLs, and TOSCA XML.

### Using it

#### CLI

    mvn compile
    cd omnlib
    ./src/main/bin/omnlib -o ttl -i ./src/test/resources/geni/request/request_bound.xml

#### Web Service

Assuming you have a running WildFly instance

     cd omnweb
     mvn clean install
     mvn wildfly:deploy

Then convert the file

    curl --data-urlencode content@../omnlib/src/test/resources/geni/request/request_bound.xml http://127.0.0.1:8080/omnweb/convert/request/ttl
    curl --data-urlencode content@../omnlib/src/test/resources/geni/request/request_bound.xml http://demo.fiteagle.org:8080/omnweb/convert/request/ttl

### Developing it

#### Using it in your own project

Add this repository to your pom.xml file:

    <project>
      ...
      <repositories>
        <repository>
          <id>sonatype</id>
          <url>https://oss.sonatype.org/content/groups/public/</url>
        </repository>
      </repositories>
      ...
      <dependencies>
        <dependency>
          <groupId>info.open-multinet</groupId>
          <artifactId>omnlib</artifactId>
          <version>0.0.1-SNAPSHOT</version>
        </dependency>
      </dependencies>
      ...
    </project>

#### Enhancing the library

 1. Checkout the code
 2. Run "mvn clean compile" to auto generate binding files
 3. Open with IDE (e.g. Eclipse or IntelliJ)

## Directory Layout

 * bin: executables
 * data: example data to work with (to help validating and discussing the ontologies)
 * generated: auto generated files based (e.g. documentation, graphical representation, other serializations)
 * import: related ontologies to be reused (second main focus)
 * ontologies: upper ontologies (to main focus)
 * queries: example queries to work with (to help validating and discussing the ontologies)
 * src: omnlib (Java implementation and tests)

## Suggested Tools

 * [sparql](http://jena.apache.org/documentation/tools/) (command line tool from jena to query data)
 * [Protégé](http://protege.stanford.edu) (graphical ontology editor)
 * [Eclipse Xturtle](http://aksw.org/Projects/Xturtle.html) (text based ontology editor with code completion and simple validation)
 * [rdf.sh](https://github.com/seebi/rdf.sh) (command line ontology tools)
 * [OwlToUml](https://github.com/twalcari/OwlToUml) (to visualize simple ontologies as plantuml diagrams)
 * [rapper](http://librdf.org/raptor/rapper.html) (command line tool to convert and check ontologies)
 * [lodlive](http://en.lodlive.it) (user friendly vizualization of a sparql endpoint)
 * [yasgui](http://yasgui.laurensrietveld.nl) (user friendly query of a sparql endpoint)
 * [LODE](http://www.essepuntato.it/lode) (documentation generator for OWL files)

## How To (rather old approach)

### Run example Queries

    ./bin/runQuery.sh

### Example (pre defined): Get the published nodes

    ./bin/runQuery.sh example2

### Example (arbitrary): Get the status of the nodes for FLS

    ./bin/runQuery.sh advertisement-fp getnodestatus
