Ontology Discussions
====================

Abstract
--------

Temporary working space for ontology related tasks within http://open-multinet.info and http://www.fed4fire.eu.


Suggested Setup
---------------
 * sparql (command line tool from jena to query data)
 * Protégé (graphical ontology editor)
 * Eclipse Xturtle (text based ontology editor with code completion and simple validation)
 * rdf.sh (command line ontology tools)
 * OwlToUml (to visualize simple ontologies as plantuml diagrams)
 * rapper (command line tool to convert and check ontologies)
 

Directory Layout
----------------
 * bin: executables
 * data: example data to work with (to help validating and discussing the ontologies)
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
