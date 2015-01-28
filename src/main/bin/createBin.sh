#!/usr/bin/env bash
mvn clean compile assembly:single && \
  (echo '#!/usr/bin/env java -jar'; \
   cat target/omnlib-jar-with-dependencies.jar) > omnlib; \
  chmod +x omnlib

