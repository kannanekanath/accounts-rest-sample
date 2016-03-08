# Accounts REST Service

Demonstrates a simple JSON based REST service with minimal dependencies. Things to make note of

   * Uses [SparkJava] (http://sparkjava.com/documentation.html) a framework with the most minimal dependencies after comparison with Spring, JAXWS etc.
   * Used [GSON] (https://github.com/google/gson) which is the minimal json to object framework
   * The test class AccountServiceTest proves that this test runs and the service works as expected
   * The datastore is backed by a simple in memory map/objects
   * A release is available in the [releases](https://github.com/kannanekanath/accounts-rest-sample/releases/tag/1.0) section which can be downloaded and run. The link to jar file is [here](https://github.com/kannanekanath/accounts-rest-sample/releases/download/1.0/account-service-executable.jar)

# Running the executable

   * The following command `java -jar account-service-executable.jar` will start a server on port 4567. The java here is a Java8 executable and the jar file is the one downloaded as shown above
   * Visit `http://localhost:4567` this will tell you that you are in the root and need to visit resources. This is deliberate as there is no service discover/wadl requirement
   * Visit `http://localhost:4567/account/1` this will show you that there is a user1 with a balance of 45. There are three users configured in memory as shown in the `InMemoryAccountService` class. 
   * Transfer is implemented as a **POST** operation on the path `/transfer/from/1/to/2/amount/20`. You can use chrome extensions to test POST or curl or alternatively look at the supplied test and create a Java client using HttpClient from Apache commons.
