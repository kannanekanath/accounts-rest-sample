# Account REST Service

Demonstrates a simple Account JSON based REST service with minimal dependencies. Things to make note of

   * Uses SparkJava a framework with the most minimal dependencies after comparison with Spring, JAXWS etc.
   * Used GSON which is the minimal json to object framework
   * The test class AccountServiceTest proves that this test runs and the service works as expected
   * The datastore is backed by a simple in memory map/objects
   * A release is available in the releases section which can be downloaded and run