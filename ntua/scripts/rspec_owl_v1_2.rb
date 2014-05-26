require 'rubygems'
require 'xmlsimple'
require 'Owl'
require 'rdf'
#require 'yowl'
#require 'rdf/raptor'
require 'rexml/document'
require 'rexml/streamlistener'
require 'xml/mapping'
include REXML
require 'C:\RSPEC_OWL_F\novi.rb'
require 'C:\RSPEC_OWL_F\rspec_header_file.rb'
require 'C:\RSPEC_OWL_F\propertyof.rb'
#include Novi.classes
$properts = Hash.new#{|h,k| h[k] = []}
$individuals = Hash.new

class Federica_new
def   ontology_name
  ontology_name = "http://www.semanticweb.org/mary/ontologies/2014/2/untitled-ontology-65" 
  return ontology_name
end 
def min_protocol_rate (b)
rate = case b 
when 'IEEE802_11a' then 6 
when 'IEEE802_11b' then 1
when 'IEEE802_11g' then 6
when 'IEEE802_11n' then 7.2
else 1 end 

return rate
end 
def protocol_bandwidth (b)
bandwidth = case b 
when 'IEEE802_11a' then 20
when 'IEEE802_11b' then 20
when 'IEEE802_11g' then 20
when 'IEEE802_11n' then 20
else 20 end 

return bandwidth
end 
def  calculate_rate (a, b, nodes , v )
 ab = distance(a, b)**(-2)
 kb = 0.0001
 nodes.each  {|k, v1| 
   if not v1[0].eql?(a[0]) and not v1[0].eql?(b[0])
      kb += distance(v1, b)**(-2)
   end 
 }
 g = ab/kb
 rate = v*(Math.log(1+g)/Math.log(2))  

return rate
end 
def distance (a, b )

  lat1 = a[2].to_f
  lon1 = a[1].to_f 
  lat2 = b[2].to_f
  lon2 = b[1].to_f
  x1= a[3].to_f
  y1= a[4].to_f
  z1= a[5].to_f
  x2= b[3].to_f
  y2= b[4].to_f
  z2= b[5].to_f
  
   rad_per_deg = 0.017453293 #  PI/180
   rmeters = 6371000    # radius in meters 
   distances = Hash.new   # this is global because if computing lots of track point distances, it didn't make
   dlon = lon2 - lon1
   dlat = lat2 - lat1

   dlon_rad = dlon * rad_per_deg
   dlat_rad = dlat * rad_per_deg
    
   lat1_rad = lat1 * rad_per_deg
   lon1_rad = lon1 * rad_per_deg
    
   lat2_rad = lat2 * rad_per_deg
   lon2_rad = lon2 * rad_per_deg
        
   a = (Math.sin(dlat_rad/2))**2 + Math.cos(lat1_rad) * Math.cos(lat2_rad) * (Math.sin(dlon_rad/2))**2
   c = 2 * Math.atan2( Math.sqrt(a), Math.sqrt(1-a))

   dMeters = rmeters * c  
  
if dMeters.eql?(0.to_f)
  dMeters =  Math.sqrt((x1-x2)**2 + (y1-y2)**2 +(z1-z2)**2)
end 

return  dMeters

end 
   
def addwirelessfeatures(temp,channels,k,v, klasses_table, type, ax)
  #im = "http://fp7-novi.eu/im.owl"
  nml = "http://fp7-novi.eu/im2.owl"
  parent_node = ""
  
  v.each {|h, n| if h.eql?('component_id') 
  parent_node = n.split(/[^a-zA-Z0-9_\-]/, -1).last
 # puts parent_node
  end }

  #FINDS THE MODEL THE CLASS BELONGS TO 
Novi::CL.each{|p, o|
if  p.to_s.split('#').last.eql?('WirelessFeatures')
  nml = p.to_s.split('#').first
end 
}   

  parent_node = [parent_node, '_', type].join
  list_channels = Hash.new  
  list_protocols = Hash.new
  haschannel = Hash.new
  haschannel_s = ""
  hasprotocol = Hash.new
  hasprotocol_s = ""
