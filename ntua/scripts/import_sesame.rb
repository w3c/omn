class MyNewClass
require 'rubygems'
require 'rdf/sesame'
require 'owlim'
begin 
input_file , repository = ARGV 

url  = RDF::URI("http://localhost:8080/openrdf-sesame")
#conn = RDF::Sesame::Connection.open(url)
uri = "http://localhost:8080/openrdf-sesame"
#uri2 = RDF::URI("http://localhost:8080/openrdf-sesame/repositories/2")
owlim = OWLIM.new(uri)
#puts owlim.host
#puts owlim.list
#server = RDF::Sesame::Server.new(url)
  #   server = RDF::Sesame::Server.new("http://localhost:8080/openrdf-sesame")
#    repository = RDF::Sesame::Repository.new(:server => server, :id => 'd')
 #    url = "http://localhost:8080/openrdf-sesame/repositories/SYSTEM"
 #    repository = RDF::Sesame::Repository.new(url)
#server.initialize 
#repository = server.repository(:d)
#puts repository.insert("c:/rspec/novi_im.owl")
#repository = "New2"
#owlim.create(repository)
# owlim.drop('New2')
#repository = 'rspec_owl'
#owlim.import(repository, 'c:/rspec/pros.owl', :format => "rdfxml")
owlim.clear(repository)
owlim.import(repository, input_file, :format => "rdfxml")
#puts "TRY"
#puts repository.title
#owlim.prefix_hash["im"] = "http://fp7-novi.eu/im.owl#"
#test = 'SELECT DISTINCT ?type WHERE { ?subj rdf:type ?type }'
#test = 'SELECT ?node1 where {  ?node1 rdf:type im:Node. ?node1 im:hardwareType "pc"}'
#owlim.query('d', test, :prefix =>owlim.prefix) {|x| print x}
#puts owlim.prefix
#result = owlim.query(repository, sparql)
#server.each_repository do |repository|
#  puts repository.inspect
#end
#puts conn. connected?
#server.protocol 
#conn.close
 # puts "YES"
rescue Exception=> e 
#raise "Missing arguments" if  ARGV.empty? or ARGV.size.eql?(1)
  puts e.message  
  puts e.backtrace.inspect 
p "Exception"
  # Excep

  # tion handling code here.
  # Don't write only "rescue"; that only catches StandardError, a subclass of Exception.
end
end
