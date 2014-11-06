###########################################################
# Makefile for running the ontology playground examples   #
###########################################################

all: checker

clean:
	@echo "nothing to do for now"

checker:
	@bin="sparql" && type $$bin > /dev/null || (echo "ERROR: Please install '$$bin' first." && exit 1)

setup:
	@type brew && (brew install jena)

.PHONY: all clean