try1 = {}
try1.compare_by_identity
try1 = {"rdf:about"=>["#{ontology_name}#", 'wirelessfeatures','_', parent_node].join}
res = {"rdf:resource"=>"#{nml}#WirelessFeatures"}
(try1['rdf:type'] ||= []) << res
 # res = ["#{ontology_name}#", 'wirelessfeatures','_', parent_node].join
#(try1["rdf:about"] ||= []) << res

  channels.each {|c, b|
      b.each {|a|
         haschannel = {"rdf:resource"=> ["#{ontology_name}#",'Channel','_',a['channel_num']].join }#'name'=>haschannel_s}
         (try1['indl_wireless:hasChannel'] ||= []) << haschannel
        }
  }
  i= 0 
 
  
  (temp['indl_wireless:hasWirelessFeatures'] ||=[])<< {"rdf:resource"=>["#{ontology_name}#",'wirelessfeatures','_', parent_node].join} 
  $properts['indl_wireless:hasWirelessFeatures'] = true
  $properts['indl_wireless:hasChannel'] = true
 # $properts['indl_wireless:hasProtocol'] = true
  ($properts['WirelessFeatures']  ||= []) <<  "Class" 
  pros = ['WirelessFeatures', '-', parent_node].join
  oout = XmlSimple.xml_out(try1, 'RootName'=> "owl:NamedIndividual" , 'NoAttr'=> false , 'ContentKey' => 'name', 'AttrPrefix' => false)				
  #puts oout 
ax = try1
    return temp, ax
end 

def addlifetime(temp,node , leases, klasses_table , granularity)
  
  im = "http://fp7-novi.eu/im.owl"
  timeline = Hash.new 
  id = node['component_id']
  timeline = {'1325376000'=>"start"}
  (timeline['64060588800']  ||= []) << "end".to_s
  leases.each { |f|  
      if f['node'][0]['component_id'].eql?(id) 
         test = f['start_time'].to_i  + (f['duration'].to_i*granularity)     
         if not timeline[f['start_time']].eql?(nil)
            timeline.delete(f['start_time'])
         else 
            (timeline[f['start_time']]  ||= []) << "end"
         end 
         if not timeline[test.to_s].eql?(nil)
            timeline.delete(test.to_s)
         else  
            (timeline[test.to_s]  ||= []) << "start"
         end
#         if not f['offset'].eql?(nil)
#           (timeline[f['offset'].to_i*granularity]  ||= []) << "offset"
#         else 
#           puts "ll"
#           (timeline['0'.to_i]  ||= []) << "offset" 
#             puts timeline
#         end  
      #temp['hasLifetime']= {"rdf:resource"=>['Lifetime','_', id].join}#kleidi[0].values} 
      end 
      }
      timeline =  timeline.sort

      i= 0 
      while i< timeline.size
       (temp['nml:existsDuring'] ||=[])<< {"rdf:resource"=>["#{ontology_name}#", 'Lifetime',i,'_', id.split(/[^a-zA-Z0-9_\-]/, -1).last].join}#kleidi[0].values} 
        pros = ['Lifetime', '-', id, i].join
 #(klasses_table [pros] ||= []) << {"my_type"=>['Lifetime',i,'_', id].join}
       (klasses_table [pros ] ||= []) << {"my_type"=>["#{ontology_name}#",'Lifetime',i,'_', id.split(/[^a-zA-Z0-9_\-]/, -1).last].join, case timeline[i][1].kind_of?(String) 
when true then timeline[i][1] else timeline[i][1][0] end=> Time.at(timeline[i][0].to_i).to_datetime.to_s, 
timeline[i+1][1][0]=>Time.at(timeline[i+1][0].to_i).to_datetime.to_s
}
       i+=2
      end 
   	  $properts['nml:existsDuring'] = true
      ($properts['Lifetime']  ||= []) <<  "Class" 

tt= ['Lifetime',i,'_', id.split(/[^a-zA-Z0-9_\-]/, -1).last].join
  
return temp
end

