### How to run
```
java -cp "mockhttps-1.0-SNAPSHOT/lib/*" com.ebay.ads.https.HttpServer
sbt run
```

### Dependencies
- org.dispatchhttp.dispatch-core: Scala wrapper of async-http-client

### Requirements
- Small memory usage: working with -Xmx128M option
- low cpu usage: use 4 threads for HTTP client and 4 threads for Scala global thread pool

### Known issues
- Exception: Too many open files when there are 10240+ open connections
- Workaround by limit 10000 open connections

### TODO
- add test cases