DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "Checking RSpec..."
${DIR}/rspeclint data/example-advertisement-rspec.xml

input="data/example-advertisement-rspec.xml"
echo "Extracting RDF graph from ..."
rapper -e -i rdfa -o turtle ${input}

input="data/example-advertisement-rspec.ttl"
echo "Extracting RDF graph from ..."
rapper -e -i guess -o turtle ${input}

input="data/example-advertisement-rspec3.xml"
echo "Extracting RDF graph from ..."
rapper -e -i rdfxml -o turtle ${input}