def included (name)
  #im = "http://fp7-novi.eu/im.owl"
  nml = "http://schemas.ogf.org/nml/2013/05/base"
  indl="http://www.science.uva.nl/research/sne/indl"
  model_im = XmlSimple.xml_in("C:/RSPEC_OWL_F/indl.owl",  { 'KeyAttr' => 'name', 'ForceArray' => true})
  klass = model_im['Class']
  klass.each do |x| 
    if  x.values.include?("#{indl}##{name}") 
      return  true 
    end
  end  
  model2_im = XmlSimple.xml_in("C:/RSPEC_OWL_F/nml-base.owl",  { 'KeyAttr' => 'name', 'ForceArray' => true})
  klass = model2_im['Class']
  klass.each do |x| 
    if  x.values.include?("#{nml}##{name}") 
      return  true 
    end
  end  
  return false
end 

def replace1 (data,lexiko) 
  if lexiko.include?(data)
    data.replace(lexiko[data])
  end 
end 


Lexiko = {"component_id"=> ["rdf:about"],
"my_type"=>["rdf:about"],
 "node" => ["Node"],
 # "link"=> ["Link"], 
 "location"=> ["nml:locatedAt"], 
 "isSink"=>["nml:isSink"],
 "link"=> ["nml:isSource" ], 
#"exclusive" => ["exclusive"],
#"hardware_type" => ["hardwareType"], 
"available" => ["available"],
"interface" => ["nml:hasInboundPort", "nml:hasOutboundPort"], 
"cpuSpeed" =>["indl:clockSpeed"],
"numCPUCores"=>["indl:cores"],
"diskSize"=>["indl:size"],
"ramSize"=>["indl:size"],
#"availableLogicalRouters" => ["hasLogicalRouters"],
#"capacity"=> ["hasCapacity"],
#"sliver_type"=>["virtualRole"], 
 "longitude"=>["nml:long"], 
 "latitude"=>["nml:lat"] ,
 "start"=>["nml:start"], 
 "end"=>["nml:end"], 
 "hasChannel"=> ["indl_wireless:hasChannel"], 
 "hasProtocol"=>["indl_wireless:hasProtocol"], 
 "rdf:Bag"=> ["rdf:Bag"], 
"rdf:li"=>["rdf:li"],
  "channel_num"=>["indl_wireless:ChannelNo"], 
    "frequency"=>["indl_wireless:frequency"],
      "standard"=>["indl_wireless:protocol"]
}

$klasses_table = Hash.new 
$klasses_table2 = Hash.new 
$klass_table = Hash.new

def propert
  @propert = Hash.new{|h,k| h[k] = []}
  return @propert
end




def function_interface (klasses_table, type, channels)
  nml ="http://schemas.ogf.org/nml/2013/05/base"
  temp = Hash.new
  ax = Hash.new 
  temp = {}
  temp.compare_by_identity
  all_nodes = ""
  oout = ""
  klasses_table_dup = klasses_table.dup
  klasses_table_dup.each do  | k, v|
    if k.start_with?('interface')
      parent_node = k.partition('-').last
      Novi::CL.each{|p, o|
      if  p.to_s.split('#').last.eql?('Port')
        nml = p.to_s.split('#').first
      end 
    }   
      temp = { "rdf:type"=> {"rdf:resource"=>"#{nml}#Port"}}
      v.each { |key|
       temp = {}
       temp.compare_by_identity
       all_nodes = ""
       temp = { "rdf:type"=> {"rdf:resource"=>"#{nml}#Port"}}
       key.keys.each do |a|
       kleidi = key[a]
       if Lexiko.include?(a)  
            if a.eql?("component_id")
              #kleidi = kleidi.split(/[^a-zA-Z0-9_\-]/, -1).last 
              pros = [kleidi, type].join('-')
             #  klass_table = {"#{pros}"=>"#{pros}"}
              temp[Lexiko[a][0]]  =  ["#{ontology_name}#", kleidi.split(/[^a-zA-Z0-9_\-]/, -1).last,'-', type].join
            else  
              if included(a.capitalize) 
                # puts "Is Class" 
                pros = kleidi
                if type.eql?('out')
                   temp.compare_by_identity
                   kl =  {"rdf:resource"=>"#{ontology_name}##{kleidi}"}
                   temp["#{Lexiko[a][0].to_s}"]= kl #[a,'_', key['component_id']].join}#kleidi[0].values} 
                   $properts[Lexiko[a][0].to_s] = true
                   pros = kleidi#[a, key['component_id']].join('-')
                  (klasses_table [pros] ||= []) << {"my_type"=>"#{ontology_name}##{kleidi}"}  
                end
   			   elsif Propertyof.isproperty(Lexiko[a][0].partition(':').last).eql?('Port') or Propertyof.isproperty(Lexiko[a][0].partition(':').last).eql?('NetworkObject')      
                 if  kleidi.size ==1 and not kleidi.kind_of?(String) 	
                    if  type.eql?('in')
                      temp.compare_by_identity
                      temp["#{Lexiko[a][0].to_s}"] =  kleidi 
                    end  
                 else        
           	        temp[Lexiko[a]]= {"name" =>kleidi} 
           	     end 
               	$properts[Lexiko[a][0].to_s] = true
               end
              end
            end 
            ($individuals["#{[kleidi, type].join('-')}"] ||= []) << "#{[kleidi, type].join('-')}"
        end#key.keys
     temp, ax = addwirelessfeatures(temp,channels,k,key, klasses_table, type, ax )                             
     
           out1 = XmlSimple.xml_out(temp, 'RootName'=> "owl:NamedIndividual" , 'NoAttr'=> false , 'ContentKey' => 'name', 'AttrPrefix' => false )
     oout = [oout,out1].join(' ')
         out1 = XmlSimple.xml_out(ax, 'RootName'=> "owl:NamedIndividual" , 'NoAttr'=> false , 'ContentKey' => 'name', 'AttrPrefix' => false )
     oout = [oout,out1].join(' ')

}

  end   #end interface
 end

