#!/usr/bin/env bash
# @author: Alexander Willner <alexander.willner@tu-berlin.de>
# @requirements:
#   - sparql command line tool (e.g. 'brew install jena')
#


##########################################################################################
# Example data
##########################################################################################
example1=( "data:request-vm"
        "query:getType"
        "description:Get the type of the message"
        "command:example1" )
example2=( "data:advertisement-fp"
        "query:getnodes"
        "description:Get the nodes published in the FUSECO Playground example"
        "command:example2" )

commands=( example1 example2 )
##########################################################################################




##########################################################################################
# Work with associative arrays in Bash 3
##########################################################################################
function debug_command () {
    declare -a hash=("${!1}")
    for command in "${hash[@]}"; do
        key="${command%%:*}"
        value="${command#*:}"
        echo "Key: ${key}"
        echo "Value: ${value}"
    done
}

function show_command () {
    declare -a hash=("${!1}")
    for command in "${hash[@]}"; do
        key="${command%%:*}"
        value="${command#*:}"
        [ "$key" == "description" ] && description="$value"
        [ "$key" == "command" ] && command="$value"
    done
    echo " - '$command' ($description)"
}

function show_commands () {
    declare -a hash=("${!1}")
    for command in "${hash[@]}"; do
        show_command $command[@]
    done
}
##########################################################################################


##########################################################################################
# Helper methods
##########################################################################################
function checkBinary {
  if command -v $1 >/dev/null 2>&1; then
     return 0
   else
     echo >&2 " * Checking for '$1'...FAILED."
     return 1
   fi
}

function checkEnvironment {
  _error=0
  checkBinary sparql; _error=$(($_error + $?))
#  checkBinary rapper; _error=$(($_error + $?))
  if [ "0" != "$_error" ]; then
    echo >&2 "FAILED. Please install the above mentioned binaries."
    exit 1
  fi
}

function runQuery {
  data="data/example-$1.ttl"
  query="queries/query-$2.sparql"
  echo "Query:";
  echo "vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv"
  cat "${query}"
  echo "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"
  echo
  echo "Result:";
  echo "vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv"
  sparql --data="${data}" --query="${query}"
  echo "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"
}

function runCommand {
    for command in "${commands[@]}"; do
        if [ "$1" == $command ]; then
            foo=$command[@] 
            declare -a hash=("${!foo}")
            for command in "${hash[@]}"; do
                key="${command%%:*}"
                value="${command#*:}"
                [ "$key" == "description" ] && description="$value"
                [ "$key" == "command" ] && command="$value"
                [ "$key" == "data" ] && data="$value"
                [ "$key" == "query" ] && query="$value"
            done
            echo "Running '$command' ($description)..."
            runQuery $data $query
        fi
    done
}

##########################################################################################


##########################################################################################
# Main
##########################################################################################
checkEnvironment

[ "${#}" -eq 1 ] && runCommand $1 && exit 0
[ "${#}" -eq 2 ] && runQuery $1 $2 && exit 0

echo "Usage: $(basename $0) <command> OR <data> <query>"
echo "Predefined commands:"
show_commands commands[@]
echo "Arbitrary query examples:"
echo " - $(basename $0) advertisement-fp getcertificate (get certificate of testbed)"
echo " - $(basename $0) advertisement-fp getnodestatus (get resource status for FLS)"

exit 1;
##########################################################################################
