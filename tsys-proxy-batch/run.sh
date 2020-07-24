#!/bin/bash

java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap \
     -Dhttp.proxyHost=${http_proxy%:*} \
     -Dhttp.proxyPort=${http_proxy#*:} \
     -jar /usr/local/bin/tsys-proxy-batch-service.jar