#  oout = XmlSimple.xml_out(temp, 'RootName'=> "owl:NamedIndividual" , 'NoAttr'=> false , 'ContentKey' => 'name', 'AttrPrefix' => false ) 
 return oout
end 

def function_nodes (nodes, type, klasses_table, klasses_table2, channels, leases)
#	im = "http://fp7-novi.eu/im.owl"
 	nml = "http://schemas.ogf.org/nml/2013/05/base"
	temp = Hash.new
	temp.compare_by_identity
	individual = String.new
	individual = ""
	klass_table2 = Hash.new
	klasses_table2 =""
	all_nodes = ""
	nodes.each do  |k|
    Novi::CL.each{|p, o|
    if  p.to_s.split('#').last.eql?(type.slice(0,1).capitalize + type.slice(1..-1))
      nml = p.to_s.split('#').first
    end 
  }    

	 temp = { "rdf:type"=> {"rdf:resource"=>"#{nml}##{type.slice(0,1).capitalize + type.slice(1..-1)}"}} 
	   k.keys.each do |key|
    		    kleidi = k[key]
    		    if key.eql?("component_id")
        		   individual = kleidi
                  (klass_table2["#{kleidi}"] ||= []) <<"#{kleidi}" #klass_table[kleidi] = true
            end
  			    if included(key.capitalize) or key.eql?('interface')
  			      #puts "Is Class"
     				  pros = [key, k['component_id']].join('-')
            #     klasses_table [pros] =kleidi #[k.values[0], kleidi]
              kleidi.each { |k|
                      (klasses_table ["#{pros}" ] ||= []) << k
                  }
   			    end 
   			    i = 0

   			    while  Lexiko.include?(key)  and (
   			          (i < kleidi.length and kleidi.kind_of?(Array)) or (kleidi.kind_of?(String) and i < 1)
   			          )

   			   #       puts "while"

   			          $properts[Lexiko[key][i]] = true
  		    	      if Lexiko[key].length > 1 #interface--must be split
    			         if included(key.capitalize) or key.eql?('interface')
    			           #  ($properts["#{Lexiko[key]}"]  ||= []) <<  "Class" 
    			           #  ($properts["#{key.capitalize}"]  ||= []) <<  "Class" 
    			              ($properts["Port"]  ||= []) <<  "Class" 
            		         pros = [kleidi[i].values.first.split(/[^a-zA-Z0-9_\-]/, -1).last,"in"].join('-')
                		     (temp["#{Lexiko[key].first}"] ||= []) << {"rdf:resource"=>"#{ontology_name}##{pros}"}
           			         pros = [kleidi[i].values[0].split(/[^a-zA-Z0-9_\-]/, -1).last,"out"].join('-')
                			 (temp["#{Lexiko[key][1]}"] ||= []) << {"rdf:resource"=>"#{ontology_name}##{pros}"}
         			     else     
             			     temp[Lexiko[key][0]]= kleidi
             			     temp[Lexiko[key][1]]= kleidi#+"-out"
             			 end 
       	    	    else
         			     if included(key.capitalize) or key.eql?('interface')
            			     #pros = kleidi[0].values
            			     temp[Lexiko[key]]= {"rdf:resource"=>["#{ontology_name}#",key,'_', individual.split(/[^a-zA-Z0-9_\-]/, -1).last].join}#kleidi[0].values} 
            			     pros = [key, k['component_id']].join('-')
                       klasses_table [pros][0]["my_type"]= ["#{ontology_name}#",key,'_', individual.split(/[^a-zA-Z0-9_\-]/, -1).last].join
          			    else      			          
         			        if  key.eql?("component_id") or key.eql?("my_type")
         			            temp[Lexiko[key]] = ["#{ontology_name}#",kleidi.split(/[^a-zA-Z0-9_\-]/, -1).last].join
            			    elsif kleidi.size ==1 and kleidi.kind_of?(Array)#new  
         			            if not Propertyof.isproperty(Lexiko[key][0].partition(':').last).eql?(type.slice(0,1).capitalize + type.slice(1..-1)) and not Propertyof.isproperty(Lexiko[key][0].partition(':').last).eql?('Resource') and not Propertyof.isproperty(Lexiko[key][0].partition(':').last).eql?('NetworkObject')
                                 if included(Propertyof.isproperty(Lexiko[key][0].partition(':').last))
    		                            $properts[Lexiko[key]] = true 
    		                            name =[individual, Propertyof.isproperty(Lexiko[key].partition(':').last).downcase!.to_s].join(".")                       
    		                            name = [name, kleidi[i]['name'].to_s].join(".") 
            	                      ($properts["#{Propertyof.isproperty(Lexiko[key][0].partition(':').last) }"]  ||= []) <<  "Class"
             	                      pros = [Propertyof.isproperty(Lexiko[key].partition(':').last), name].join('-')
                                    (klasses_table2 [pros] ||= []) << 
                                       {"component_id"=> "#{name}","#{key}"=> case kleidi[i].class when String.class then kleidi[i] else kleidi[i].values end}
             			               end
    			                end  
         			            j = 0
         			            if not kleidi[i].is_a?(String)
         			               while j<kleidi[i].length 
         			                  if kleidi[i].keys[j].eql?("name") and (Propertyof.isproperty(Lexiko[key].partition(':').last).eql?(type.slice(0,1).capitalize + type.slice(1..-1)) or Propertyof.isproperty(Lexiko[key].partition(':').last).eql?('NetworkObject'))
             			                  temp[Lexiko[key]] = kleidi[i]
             			                  name = kleidi[i]
             			              else
                     			          if key.eql?("available") and Lexiko.include?(kleidi[i].keys[j].partition(':').last) 
                         			           if Propertyof.isproperty(Lexiko[kleidi[i].keys[j].partition(':').last][0].partition(':').last).eql?(type.slice(0,1).capitalize + type.slice(1..-1))
                                                   $properts[Lexiko[kleidi[i].keys[j].partition(':').last]] = true
             			                           temp[Lexiko[kleidi[i].keys[j].partition(':').last][0]] = {"name" =>  kleidi[i].values[j]}
             			                       else  
              			                           name =[ Propertyof.isproperty(Lexiko[kleidi[i].keys[j].partition(':').last][0].partition(':').last).to_s,individual.split(/[^a-zA-Z0-9_\-]/, -1).last].join("_")
             			                           $properts[Lexiko[kleidi[i].keys[j].partition(':').last][0]] = true 
             			                           
             			                           if included(Propertyof.isproperty(Lexiko[kleidi[i].keys[j].partition(':').last][0].partition(':').last))
            			                               ($properts["#{Propertyof.isproperty(Lexiko[kleidi[i].keys[j].partition(':').last][0].partition(':').last) }"]  ||= []) <<  "Class"
             			                                pros = [Propertyof.isproperty(Lexiko[kleidi[i].keys[j].partition(':').last][0].partition(':').last), name].join('-')
                                                        (klasses_table [pros] ||= []) << 
                                                        {"component_id"=> "#{name}","#{kleidi[i].keys[j].partition(':').last}"=> "#{kleidi[i].values[j]}"} 
  
                                			       end
             			                           (temp["indl:hasComponent"] ||= []) << {"rdf:resource"=>["#{ontology_name}#","#{name}"].join} 
             			                          ($properts["indl:hasComponent"]  ||= []) <<  "true"
             			                       end 
             			                  elsif not key.eql?("available") and (Propertyof.isproperty(Lexiko[key].partition(':').last).eql?(type.slice(0,1).capitalize + type.slice(1..-1))or Propertyof.isproperty(Lexiko[key].partition(':').last).eql?('NetworkObject'))
             			               
             			                       temp[Lexiko[key]] = kleidi[i].keys[j]
             			                  end
                              	      end 
         			                  j+=1
         			            end
         			            else #is a string 
         			                  temp[Lexiko[key]]= kleidi#{"name" =>kleidi} 
         			            end
         			            
         			        elsif Propertyof.isproperty(Lexiko[key][0].partition(':').last).eql?(type.slice(0,1).capitalize + type.slice(1..-1)) or Propertyof.isproperty(Lexiko[key][0].partition(':').last).eql?('NetworkObject')
              			            temp[Lexiko[key]]= {"rdf:datatype"=> Propertyof.range(Lexiko[key][0].partition(':').last),"name" =>kleidi} 
           			      else 
           			        puts "Not found"    
         			        end
         			     end
   				      end 
   				 
   				  i +=1
   				 
    			end 
  		end 
  		if type.eql?('node')
    		temp = addlifetime(temp,k , leases, klasses_table, k['granularity'][0].to_i)
      end
            
	# temp['hasLifetime']= {"rdf:resource"=>['Lifetime','_', individual].join}
		oout = XmlSimple.xml_out(temp, 'RootName'=> "owl:NamedIndividual" , 'NoAttr'=> false , 'ContentKey' => 'name', 'AttrPrefix' => false )				
		all_nodes = [all_nodes, oout].join(' ')

    $individuals = klass_table2

	end  
    #puts "TESTED"
        
	return all_nodes
