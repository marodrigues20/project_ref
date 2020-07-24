docker exec -it cardrailsdb ./cockroach sql --insecure \
 --execute="CREATE DATABASE IF NOT EXISTS cardrailsdb;
            CREATE USER IF NOT EXISTS root;
            GRANT ALL ON DATABASE cardrailsdb TO root;"
