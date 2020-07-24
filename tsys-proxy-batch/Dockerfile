FROM 786796192785.dkr.ecr.eu-west-1.amazonaws.com/10x/10x-openjdk-ssm:latest AS build
ADD . /src
WORKDIR /src
RUN ./gradlew --no-daemon tsys-proxy-batch-service:clean tsys-proxy-batch-service:bootJar

FROM ${AWS_ACCOUNT_NO:-786796192785}.dkr.ecr.eu-west-1.amazonaws.com/10x/10x-openjre-ssm:latest
EXPOSE 9088
HEALTHCHECK --retries=12 --interval=10s CMD curl -s localhost:9088/health || exit 1
COPY --from=build /src/tsys-proxy-batch-service/build/libs/tsys-proxy-batch-service-*.jar /usr/local/bin/tsys-proxy-batch-service.jar
COPY --from=build /src/run.sh /usr/local/bin/run.sh
RUN chmod +x /usr/local/bin/tsys-proxy-batch-service.jar
RUN chmod +x /usr/local/bin/run.sh
CMD ["chamber", "exec", "tsys-proxy-batch", "--", "/usr/local/bin/run.sh"]
