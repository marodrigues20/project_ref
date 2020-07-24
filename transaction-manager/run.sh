#!/bin/bash
java -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap \
     -Dserver.port=8099 \
     -Dredis.host=${REDIS_HOST} \
     -Dredis.port=${REDIS_PORT} \
     -Dhttp.proxyHost=${http_proxy%:*} \
     -Dhttp.proxyPort=${http_proxy#*:} \
     -jar /usr/local/bin/service.jar