end 

end 

#=============================================================
#==========================================================
begin

  puts   Time.now.to_datetime  
  p "START"
input_file, output_file = ARGV 
#im = "http://fp7-novi.eu/im.owl"
nml = "http://schemas.ogf.org/nml/2013/05/base"
temp = Hash.new
temp.compare_by_identity
ontology_name = Hash.new
klasses_table = Hash.new
klasses_table2 = Hash.new
channels = Hash.new
leases = Hash.new
test = Federica_new.new 
node_position = Hash.new
all_nodes = ""
all_nodes0 = ""
all_nodes2 = ""
all_nodes4 = ""
all_nodes3 = ""
  all_nodes5 = ""
f= open('pros_2.owl', 'w+') 
#f = open(output_file, 'w+')
f.write(RspecHeaderFile::HEADER )
ontology_name = { "rdf:about"=> test.ontology_name}
oout = XmlSimple.xml_out(ontology_name, 'RootName'=> "owl:Ontology" , 'NoAttr'=> false , 'ContentKey' => 'name', 'AttrPrefix' => false, 'OutputFile' => f )

frspec = XmlSimple.xml_in("C:/RSPEC_owl_f/nitos_rspecs_v0.1_2_nodes.xml", 'ForceArray' => true, 'KeyAttr'=>true)#,'fedad' => "http://sorch.netmode.ntua.gr/ws/RSpec/ext/federica")
#frspec = XmlSimple.xml_in(input_file, 'ForceArray' => true, 'KeyAttr'=>true)#,'fedad' => "http://sorch.netmode.ntua.gr/ws/RSpec/ext/federica")
#frspec = XmlSimple.xml_in("node_link1.xml", 'ForceArray' => true, 'KeyAttr'=>true)
frspec_pros  = frspec

