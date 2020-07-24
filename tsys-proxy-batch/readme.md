# Tsys Proxy Batch

Tsys proxy batch is responsible for handling settlement request from Tsys batch processer.

## Running it locally from Intellij
When you run the Application from Intellij use the 'dev' profile (add `-Dspring.profiles.active=dev` in the VM options)

## Running it locally from command line
```
./gradlew run
```

## Check that it is running
```
http://localhost:9088/swagger-ui.html
```

## Generating the Operating Manual

The operating manual is written using the asciidoc format and the source files can be found in the docs directory.
To generate the operating manual the following command is executed in the root of the project.

```
> ./gradlew asciidoc
```

The operating manual will be rendered in HTML and the output can be located in the build/operating-manual directory.

## Project Structure

### Testing
The microservice skeleton project contains a service module/directory and this is a standard Springboot web project with 
JUnit 5 and JUnit 4 configured.  To test the service the following command is used from the root project.

```
> ./gradlew test
``` 

After the tests have executed successfully a report can be viewed in the service/build/reports directory.

### Code Coverage
The skeleton is configured with JaCoCo code coverage.  The report can be generated through the following command.

```
> ./gradlew jacocoTestReport
```

A code coverage report can be viewed in the service/build/reports directory.

### Spring Cloud Sleuth

The skeleton is configured to use AWS X-Ray to allow distributed tracing using the Spring Cloud Sleuth library.

The skeleton and AWS X-Ray required the X-Ray daemon to allow tracing to be viewed inside the AWS console.  Within 
a deployed environment the daemon executes as one of several sidecars.  More information can be found witin the following 
resources
* http://cloud.spring.io/spring-cloud-static/spring-cloud-sleuth/2.0.0.RC2/single/spring-cloud-sleuth.html
* https://docs.aws.amazon.com/xray/latest/devguide/xray-daemon.html
* https://confluence.10x.mylti3gh7p4x.net/display/DEV2/Distributed+Tracing+using+AWS+X-Ray

