FROM 786796192785.dkr.ecr.eu-west-1.amazonaws.com/10x/10x-openjdk-ssm:latest AS build
ADD . /src
WORKDIR /src
ARG SONARQUBE_SCANNER_PARAMS='{}'
ARG COMMIT_ID
ENV COMMIT_ID=$COMMIT_ID
RUN ./gradlew transaction-manager-service:clean transaction-manager-service:build transaction-manager-service:serviceSecurityChecks transaction-manager-service:bootRepackage --info transaction-manager-service:sonarqube

FROM ${AWS_ACCOUNT_NO:-786796192785}.dkr.ecr.eu-west-1.amazonaws.com/10x/10x-openjre-ssm:latest
EXPOSE 8099
HEALTHCHECK --retries=12 --interval=10s CMD curl -s localhost:8099/health || exit 1
COPY --from=build /src/transaction-manager-service/build/libs/transaction-manager-service-*.jar /usr/local/bin/service.jar
COPY --from=build /src/run.sh /usr/local/bin/run.sh
RUN chmod +x /usr/local/bin/service.jar
RUN chmod +x /usr/local/bin/run.sh
CMD ["chamber", "exec", "transaction-manager", "--", "/usr/local/bin/run.sh"]
