require 'rubygems'
require 'xmlsimple'
require 'Owl'
#require 'rdf'
#require 'yowl' 
#require 'rdf/raptor'
require 'rexml/document'
require 'rexml/streamlistener'
require 'xml/mapping'
include REXML
#require 'novi.rb'
require 'my_new_class_2.rb'
  NodesGraph = Hash.new
  LinkCapacity = Hash.new
  IsSinkNodes = Hash.new
  IsSourceNodes = Hash.new
  Links = Hash.new
  Nodes= Hash.new
  LinkHops =  1
#im = "http://fp7-novi.eu/im.owl"
#indl="http://www.science.uva.nl/research/sne/indl"
#nml="http://schemas.ogf.org/nml/2013/05/base" 
  Im = "indl_wireless"
  Indl = "indl"
  Nml = "nml"
 def safe_title(title)
 return title.gsub(/[ '-.<>\/]/, '')#.gsub(/\([^\)]*?\)/, '').sub(/^(\d+)/, "a_#{$1}").gsub(/^_+|_+$/, '').gsub(/_+/, '_')
end
def interfacewirelessproperties(interface,phnodeA, sql, type )
 # sql += '?' + phnodeA + ' rdf:type '+ Nml + ':Node.' +"\n"
  temp = safe_title(interface.keys[0].partition('-').last)
  if type.eql?("out")
    sql +=  '?'+ phnodeA + ' '+ Nml + ':hasOutboundPort '+  '?' + [phnodeA,temp].join + '.' +"\n"
    sql +=  '?' + [phnodeA,temp].join + ' '+ Im + ':hasWirelessFeatures ' + '?' + [phnodeA,temp, 'wireless'].join + '.' +"\n"
     # sql +=  '?' + [phnodeA,temp, 'wireless'].join + ' ' + Im + ':hasChannel ' + '?' + [phnodeA,temp, 'wireless'].join + '.' +"\n"
  elsif type.eql?("in") 
    sql +=  '?'+ phnodeA + ' '+ Nml + ':hasInboundPort ' +'?' + [phnodeA,temp].join + '.' +"\n"
    sql += '?' + [phnodeA,temp].join + ' '+ Im + ':hasWirelessFeatures ' + '?' + [phnodeA,temp,'wireless'].join + '.' +"\n"
  end 
  sql += '?' + [phnodeA,temp,'wireless'].join + ' rdf:type '+ Im + ':WirelessFeatures.' +"\n"
  time = 1  
cha = 1
  interface.values[0].each  {|l, x|
    cha = l
    sql +=  '?' + [phnodeA,temp, 'wireless'].join + ' ' + Im + ':hasChannel ' + '?' + [phnodeA,temp, 'wireless',cha].join + '.' +"\n"
    x.each {|x0, v|
      #REWRIITEEEEE
      
    if  x0.to_s.include?('ChannelNo')
      time = 1 
      sql +=  '?'+  [phnodeA,temp,'wireless', cha].join  + ' '+ Im + ':ChannelNo ' +  '?'+[phnodeA,temp, 'wireless',cha,"#{x0}"].join + '.' +"\n"
      sql += 'FILTER ('+ '?'+ [phnodeA,temp, 'wireless',cha,"#{x0}"].join  + ' = "' + "#{v}" + '"^^xsd:integer' +')'+ '.'+"\n"  
    end #if hasChannels 

    if  x0.to_s.include?('frequency')
      time = 1 
      sql +=  '?'+  [phnodeA,temp,'wireless',cha].join  + ' '+ Im + ':frequency ' +  '?'+[phnodeA,temp, 'wireless',cha,"#{x0}"].join + '.' +"\n"
      sql += 'FILTER ('+ '?'+ [phnodeA,temp, 'wireless',cha,"#{x0}"].join  + ' = "' + "#{v}" + '"^^xsd:string' +')'+ '.'+"\n"  
    end #if hasfrequency
    if  x0.to_s.include?('protocol')
      time = 1 
      sql +=  '?'+  [phnodeA,temp,'wireless',cha].join  + ' '+ Im + ':protocol ' +  '?'+[phnodeA,temp, 'wireless',cha,"#{x0}"].join + '.' +"\n"
      sql += 'FILTER ('+ '?'+ [phnodeA,temp, 'wireless',cha,"#{x0}"].join  + ' = "' + "#{v}" + '"^^xsd:string' +')'+ '.'+"\n"  
    end #if hasprotocol 

    # if  x0.to_s.start_with?('hasProtocol')
   #   if time == 1 
   #     sql += '{'
   #     time +=1
   #   else 
   #     sql += ' UNION {'
   #   end  
   #   sql +=  '?' + [phnodeA, temp].join  + ' '+ Im + ':hasProtocol ' +  '?'+[phnodeA, temp,  x0].join + '.' +"\n"
   #   sql +=  '?' +[phnodeA,temp, x0].join +  ' ?' + [phnodeA,"_list", x0].join + ' ?' +[phnodeA,temp,"_list", x0].join + '.'+"\n" 
   #   sql += 'FILTER ('+ '?' +[phnodeA,temp,"_list", x0].join + ' = "' + "#{v}" + '"^^xsd:string' +')'+ '.'+"\n"    
   #   sql += '}' 
   # end #if hasProtocol
     } #x.each
cha += 1    }  # Interface
return sql 
end   
def combinations(array)
  m = array.length
  temporal1 = Hash.new
  (1...2**m).map do | n | 
    temporal = Hash.new
    (0...m).select { | i | n[i] == 1 }.map { | i | array.keys[i]  
    z= temporal.select { |k,v| v == array.values[i] } 
      if  z.size == 0 
      temporal["#{array.keys[i]}"]=  array.values[i]
    end 
  }
  z= temporal1.select { |k,v| v == temporal } 
  if  z.size == 0 and temporal.size != 1 
    temporal1[n] = temporal
  end 
  end
 
  return  temporal1
end
def combinations_pairs(array)
  m = array.length
  temporal1 = Hash.new
  (1...2**m).map do | n | 
    temporal = Hash.new
    (0...m).select { | i | n[i] == 1 }.map { | i |  
      array.keys[i]  
      
    z= temporal.select { |k,v| v == array.keys[i] } 
      if  z.size == 0 
      temporal["#{array.keys[i]}"]=  array.keys[i]
    end 
  }
  #puts temporal
  z= temporal1.select { |k,v| v == temporal } 
  if  z.size == 0 and temporal.size == 2 
    temporal1[n] = temporal
  end 
  end
  return  temporal1
end
def connects(nodeA,portt, linkAB,nodeB,capacity,hop, sql) 

  #(Links[linkAB]||=[]) <<  "true"

  if hop > 1
    sql = connects(nodeA,portt, [linkAB,1].join, [nodeA,linkAB, hop].join , capacity, 1, sql) 
    sql = connects([nodeA,linkAB,hop].join,[portt,hop].join,[linkAB,hop].join, nodeB, capacity, hop-1, sql) 
  else 
   (Links[linkAB]||=[]) <<  "true"
    sql += '?' + nodeA + ' rdf:type '+ Nml + ':Node.' +"\n"
    sql +=  '?'+ nodeA + ' '+ Nml + ':hasOutboundPort ' + '?'+ [nodeA, portt].join+'.' +"\n"
    sql += '?'+ [nodeA,portt].join + ' '+ Nml + ':isSource ' +  '?'+linkAB +'.' +"\n"
    sql += '?'+ [nodeA,portt].join + ' rdf:type '+ Nml + ':Port.' +"\n"
    sql += '?' + nodeB + ' rdf:type '+ Nml + ':Node.' +"\n"
    sql +=  '?'+ nodeB + ' '+ Nml + ':hasInboundPort ' + '?'+ [linkAB,nodeB].join+'.' +"\n"
    sql += '?'+ [linkAB,nodeB].join + ' '+ Nml + ':isSink ' +  '?'+linkAB +'.' +"\n"
    sql += '?'+ [linkAB,nodeB].join + ' rdf:type '+ Nml + ':Port.' +"\n"
    #wired topology#  sql += '?'+linkAB + ' '+ Im + ':hasCapacity ' + '?'+ [linkAB,"cap",hop].join + '.'+"\n"
    #   sql += '?'+linkAB + ' indl:hasLabel ' + '?'+ [linkAB,"label",hop].join + '.'+"\n"    
    #   sql += '?' + [linkAB,"label", hop].join + ' indl:labelType "hasCapacity"^^xsd:string;indl:value "' + capacity + '"^^xsd:string.'+"\n"
    #wired topology#     sql += 'FILTER ('+ 'xsd:float(?' + [linkAB,"cap", hop].join + ') >=  xsd:float(' +"#{capacity}" + ')' +')'+ '.'+"\n"
    sql +=  'FILTER ('+ '?' + nodeA + ' != ' + '?' + nodeB + ')'+'.' +"\n" 
  end  
  return sql 
end 

def nodeproperties(nodeA,phnodeA, sql )
  sql += '?' + phnodeA + ' rdf:type '+ Nml + ':Node.' +"\n"
  time = 0
  last = -1
  port = {}
  sql += 'FILTER EXISTS {'
  Nodes[nodeA].each  {|x|

  if  x.keys.to_s.include?('LifetimeSSSSSSSSSSSSSSSSSSSSSSSs')
    time = 0
    temp = x.keys[0]
    sql +=  '?'+ phnodeA + ' '+ Nml + ':existsDuring ' +'?'+[phnodeA,x.keys[0].to_s].join  + '.' +"\n"
    sql +=  '?'+[phnodeA,x.keys[0].to_s].join + ' rdf:type '+ Nml + ':Lifetime' + '.' +"\n"
    x.values.each { |ll|
    sql1 =  '?'+[phnodeA,x.keys[0].to_s].join + ' '+ Nml + ':start' + ' ' +  '?'+[phnodeA,temp,'start'].join + '.' +"\n"
     sql1 += 'FILTER ('+ '?' + [phnodeA,temp,'start'].join + ' <= "' + "#{ll['start']}" + '"^^xsd:dateTime' +')'+ '.'+"\n"
    sql1 +=  '?'+[phnodeA,x.keys[0].to_s].join + ' '+ Nml + ':end' + ' ' +  '?'+[phnodeA,temp,'end'].join + '.' +"\n"
     
     sql1 += 'FILTER ('+ '('+'?'+[phnodeA,temp,'start'].join + ' >= "' + "#{ll['start2']}" + '"^^xsd:dateTime. ' + ' && ' +
      '?' + [phnodeA,temp,'end'].join + ' >= ' + '?'+[phnodeA,temp,'start'].join  + '+ "' +  "#{Time.at(ll['duration'].to_i)}" + '"^^xsd:dateTime. '+ ')' +
      ' || '+'('+'?'+[phnodeA,temp,'start'].join + ' < "' + "#{ll['start2']}" + '"^^xsd:dateTime. ' +  ' && ' +
       '?' + [phnodeA,temp,'end'].join + ' >= ' + '"' + "#{ll['start2']}" + '"^^xsd:dateTime'  + '+ "' +  "#{Time.at(ll['duration'].to_i)}" + '"^^xsd:dateTime. '+ ')'+ ')'+'.' +"\n"

    ll.each {|x0, v|

     sql +=  '?'+[phnodeA,x.keys[0].to_s].join + ' '+ Nml + ':'+x0 + ' ' +  '?'+[phnodeA,temp,x0].join + '.' +"\n"
     sql += 'FILTER ('+ '?' + [phnodeA,temp,x0].join + ' >= "' + "#{v}" + '"^^xsd:dateTime' +')'+ '.'+"\n"
    }}
    end #if lifetime
  if  x.keys[0].to_s.start_with?('Locatgion')
    time = 0
    sql +=  '?'+ phnodeA + ' '+ Nml + ':locatedAt/'+ Nml + ':'+"#{x.values[0].keys[0]}" + ' ' +  '?'+[phnodeA,x.keys,"#{x.values[0].keys[0]}"].join + '.' +"\n"
    sql += 'FILTER ('+ '?' + [phnodeA,x.keys,"#{x.values[0].keys[0]}"].join + ' = "' + "#{x.values[0].values[0]}" + '"^^xsd:float' +')'+ '.'+"\n"
    sql +=  '?'+ phnodeA + ' '+ Nml + ':locatedAt/'+ Nml + ':'+"#{x.values[0].keys[1]}" + ' ' +  '?'+[phnodeA,x.keys,"#{x.values[0].keys[1]}"].join + '.' +"\n"
    sql += 'FILTER ('+ '?' + [phnodeA,x.keys,"#{x.values[0].keys[1]}"].join + ' = "' + "#{x.values[0].values[1]}" + '"^^xsd:float' +')'+ '.'+"\n"
    sql +=  '?'+ phnodeA + ' '+ Nml + ':locatedAt/'+ Nml + ':'+"#{x.values[0].keys[2]}" + ' ' +  '?'+[phnodeA,x.keys,"#{x.values[0].keys[2]}"].join + '.' +"\n"
    sql += 'FILTER ('+ '?' + [phnodeA,x.keys,"#{x.values[0].keys[2]}"].join + ' = "' + "#{x.values[0].values[2]}" + '"^^xsd:float' +')'+ '.'+"\n"
  end #if location
  if  x.keys.to_s.include?('ProcessingComponent') ||  x.keys.to_s.include?('MemoryComponent') ||  x.keys.to_s.include?('StorageComponent') ||  x.keys[0].to_s.start_with?('Location')
    temp = x.keys[0]
    if x.keys.to_s.include?('Component') 
      sql +=  '?'+ phnodeA + ' '+ Indl + ':hasComponent ' +'?'+[phnodeA,x.keys[0].to_s].join  + '.' +"\n"
    elsif x.keys.to_s.include?('Location')
      sql +=  '?'+ phnodeA + ' '+ Nml + ':locatedAt ' +'?'+[phnodeA,x.keys[0].to_s].join  + '.' +"\n"
    elsif x.keys.to_s.include?('Lifetime')
      sql +=  '?'+ phnodeA + ' '+ Nml + ':existsDuring ' +'?'+[phnodeA,x.keys[0].to_s].join  + '.' +"\n"
    end  
    cas =  case x.keys[0].to_s.include?('Component') when true then Indl else Nml end
    sql +=  '?'+[phnodeA,x.keys[0].to_s].join + ' rdf:type '+ cas + ':' + x.keys[0].to_s+ '.' +"\n"
    x.values.each { |ll|
      ll.each {|x0, v|
       if x0.eql?('cpuarch') and not v.eql?("")
         sql +=  '?'+[phnodeA,x.keys[0].to_s].join + ' '+ cas + ':'+x0 + ' ' +  '?'+[phnodeA,temp,x0].join + '.' +"\n"
         sql += 'FILTER ('+ '?' + [phnodeA,temp,x0].join + ' >= "' + "#{v}" + '"^^xsd:string' +')'+ '.'+"\n"
     elsif not v.eql?("")
       sql +=  '?'+[phnodeA,x.keys[0].to_s].join + ' '+ cas + ':'+x0 + ' ' +  '?'+[phnodeA,temp,x0].join + '.' +"\n"
       sql += 'FILTER ('+ '?' + [phnodeA,temp,x0].join + ' >= "' + "#{v}" + '"^^xsd:float' +')'+ '.'+"\n"  
     end   
}
}
  end #if ProcessingComponent/MemoryComponent/StorageComponent

  
  if  x.keys[0].to_s.start_with?('Port')
    temp = x.keys[0] 
    sql = interfacewirelessproperties(x,phnodeA, sql , "out" )
    port[[phnodeA,safe_title(x.keys[0].partition('-').last)].join] = true
#  port2 = port #[phnodeA,x.keys[0]].join
#  if not port1.eql?(port)
#    if not port.eql?("")
#      sql += 'FILTER ('+ '?'+ port1 + ' != ' + '?' + port2 + ' ).'+"\n" 
#    end
#    port = port1
#  end 
  end #if Interface
  
  }#Nodes[nodeA].each  

b= combinations_pairs(port)
 sql = Restriction(sql,b)
sql += '}'
return sql 
end 

def createcomponent(nodeA, klass, value)
     xvalue =  nodeA[value]
     if value.eql?('hasComponent')        
               m = 0 
               while m <  xvalue.size
                 klass.each do |x|  
                 if x['rdf:about'].eql?(xvalue[m]['rdf:resource'])
                  if x['type'][0]['rdf:resource'].to_s.split('#')[1].eql?('StorageComponent')
                    (Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"] ||= []) << {"StorageComponent"=>{"size"=>"#{x['size'][0]['content']}"}}
                  end
                  if x['type'][0]['rdf:resource'].to_s.split('#')[1].eql?('ProcessingComponent')
                #  (Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"] ||= []) << {"ProcessingComponent"=>{"cpuspeed"=>"#{x['cpuspeed'][0]['content']}"}}
                #  (Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"] ||= []) << {"ProcessingComponent"=>{"cores"=>"#{x['cores'][0]['content']}"}}
                #  (Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"] ||= []) << {"ProcessingComponent"=>{"cpuarch"=>"#{x['cpuarch'][0]['content']}"}}
                    (Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"] ||= []) << {"ProcessingComponent"=>{"cpuspeed"=>"#{x['cpuspeed'][0]['content']}"}}
                    index = Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"].index { |h| h.keys[0].eql?("ProcessingComponent")}
                    Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"][index]['ProcessingComponent'].store("cores", "#{x['cores'][0]['content']}")
                    Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"][index]['ProcessingComponent'].store("cpuarch", "#{x['cpuarch'][0]['content']}") 
                  end
                  if x['type'][0]['rdf:resource'].to_s.split('#')[1].eql?('MemoryComponent')
                    (Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"] ||= []) << {"MemoryComponent"=>{"size"=>"#{x['size'][0]['content']}"}}
                  end                  
                 end
                 end #klass.each
               m +=1 
               end #while
      elsif value.eql?('existsDuring')
               m = 0 
               while m <  xvalue.size
                 klass.each do |x|  
                  if x['rdf:about'].eql?(xvalue[m]['rdf:resource'])
                    if x['type'][0]['rdf:resource'].to_s.split('#')[1].eql?('Lifetime')
                    #comments(Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"] ||= []) << {["Lifetime",m].join=>{"start"=>"#{x['start'][0]['content']}"}}
                    #comments(Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"] ||= []) << {["Lifetime",m].join=>{"end"=>"#{x['end'][0]['content']}"}}
                 (Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"] ||= []) << {["Lifetime",m].join=>{"start"=>"#{x['start'][0]['content']}"}}
                  index = Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"].index { |h| h.keys[0].eql?(["Lifetime",m].join)}
  #                start =  Time.parse(x['start'][0]['content']).to_i
 #                 dur = 3#x['timeFlexibility'][0]['content'].to_i
 #                 start2 = start+ dur
 #                 start2 = Time.at(start2) 
#                  start2 = Time.at(Time.parse(x['start'][0]['content']).to_i +x['timeFlexibility'][0]['content'].to_i).to_datetime
                  Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"][index][["Lifetime",m].join].store("end", "#{x['end'][0]['content']}")

Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"][index][["Lifetime",m].join].store("offset", case x['offset'].eql?(nil) when true then 0 else "#{x['offset'][0]['content']}" end)

  #       (Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"] ||= []) << {["Lifetime",m].join=>{"#{x['start'][0]['content']}"=>"#{x['end'][0]['content']}"}}
                    end                 
                  end
                 end #klass.each
                 m +=1 
               end #while
       elsif value.eql?('locatedAt')
               m = 0 
                 klass.each do |x|  
                  if x['rdf:about'].eql?(xvalue[m]['rdf:resource'])
                    if x['type'][0]['rdf:resource'].to_s.split('#')[1].eql?('Location')
                      (Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"] ||= []) << {"Location"=>{"long"=>"#{x['long'][0]['content']}"}}
                      index = Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"].index { |h| h.keys[0].eql?('Location')}
                  #    Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"][index]['Location'].store("long", "#{x['long'][0]['content']}")
                      Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"][index]['Location'].store("lat", "#{x['lat'][0]['content']}") 
                    end                 
                  end
                 end #klass.each
      elsif value.eql?('hasOutboundPort')# ||  value.eql?('hasInboundPort')
               m = 0 
               while m <  xvalue.size
                 klass.each do |x|  
                  if x['rdf:about'].eql?(xvalue[m]['rdf:resource'])
                    if x['type'][0]['rdf:resource'].to_s.split('#')[1].eql?('Port')
                       klass.each do |w|  
                           if w['rdf:about'].eql?(x['hasWirelessFeatures'][0]['rdf:resource'])
                              if w['type'][0]['rdf:resource'].to_s.split('#')[1].eql?('WirelessFeatures')
                 tt = 0
                 while tt< w['hasChannel'].size
                                klass.each do |w2|  
                                  if w2['rdf:about'].eql?(w['hasChannel'][tt]['rdf:resource'])
                                    if w2['type'][0][ 'rdf:resource'].to_s.split('#')[1].eql?('Channel')
                                if h = Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"].find { |h| h.keys.eql?(["Port","-","#{x['rdf:about'].split('#')[1]}","cha"].join)
                                  h.keys[0].to_s.eql?(["Port","-","#{x['rdf:about'].split('#')[1]}","cha"].join)
                                  }  
                                  index = Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"].index { |h| h.keys.eql?(["Port","-","#{x['rdf:about'].split('#')[1]}","cha"].join)
                                  h.keys[0].to_s.eql?(["Port","-","#{x['rdf:about'].split('#')[1]}","cha"].join) 
                                  }  
                                  Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"][index][["Port","-","#{x['rdf:about'].split('#')[1]}","cha"].join][tt]= {"ChannelNo"=>"#{w2['ChannelNo'][0]['content']}","frequency"=>"#{w2['frequency'][0]['content']}","protocol"=>"#{w2['protocol'][0]['content']}"}
                                else                        
                 (Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"] ||= []) << {["Port","-","#{x['rdf:about'].split('#')[1]}","cha"].join=>{tt=>{"ChannelNo"=>"#{w2['ChannelNo'][0]['content']}","frequency"=>"#{w2['frequency'][0]['content']}","protocol"=>"#{w2['protocol'][0]['content']}"}}}
                                  end  #h if 
end 
                              #  j = 0
                              #  while j < w['hasProtocol'][0]['Bag'][0]['li'].size
                              #        if h = Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"].find { |h| h.keys.eql?(["Port",m].join)
                              #        h.keys[0].to_s.eql?(["Port","-","#{x['rdf:about'].split('#')[1]}"].join)
                              #        }  
                              #          index = Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"].index { |h| h.keys.eql?(["Port",m].join)
                              #          h.keys[0].to_s.eql?(["Port",m].join)
                              #          }  
                              #          Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"][index][["Port",m].join].store(["hasProtocol",j].join, "#{w['hasProtocol'][0]['Bag'][0]['li'][j]}")
                              #        else 
                              #          (Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"] ||= []) << {["Port",m].join=>{["hasProtocol",j].join=>"#{w['hasProtocol'][0]['Bag'][0]['li'][j]}"}}
                              #        end    
                              #        j  += 1  
                              #  end #while
end 
end 
tt +=1
end


                              end    #WirelessFeatures             
                           end#hasWirelessFeatures
                       end #klass.each
              #      (Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"] ||= []) << {["Port",m].join=>"#{x['hasWirelessFeatures'][0]['content']}"}
                    end     #Interface            
                  end #x['rdf:about'].eql?(xvalue[m]['rdf:resource'])
                 end #klass.each
                 m +=1 
               end #while
         end #value.eql?
end 
def definenodeproperties (nodeA, klass)

           xvalue = ""
           nodeA.keys.each do |xvalue|
            (Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"] ||= [])
            (Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"] ||= []) << {"#{xvalue}"=>"#{nodeA[xvalue][0]['content']}"}         
            if xvalue.eql?('hasComponent') || xvalue.eql?('existsDuring') ||  xvalue.eql?('hasInboundPort') ||  xvalue.eql?('hasOutboundPort') ||  xvalue.eql?('locatedAt')
              createcomponent(nodeA,klass,xvalue)
            end
 end 
end 

def linksconnected(linkAB,linkBC,capacity,hop, sql, flag) 
  if hop > 1
    sql = linksconnected(linkAB,[linkAB,hop].join , capacity, 1, sql, 0) 
    sql = linksconnected([linkAB,hop].join, linkBC, capacity, hop-1, sql, 0)
    if flag == 1
      sql += '?'+linkBC + ' '+ Im + ':hasCapacity ' + '?'+ [linkBC,"cap",hop].join + '.'+"\n"
      sql += 'FILTER ('+ 'xsd:float(?' + [linkBC,"cap", hop].join + ') >=  xsd:float(' +"#{capacity}" + ')' +')'+ '.'+"\n"
    end 
 else 
  #  sql = ""
    if hop > 0 
      sql += '?'+linkAB + ' (^'+ Nml + ':isSink/^'+ Nml + ':hasInboundPort)/('+ Nml + ':hasOutboundPort/'+ Nml + ':isSource) ' +'?'+ linkBC  +'.' +"\n"
    end 
    sql +='?'+ ["nodeleft",linkAB].join + ' '+ Nml + ':hasOutboundPort/'+ Nml + ':isSource '+'?'+ linkAB+ '.'+"\n"
    sql +='?'+ ["noderight",linkBC].join + ' '+ Nml + ':hasInboundPort/'+ Nml + ':isSink '+'?'+ linkBC+ '.'+"\n"
    sql += 'FILTER (' + '?'+ ["nodeleft",linkAB].join + '!=' +'?'+ ["noderight",linkBC].join + ').' + "\n"
    sql += '?'+linkAB + ' '+ Im + ':hasCapacity ' + '?'+ [linkAB,"cap",hop].join + '.'+"\n"
    sql += 'FILTER ('+ 'xsd:float(?' + [linkAB,"cap", hop].join + ') >=  xsd:float(' +"#{capacity}" + ')' +')'+ '.'+"\n"
    if flag == 1
      sql += '?'+linkBC + ' '+ Im + ':hasCapacity ' + '?'+ [linkBC,"cap",hop].join + '.'+"\n"
      sql += 'FILTER ('+ 'xsd:float(?' + [linkBC,"cap", hop].join + ') >=  xsd:float(' +"#{capacity}" + ')' +')'+ '.'+"\n"
    end 
  end  
  return sql 
end  
def linksRestriction ( sql, array )
# proslinks = Hash.new
#sql = ""
array.each do |x,y|
  
  sql += 'FILTER NOT EXISTS {'
  
  y.each do |z, w| 
    sql += 'FILTER EXISTS {'+ '?' + z.to_s + ' '+ Im + ':hasCapacity ' +'?'+ [z.to_s,"cap",1].join + '}.'+"\n"
  end 
    sql +=  'FILTER ('

  sumcapacity = 0 
  prev = y.keys[0]
  y.each do |z, w| 
    if !prev.eql?(z) 
    sql += '?' + prev+ ' = '+ '?' + z + ' && ' 
    end
    sumcapacity  +=  LinkCapacity[w]["content"].to_f
    prev = z  
  end 
  sql += 'xsd:float('+'?'+ [y.keys[0],"cap",1].join + ') <  xsd:float(' + "#{sumcapacity}" +'))'+"\n" 

  sql += '}.'+"\n"
 end
    
 return sql 
end

def Restriction ( sql, array )
# proslinks = Hash.new
#sql = ""
array.each do |x,y|
  
    sql +=  'FILTER ('
 
  prev = y.keys[0]
  y.each do |z, w| 
    if !prev.eql?(z) 
    sql += '?' + prev+ ' != '+ '?' + z  
    end
     prev = z  
  end 
  sql += ').'+"\n" 

 end
    
 return sql 
end


class Query
  im = "http://fp7-novi.eu/im.owl"
  LinkHops = 1
  #model = XmlSimple.xml_in("request_wireless_2.owl",  { 'KeyAttr' => 'name', 'ForceArray' => true})
  model = XmlSimple.xml_in("pros_v0.owl",  { 'KeyAttr' => 'name', 'ForceArray' => true})
  klass = model['NamedIndividual']#.to_hash
  klass.each do |x|  
      y = x['type']#['rdf:resource']
      y.each do |y|
        if y['rdf:resource'].to_s.split('#')[1].eql?("Node")
           (NodesGraph["#{x['rdf:about'].to_s.split('#')[1]}"] ||= [])# <<""
           definenodeproperties(x, klass)
        end 
      end
      y = x['hasOutboundPort']#['rdf:resource']
      if not y.eql?(nil)
        y.each do |y|
           klass.each do |z|
              if  z['rdf:about'].eql?(y['rdf:resource']) 
                  link = z['isSource']
                  if not link.eql?(nil)
                    link.each do |link| 
                      #--Commented   NodesGraph["#{x['rdf:about'].to_s.split('#')[1]}"] = "#{link['rdf:resource'].to_s.split('#')[1]}" 
                (IsSourceNodes["#{x['rdf:about'].to_s.split('#')[1]}"] ||=[]) << {"#{y.values[0].split('#')[1]}"=>"#{link['rdf:resource'].to_s.split('#')[1]}"} 
                ( NodesGraph["#{x['rdf:about'].to_s.split('#')[1]}"] ||= []) <<  link['rdf:resource'].to_s.split('#')[1] 
                      klass.each do |d|
                          if not d['rdf:about'][link['rdf:resource']].eql?(nil)
                              linkcap = d['hasCapacity']    
                              if not linkcap.eql?(nil)                            
                              linkcap.each do |linkcap| 
                                  #--commented    LinkCapacity["#{d['rdf:about'].to_s.split('#')[1]}"]  = linkcap
                              end
                              end
                          end 
                       end  #klass
                  end #link.each
             end 
              end #if 
           end 
        end #y
      end    #if not y.eql?(nil)
      y = x['hasInboundPort']#['rdf:resource']
      if not y.eql?(nil)
         y.each do |y|
             klass.each do |z|
                if  z['rdf:about'].eql?(y['rdf:resource'])
                   link = z['isSink']       
                  if not link.eql?(nil)
                   link.each do |link| 
                     (IsSinkNodes["#{x['rdf:about'].to_s.split('#')[1]}"] ||=[]) << {"#{y.values[0].split('#')[1]}"=>"#{link['rdf:resource'].to_s.split('#')[1]}"}                      
                         klass.each do |d|
                            if not d['rdf:about'][link['rdf:resource']].eql?(nil)
                                linkcap = d['hasCapacity']  
                                if not linkcap.eql?(nil)                                
                                linkcap.each do |linkcap| 
                                     LinkCapacity["#{d['rdf:about'].to_s.split('#')[1]}"]  = linkcap
                                end
                                end
                            end 
                         end                   
                   end #link.each
                end
                end #if
             end #klass.each
         end    #y
      end # if not y.eql?(nil)
   end #klass.each
end #class Query

#b= combinations_pairs(Nodes)
#   sql = Restriction(sql,b)
#puts sql 
# puts ghf 
   #puts NodesGraph
  # puts LinkCapacity
 #  puts IsSinkNodes
   sql = String.new
   sql = ""
 #    Novi::IND.keys.each { |uri| puts "#{uri.to_s.split('#')[1]}"}# + #{classes.values}"

  NodesGraph.keys.each do |c|
    #  sql += '?' + c + ' rdf:type '+ Indl + ':VirtualNode.' +"\n"
    #  sql += '?' + c + ' ^'+ Indl + ':implements '+ '?'+ ["ph",c].join +  '.' +"\n"
    sql = nodeproperties(safe_title(c),safe_title(["ph",c].join) , sql)
      NodesGraph[c].each do |l|
      j = 1  
    sql += ' FILTER EXISTS { '
      while j<=LinkHops 
        sql += '{ ' 
       # puts l
        a = ""
        portt = ""
        IsSinkNodes.each { |h, v| 
          v.each { |n|
         #if not n.index(l).eql?(nil)
         if n.values[0].eql?(l)
            a = h
            portt = n.keys[0]
         end 
        }
        }
         IsSourceNodes[c].each { |v|
if v.values[0].eql?(l)
    portt = v.keys[0]
end 
}
         #                index = Nodes["#{nodeA['rdf:about'].to_s.split('#')[1]}"].index { |h| h.keys[0].eql?("ProcessingComponent")}
      
        sql =   connects(safe_title(["ph",c].join), safe_title(portt), safe_title([l,j].join),safe_title(["ph",a].join),case LinkCapacity[l].eql?(nil) when true then -1 else LinkCapacity[l]['content'] end , j, sql)
        
        #sql =   connects(["ph",c].join,[l,j].join,["ph",IsSinkNodes.index([l])].join, LinkCapacity[l]['content'], j, sql)
        
        Links.each{|i,k| 
          if k[0].eql?( 'true') then 
            Links[i]= l 
          end
        } # Links.each
        sql += '} ' +"\n"
        if j < LinkHops 
          sql +=' UNION '+"\n"
        end 
        j+=1
        #sql =  linksconnected("AB","BC", 5, 1, sql)
      end #while
      sql += '} ' +"\n"
    end #NodesGraph[c].each
  end #NodesGraph.keys.each
  
 
  #sql =  linksconnected("AB","BC", 5, 3, sql, 1)
    #   sql =   connects(Pnode,"AB",Pnode2, 5, 2, sql)

    #    sql =   connects(Pnode,"AC",Pnode3, 6, 2, sql)  

    #  Links["AC1"]= "AD"

  #  puts Links
# b = combinations(Links)
#    sql = linksRestriction(sql,b)
puts 'start'
      #puts sql
#ALL NODES ARE DIFFERENT EACH OTHER
node = {}
 NodesGraph.keys.each {|nodef| 
   node[safe_title(["ph",nodef].join)] = true
#     node2 = node #[phnodeA,x.keys[0]].join
#     if not node1.eql?(node)
#       if not node.eql?("")
#         sql += 'FILTER ('+ '?'+ node1 + ' != ' + '?' + node2 + ' ).'+"\n" 
#       end
#       node = node1
#     end 
}

 # b= combinations_pairs(node)

 #  sql = Restriction(sql,b)
 
node.each {|a, n1|
  node.each {|b,n2|
    
    if not a.eql?(b) and a.<(b)
      sql +=  'FILTER ('
      sql += '?' + a+ ' != '+ '?' + b  
      sql += ').'+"\n" 

    end 
    
   }
} 
puts "NO"
# test = 'SELECT  REDUCED '
#NodesGraph.keys.each {|nodef| 
#   node1 = safe_title(["ph",nodef].join)
#    
#         test += '?'+ node1 + ' ' 
#   
#}
test = 'SELECT * '
test += ' where { '
 sql = [test, sql , '}'].join
sql = [ sql , '  LIMIT 1'].join
 #puts sql
puts MYNEWCLASS(sql)
puts 'END'
#MYNEWCLASS::result 
#puts Nodes