#READS THE CHANNELS
channels =  frspec_pros['network'][0]['spectrum'][0]#.index 


#READS THE LEASES 
leases = frspec_pros['network'][0]['lease']
index = frspec_pros["network"][0]#.index { |h| #puts h.keys.eql?("node") # h.keys[0].eql?("node")}
index = frspec_pros["network"][0]#.index { |h| #puts h.keys.eql?("node") # h.keys[0].eql?("node")}
frspec_pros = index 


  
puts   Time.now.to_datetime
#For each node 
if frspec_pros.has_key?('node') then
  nodes = frspec_pros['node']
  i = 0;
    #STORES NODES POSITION   --takes one second 
    while i< nodes.length 
      node_position[i]= [nodes[i]['component_id'], nodes[i]['location'][0]['longitude'],  nodes[i]['location'][0]['latitude'],nodes[i]['position_3d'][0]['x'], nodes[i]['position_3d'][0]['y'], nodes[i]['position_3d'][0]['z']]
      i+=1
    end 
puts   Time.now.to_datetime
    all_nodes = test.function_nodes(nodes,'node', klasses_table , klasses_table2, channels, leases)
    #puts "NODES CREATED"
   ($properts['node'.capitalize]  ||= []) <<  "Class" 
end
puts   Time.now.to_datetime  
p "NODES CREATED"
#LINK EXISTS?? 
node_position.each {|a, n1|
  node_position.each {|b,n2|
    if not n1[0].eql?(n2[0])  and not  klasses_table['interface' + '-' + n1[0]].nil? and not  klasses_table['interface' + '-' + n2[0]].nil?
      rate = test.calculate_rate(n1,n2,node_position, test.protocol_bandwidth('IEEE802_11a') )
      if rate > test.min_protocol_rate('IEEE802_11a')
       #  puts "LINK EXISTS"
         name = ""
         name1 = ""
         i = 0 
         j = 0 
         while i < klasses_table['interface' + '-' + n1[0]].size
         j=0
         while j < klasses_table['interface' + '-' + n2[0]].size
         name = ""
         name1 = ""
         klasses_table['interface' + '-' + n1[0]][i].each { |k, v| 
           if k.eql?('component_id') 
              name = v  
              name = name.split(/[^a-zA-Z0-9_\-]/, -1).last
           end 
          }
         klasses_table['interface' + '-' + n2[0]][j].each { |k, v| 
            if k.eql?('component_id') 
              name1 = v
              name1 = name1.split(/[^a-zA-Z0-9_\-]/, -1).last  
            end 
            }
         link_name = ['link-', name, 'AND', name1].join
         #link_name = ['link_', klasses_table['interface' + '-' + n1[0]][i]['component_id'],'AND', klasses_table['interface' + '-' + n2[0]][j]['component_id']].join
         ii = {"rdf:resource"=>["#{test.ontology_name}#", link_name].join}
         klasses_table['interface' + '-' + n1[0]][i].compare_by_identity
         klasses_table['interface' + '-' + n1[0]][i].store("link",link_name)
         klasses_table['interface' + '-' + n2[0]][j].compare_by_identity
         klasses_table['interface' + '-' + n2[0]][j].store("isSink",ii)
         #   (klasses_table['interface' + '-' + n2[0]][j]["isSink"] ||= [] ) << ii
         j +=1
         end 
         i +=1
         end #while
       end 
    end 
   }
}

