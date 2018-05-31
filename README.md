### How to run
```bash
java -cp "mockhttps-1.0-SNAPSHOT/lib/*" com.ebay.ads.https.HttpServer
sbt run
```

### Dependencies
- `org.dispatchhttp.dispatch-core`: Scala wrapper of `async-http-client`

### Requirements
- **Small memory usage:** sample data works with `-Xmx32M` option
- **Low cpu usage:** uses constant 30% CPU time on MacBook Pro 15 (2017)

### Known issues
- JVM on MacOS: open files limit: 10240.
    - Add `-XX:-MaxFDLimit` to `~/Library/Preferences/IntelliJIdea2018.1/idea.vmoptions`
    - Add `-XX:-MaxFDLimit` to VM option in Run/Debug Configurations 
- HTTP client hang when has more than 50k open connections
    - Fix: Set batch size no more than 10k

### Micro-benchmark
- pre-condition: no warm-up run
- sync batch:  **`time: 774.22 sec, 1434 op/s`**
- async batch: **`time: 71.44 sec, 15538 op/s`**

### TODO
- test edge cases
