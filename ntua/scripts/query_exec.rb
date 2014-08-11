

  require 'rubygems'
#  require 'rdf/sesame'
  require 'owlim'
  require 'net/http'
  require 'sparql/client'
def  MYNEWCLASS (test)
  begin 
#url  = RDF::URI("http://localhost:8080/openrdf-sesame")
#  conn = RDF::Sesame::Connection.open(url)
#  uri = "http://localhost:8080/openrdf-sesame"
#  owlim = OWLIM.new(uri)
#host = ENV['SESAME_URL'] || "http://localhost:8080/openrdf-sesame"
#serv = OWLIM.new(host)
 http = Net::HTTP.new("localhost", "8080")

#     server = RDF::Sesame::Server.new(url) 
#    conn = RDF::Sesame::Connection.open(url)
     #server.initialize 
    puts http.read_timeout
#
# puts http.methods.sort 
    http.read_timeout = 1500

puts sparql.bytesize

sparql1 = SPARQL::Client.new('http://localhost:8080/openrdf-sesame/repositories/rspec_owl')

queryString="PREFIX nml: <http://schemas.ogf.org/nml/2013/05/base#>"
queryString +="PREFIX indl: <http://www.science.uva.nl/research/sne/indl#>"
queryString +="PREFIX indl_wireless: <http://www.semanticweb.org/mgiatili/ontologies/2014/2/untitled-ontology-16#>"
queryString += test#sparql#"SELECT * WHERE { ?s ?p ?o } OFFSET 100 LIMIT 10"

puts queryString
  puts   Time.now.to_datetime 
query= sparql1.query(queryString)
query.each_solution do |solution|
  puts solution.inspect
end

   # sparql1.close
#    http.close# = Net::HTTP
#serv.query(repository.id, sparql, :continue_timeout => nil, :bytesize => 16000) {|x| print x}

  puts   Time.now.to_datetime 

# owlim.query(repository.id,  test, :prefix =>owlim.prefix, :format => "xml") {|x| print x}
#  conn.close
 #   RESULT =  result.dup
puts "YES"
return  query  
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

#MYNEWCLASS("OO")