#For each link (not for wireless networks)   
if frspec_pros.has_key?('link') then
   nodes = frspec_pros['link']
  ($properts['link'.capitalize]  ||= []) <<  "Class" 
   all_nodes3 = test.function_nodes(nodes,'link', klasses_table, klasses_table2 )
end 


#================================================================================

puts   Time.now.to_datetime  
p "BEFORE INTERFACES"
all_nodes2= test.function_interface(klasses_table, "in", channels)
final_nodes = [all_nodes, all_nodes2].join(' ')
all_nodes2= test.function_interface(klasses_table, "out", channels)
final_nodes = [final_nodes, all_nodes2].join(' ')

puts   Time.now.to_datetime  
p "AFTER INTERFACES"
#FOR ALL CLASSES 
k = 0
while k < klasses_table.keys.length
  nodes = klasses_table[klasses_table.keys[k]]
  class_name =  klasses_table.keys[k].partition('-').first
  ($properts[class_name.capitalize]  ||= []) <<  "Class" 
   if not class_name.to_s.eql?('interface')
       all_nodes0 = test.function_nodes(nodes,class_name, klasses_table, klasses_table2, channels, leases )
       all_nodes4 = [all_nodes4, all_nodes0].join(' ')
   end 
   k+=1
end 

channel2 =  frspec_pros['spectrum'][0]['channel']
Novi::CL.each{|p, o|
if  p.to_s.split('#').last.eql?('Channel')
  nml = p.to_s.split('#').first
end 
}   

  channel2.each {|d|
    d['my_type'] = "#{nml}#Channel_#{d['channel_num']}"
}

