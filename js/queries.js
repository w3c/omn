$( document ).ready(function() {
  prefixes=
`PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX omn: <http://open-multinet.info/ontology/omn#>
PREFIX omnfed: <http://open-multinet.info/ontology/omn-federation#>
PREFIX omnres: <http://open-multinet.info/ontology/omn-resource#>
PREFIX omnlc: <http://open-multinet.info/ontology/omn-lifecycle#>
PREFIX omnwireless: <http://open-multinet.info/ontology/omn-domain-wireless#>
PREFIX omncomp: <http://open-multinet.info/ontology/omn-component#>
PREFIX dbpedia: <http://dbpedia.org/property/>
`
  $('#findAMs').click(function(){
    yasqe.setValue(findAMs);
    yasqe.query(yasr.setResponse);
    return false;
  });
  findAMs=prefixes+`
SELECT DISTINCT ?infrastructureName ?infrastructureURI ?amURI ?endpointURL WHERE {
  ?amURI rdf:type <http://open-multinet.info/ontology/omn-domain-geni-fire#AMService> .
  OPTIONAL {?amURI omn:hasEndpoint ?endpointURL }
  OPTIONAL {?infrastructureURI omn:hasService ?amURI ; rdfs:label ?infrastructureName .}
} ORDER BY DESC (?infrastructureName) LIMIT 200`

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

  $('#findAvailResources').click(function(){
    yasqe.setValue(findAvailResources);
    yasqe.query(yasr.setResponse);
    return false;
  });
  findAvailResources=prefixes+`
SELECT  ?AM (count (?resource) as ?no) WHERE {
?resource rdf:type omnres:Node.
?resource omnres:isAvailable "true"^^xsd:boolean. 
?resource omnlc:managedBy ?AM.
} group by ?AM`


  $('#findHardware').click(function(){
    yasqe.setValue(findHardware);
    yasqe.query(yasr.setResponse);
    return false;
  });
  findHardware=prefixes+`
SELECT DISTINCT ?model ?harware_type WHERE {
?resource rdf:type omnres:Node.
?resource omn:hasComponent ?cpuComp. 
?cpuComp rdf:type omncomp:CPU.
?cpuComp rdfs:label ?model.  
?resource omnres:hasHardwareType ?type1.
?type1 rdf:type omnres:HardwareType.
?type1 rdfs:label ?harware_type.
}`

  $('#findwirelessAMs').click(function(){
    yasqe.setValue(find wirelessAMs);
    yasqe.query(yasr.setResponse);
    return false;
  });
  findwirelessAMs=prefixes+`
SELECT DISTINCT ?am  WHERE   {
?node rdf:type omnres:Node.
?node omnres:hasInterface ?interface. 
?node  omnlc:managedBy ?am. 
?interface rdf:type omnwireless:WirelessInterface.
}`

  $('#findAMDnodes').click(function(){
    yasqe.setValue(findAMDnodes);
    yasqe.query(yasr.setResponse);
    return false;
  });
  findAMDnodes=prefixes+`
SELECT  ?am (count(*) as ?count) WHERE   {
?node rdf:type omnres:Node.
?node omnlc:managedBy ?am. 
?node omn:hasComponent ?cpuComp. 
?cpuComp rdf:type omncomp:CPU.
?cpuComp rdfs:label ?model. filter(regex(str(?model), 'AMD','i')).  
} GROUP BY ?am`


  $('#find80211bnodes').click(function(){
    yasqe.setValue(find80211bnodes);
    yasqe.query(yasr.setResponse);
    return false;
  });
  find80211bnodes=prefixes+`
SELECT DISTINCT ?node WHERE {
?node rdf:type omnres:Node.
?node <http://www.w3.org/2003/01/geo/wgs84_pos#long> ?lon1.
?node <http://www.w3.org/2003/01/geo/wgs84_pos#lat> ?lat1. 
FILTER( (37.971472-xsd:float(?lat1))*( 37.971472-xsd:float(?lat1))+( 23.726633-xsd:float(?lon1))*( 23.726633-xsd:float(?lon1))*( 0.942964-(0.0084674*xsd:float(?lat1))) < 0.00808779738472242*250/100).
?node omnres:hasInterface ?interface. 
?interface rdf:type omnres:Interface.
?interface omn:hasComponent/omnwireless:supportsStandard/rdfs:label '802.11b'^^xsd:string. 
}`


  $('#findStorage').click(function(){
    yasqe.setValue(findStorage);
    yasqe.query(yasr.setResponse);
    return false;
  });
  findStorage=prefixes+`
SELECT DISTINCT ?node WHERE {
?node rdf:type omnres:Node.
?node omn:hasComponent ?storage.
?storage rdf:type  omncomp:StorageComponent. 
?storage dbpedia:storage ?value1. 
} ORDER BY DESC(?value1) LIMIT 3`



  $('#findWireless').click(function(){
    yasqe.setValue(findWireless);
    yasqe.query(yasr.setResponse);
    return false;
  });
  findWireless=prefixes+`
SELECT DISTINCT ?node WHERE {
?node rdf:type omnres:Node.
?node omn:hasComponent ?cpu.
?cpu rdf:type omncomp:CPU.  
?cpu dbpedia:fastest ?cpu_value1.
FILTER(?cpu_value1 >= '100.0'^^<http://www.w3.org/2001/XMLSchema#float>).
?node omnlc:managedBy ?am.
?node omn:hasComponent  ?storage.
?storage rdf:type omncomp:StorageComponent.
?storage dbpedia:storage ?value1.
FILTER(?value1 >= '1.0'^^<http://www.w3.org/2001/XMLSchema#float>).
?node omnres:hasInterface ?interface1. 
MINUS  {SELECT ?node WHERE {?node rdf:type  omnres:Node.
?node  omnlc:hasLease ?life1.
?life1  rdf:type  omnlc:Lease.
?life1  omnlc:expirationTime ?end1.
?life1  omnlc:startTime ?start1.
FILTER (((xsd:dateTime(?start1) >= '2015-12-14T15:00:00+03:00'^^xsd:dateTime ) && (xsd:dateTime(?end1) < '2015-12-14T17:00:00+03:00'^^xsd:dateTime )) || ((xsd:dateTime(?end1) >= '2015-12-14T17:00:00+03:00'^^xsd:dateTime ) &&   (xsd:dateTime(?start1) < '2015-12-14T17:00:00+03:00'^^xsd:dateTime )) || ((xsd:dateTime(?end1) > '2015-12-14T15:00:00+03:00'^^xsd:dateTime ) && (xsd:dateTime(?start1) <= '2015-12-14T15:00:00+03:00'^^xsd:dateTime )))}}} `

});
