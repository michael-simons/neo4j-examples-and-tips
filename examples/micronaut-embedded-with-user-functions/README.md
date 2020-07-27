## Micronaut + Neo4j embedded + Custom procedures

```
./mvnw clean mn:run 
curl -X POST localhost:8080/createSomeData
curl localhost:8080/callFunction
```