all_nodes0 = test.function_nodes(channel2, 'channel', klasses_table, klasses_table2, channels, leases)
($properts['channel'.capitalize]  ||= []) <<  "Class" 
all_nodes4 = [all_nodes4, all_nodes0].join(' ')
  p "CHANNELS CREATED"  
puts   Time.now.to_datetime  
p "AFTER ALL CLASSES"
#final_nodes = [final_nodes, all_nodes3].join(' ')
final_nodes = [final_nodes, all_nodes4].join(' ')


#=======================================================================
#=======================================================================

#temp1 = Hash.new
#all_nodes = ""

#FOR INSERT INDIVIDUALS TO A PLATFORM OR TOPOLOGY
#temp2 = Hash.new
#temp2.compare_by_identity
#temp2 = { "rdf:about"=>"#{im}#FEDERICA"}
#contain = "contains"
#oout = XmlSimple.xml_out(temp1, 'RootName'=> "owl:NamedIndividual" , 'NoAttr'=> false , 'ContentKey' => 'name', 'AttrPrefix' => false )
#temp2["rdf:type"]= {"rdf:resource"=>"#{im}#Platform"}
#($properts['Platform'.capitalize]  ||= []) <<  "Class"
#$individuals.keys.each { |key|   (temp2["#{contain}"] ||= []) <<{"rdf:resource"=>"#{key}"}  }
#($properts["contains"]  ||= []) <<  "true"
#END FOR INSERT INDIVIDUALS TO A PLATFORM OR TOPOLOGY

#CREATES THE PROPERTIES/CLASSES 
$properts.keys.each { |key2|
  key= key2.partition(':').last
  if Novi::DATA_PROP.select{|p, o| p.to_s.split('#').last.eql?(key)}.any? or Novi::OBJ_PROP.select{|p, o| p.to_s.split('#').last.eql?(key)}.any? or Novi::CL.select{|p, o| p.to_s.split('#').last.eql?(key2)}.any?
    if  Novi::DATA_PROP.select{|p, o| p.to_s.split('#').last.eql?(key)}.any?
      type = "DatatypeProperty"
      nml = Novi::DATA_PROP.select{|p, o| p.to_s.split('#').last.eql?(key)}.keys[0].split('#').first
      full_property_name = [nml, key].join('#').to_s      
      rootname = ["owl",type].join(':').to_s
    elsif Novi::OBJ_PROP.select{|p, o| p.to_s.split('#').last.eql?(key)}.any?
      type = "ObjectProperty"
      nml = Novi::OBJ_PROP.select{|p, o| p.to_s.split('#').last.eql?(key)}.keys[0].split('#').first
      full_property_name = [nml, key].join('#').to_s
      rootname = ["owl",type].join(':').to_s
    elsif Novi::CL.select{|p, o| p.to_s.split('#').last.eql?(key2)}.any?
      type = "Class"
      nml = Novi::CL.select{|p, o| p.to_s.split('#').last.eql?(key2)}.keys[0].split('#').first
      full_property_name = [nml, key2].join('#').to_s
      rootname = ["owl",type].join(':').to_s
    end
    XmlSimple.xml_out({"rdf:about"=>full_property_name}, 'RootName'=> rootname , 'OutputFile' => f) 
  end
 } 
#END CREATES THE PROPERTIES/CLASSES 
puts   Time.now.to_datetime  
p "BEFORE STORE FILE"
#IMPORT TO FILE
f.write(final_nodes)
f.write(RspecHeaderFile::FOOTER )
f.close

puts "END"
puts   Time.now.to_datetime  
p "THE END"
rescue Exception=> e 
#raise "Missing arguments" if  ARGV.empty? or ARGV.size.eql?(1)
  puts e.message  
  puts e.backtrace.inspect 
p "Exception"
  # Exception handling code here.
end
