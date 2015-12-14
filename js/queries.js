$( document ).ready(function() {
  prefixes=
`PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX omn: <http://open-multinet.info/ontology/omn#>
PREFIX omnfed: <http://open-multinet.info/ontology/omn-federation#>
PREFIX omnres: <http://open-multinet.info/ontology/omn-resource#>
PREFIX omnlc: <http://open-multinet.info/ontology/omn-lifecycle#>
`
  $('#findAMs').click(function(){
    yasqe.setValue(findAMs);
    yasqe.query(yasr.setResponse);
    return false;
  });
  findAMs=prefixes+`
SELECT DISTINCT ?name ?am ?endpoint WHERE {
  ?infra omn:hasService ?am ;
         rdfs:label ?name .
  ?am rdf:type <http://open-multinet.info/ontology/omn-domain-geni-fire#AMService> ;
      omn:hasEndpoint ?endpoint .
} LIMIT 100`

  $('#findInfras').click(function(){
    yasqe.setValue(findInfras);
    yasqe.query(yasr.setResponse);
    return false;
  });
  findInfras=prefixes+`
SELECT DISTINCT  ?name ?uri  WHERE {
  ?fed omnfed:hasFederationMember ?uri .
  OPTIONAL { ?uri rdfs:label ?name }
} ORDER BY DESC (?name) LIMIT 100`


  $('#findOverview').click(function(){
    yasqe.setValue(findOverview);
    yasqe.query(yasr.setResponse);
    return false;
  });
  findOverview=prefixes+`
SELECT
  (COUNT (DISTINCT ?manager) as ?managers)
  (COUNT (DISTINCT ?node) as ?nodes)
  (COUNT (DISTINCT ?interface) as ?interfaces)
  (COUNT (DISTINCT ?link) as ?links)
WHERE {
  {?component omnlc:hasComponentManagerName ?manager .}
  UNION {?node a omnres:Node .}
  UNION {?interface a omnres:Interface .}
  UNION {?link a omnres:Link .}
}`

});
