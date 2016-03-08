# Accounts REST Service

Demonstrates a simple JSON based REST service with minimal dependencies. Things to make note of

   * Uses [SparkJava] (http://sparkjava.com/documentation.html) a framework with the most minimal dependencies after comparison with Spring, JAXWS etc.
   * Used [GSON] (https://github.com/google/gson) which is the minimal json to object framework
   * The test class AccountServiceTest proves that this test runs and the service works as expected
   * The datastore is backed by a simple in memory map/objects
   * A release is available in the [releases](https://github.com/kannanekanath/accounts-rest-sample/releases/tag/1.0) section which can be downloaded and run. The link to jar file is [here](https://github.com/kannanekanath/accounts-rest-sample/releases/download/1.0/account-service-executable.jar)
