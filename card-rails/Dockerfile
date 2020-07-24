FROM 786796192785.dkr.ecr.eu-west-1.amazonaws.com/10x/10x-openjdk-11-ssm:latest AS build
ADD . /src
WORKDIR /src
ARG COMMIT_ID
ENV COMMIT_ID=$COMMIT_ID
RUN ./gradlew --no-daemon card-rails-service:clean card-rails-service:bootJar card-rails-service:serviceSecurityChecks --info

FROM ${AWS_ACCOUNT_NO:-786796192785}.dkr.ecr.eu-west-1.amazonaws.com/10x/10x-openjre-11-ssm:latest
EXPOSE 8080
COPY --from=build /src/card-rails-service/build/libs/card-rails-service.jar /usr/local/bin/card-rails-service.jar
COPY --from=build /src/run.sh /usr/local/bin/run.sh
RUN chmod +r /usr/local/bin/card-rails-service.jar
RUN chmod +x /usr/local/bin/run.sh

CMD ["chamber", "exec", "card-rails", "--", "/usr/local/bin/run.sh"]

