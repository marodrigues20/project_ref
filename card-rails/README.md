# Card Rails

This microservice is responsible for management of card authorisation payments.


## Running `card-rails-service` locally on a `dev` machine
* start application dependencies (Redis, Cockroach, Schema Registry, Kafka Cluster):
```
$ docker-compose up
```

* create a `cardrailsdb`
```
$ ./cockroach-init-dev.sh
```

* start the card-rails-service with `dev` profile active:
```
$ ./gradlew -Dspring.profiles.active=dev cards-rails-service:bootRun
```

* to shutdown application dependencies:
```
$ docker-compose down
```

* to inspect data saved in `cardrailsdb`:
```
$ docker exec -it cardrailsdb bash
$ ./cockroach sql --insecure --database cardrailsdb
```


## Logback configuration
By default logback is configured to encode logs as JSON. While this is a requirement to support SUMO
logic, it is not great for development. To disable logback JSON encoding set the active spring profile
to `dev` by adding the following JVM option: `-Dspring.profiles.active=dev`


## Cockroach cluster overview UI
After `docker-compose up` there is a portal where you can access [cockroach local cluster](http://localhost:8082).