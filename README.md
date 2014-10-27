Fuge
====

Fuge is a weekend project written while trying to dig deep into Java threads, synchronization and communication using ConcurrentLinkedQueue.

Fuges creates 1 job dispatcher thread, 1 job result aggregator, multiple job consumer threads. Fuge use 2 thread-safe queues `jobQueue` and `resultQueue` for sending job and their results back and forth. Here is how it all gets connected:

- `dispatcher` thread sends new job over `jobQueue`
  Multiple `consumer` threads listen for incoming jobs on `jobQueue`.
  A consumer accepts 1 job at a time and process it
- `resultQueue` over which `consumer` threads send job result.
  `aggregator` thread flushes incoming results over `resultQueue`.

Crawler
-------

Crawler is a web crawler writtern on to of Fuge. Crawler setup Fuge and seeds an input url. Input job (url) is received by one of the consumer who result back a Crawler result object containing information about crawled URL. Links found on the job url are feeded back into the Fuge system as input. This goes on endlessly crawling possibly the entire web.

Usage
-----

```
git clone git@github.com:abhinavsingh/fuge.git
cd fuge
mvn clean compile assembly:single
java -jar target/Crawler-jar-with-dependencies.jar http://abhinavsingh.com
```
