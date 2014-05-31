Open-Multinet Ontology Playground
=================================

Abstract
--------

Playground for the ontology work focussing on putting ontologies in RSpecs.

Suggested Setup
---------------
 * [sparql](http://jena.apache.org/documentation/tools/) (command line tool from jena to query data)
 * [Protégé](http://protege.stanford.edu) (graphical ontology editor)
 * [Eclipse Xturtle](http://aksw.org/Projects/Xturtle.html) (text based ontology editor with code completion and simple validation)
 * [rdf.sh](https://github.com/seebi/rdf.sh) (command line ontology tools)
 * [OwlToUml](https://github.com/twalcari/OwlToUml) (to visualize simple ontologies as plantuml diagrams)
 * [rapper](http://librdf.org/raptor/rapper.html) (command line tool to convert and check ontologies)
 * [lodlive](http://en.lodlive.it) (user friendly vizualization of a sparql endpoint)
 * [yasgui](http://yasgui.laurensrietveld.nl) (user friendly query of a sparql endpoint)
 * [LODE](http://www.essepuntato.it/lode) (documentation generator for OWL files)

Directory Layout
----------------
 * bin: executables
 * data: example data to work with (to help validating and discussing the ontologies)
 * generated: auto generated files based (e.g. documentation, graphical representation, other serializations)
 * import: related ontologies to be reused (second main focus)
 * ontologies: upper ontologies (to main focus)
 * queries: example queries to work with (to help validating and discussing the ontologies)
 
How To
------

### Run example Queries

    ./bin/runQuery.sh

### Example (pre defined): Get the published nodes

    ./bin/runQuery.sh example2
    
### Example (arbitrary): Get the status of the nodes for FLS

    ./bin/runQuery.sh advertisement-fp getnodestatus
