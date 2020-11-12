This is a messy POC based on Corda's Java template, found here: https://github.com/corda/cordapp-template-java

# Pre-Requisites

See https://docs.corda.net/getting-set-up.html.

# Usage

## Running tests inside IntelliJ
(I'm leaving this in verbatim, but this is a VS Code project now. On the other hand, nothing should prevent it from working in IntelliJ)	
We recommend editing your IntelliJ preferences so that you use the Gradle runner - this means that the quasar utils
plugin will make sure that some flags (like ``-javaagent`` - see below) are
set for you.

To switch to using the Gradle runner:

* Navigate to ``Build, Execution, Deployment -> Build Tools -> Gradle -> Runner`` (or search for `runner`)
  * Windows: this is in "Settings"
  * MacOS: this is in "Preferences"
* Set "Delegate IDE build/run actions to gradle" to true
* Set "Run test using:" to "Gradle Test Runner"

If you would prefer to use the built in IntelliJ JUnit test runner, you can run ``gradlew installQuasar`` which will
copy your quasar JAR file to the lib directory. You will then need to specify ``-javaagent:lib/quasar.jar``
and set the run directory to the project root directory for each test.

VS Code

Mainly same above, except you're running gradle from the command line.
* ./gradlew clean build
  * this will clean and build everything, obviously, except the React application.
* npm run build-to-client
  * Run this from within clients/src/main/web
  * It builds the React app and copies it to the resources folder of the SpringBood application.
  * Then if you call ../gradlew build from /clients a new clients-1.0.jar will be generated in build with the React application embedded.
* ./gradlew deployNodes
  * This will generated a build directory in which you'll find a nodes directory in which you'll find a script that will
    start your nodes locally.
  * This build task generates a node for every entry in build.gradle. It's here that you can specify the ports, etc.
  * These ports are relevant to the client, which can be started with
    * java -jar clients/build/libs/clients-0.1.jar --server.port=10050 --config.rpc.host=localhost --config.rpc.port=10013 --config.rpc.username=user1 --config.rpc.password=test
    * port 10050 is the port on which the client application is hosted. You can reach it in your browser at localhost:1050
    * localhost is the host. Running this locally it's unlikely you'll want to change that.
    * 10013 is the rpc port. This tells the client to which node it should connect. It corresponds to the node configuration mentioned above in build.gradle.

## Interacting with the nodes

### Shell

When started via the command line, each node will display an interactive shell:

    Welcome to the Corda interactive shell.
    Useful commands include 'help' to see what is available, and 'bye' to shut down the node.
    
    Tue Nov 06 11:58:13 GMT 2018>>>

You can use this shell to interact with your node. For example, enter `run networkMapSnapshot` to see a list of 
the other nodes on the network:

    Tue Nov 06 11:58:13 GMT 2018>>> run networkMapSnapshot
    [
      {
      "addresses" : [ "localhost:10002" ],
      "legalIdentitiesAndCerts" : [ "O=Notary, L=London, C=GB" ],
      "platformVersion" : 3,
      "serial" : 1541505484825
    },
      {
      "addresses" : [ "localhost:10005" ],
      "legalIdentitiesAndCerts" : [ "O=PartyA, L=London, C=GB" ],
      "platformVersion" : 3,
      "serial" : 1541505382560
    },
      {
      "addresses" : [ "localhost:10008" ],
      "legalIdentitiesAndCerts" : [ "O=PartyB, L=New York, C=US" ],
      "platformVersion" : 3,
      "serial" : 1541505384742
    }
    ]
    
    Tue Nov 06 12:30:11 GMT 2018>>> 

You can find out more about the node shell [here](https://docs.corda.net/shell.html).

### Client

`clients/src/main/java/com/template/Client.java` defines a simple command-line client that connects to a node via RPC 
and prints a list of the other nodes on the network.

clients/src/main/web is a React application.
* You can start it with npm run start and it will connect by default to port 10050. You'll need to have a client SpringBoot running on that port.
* Or you can start the SpringBoot application which will expose the client on whichever port for which it's configured, such as 10050. The advantage of npm run start, of course, is hot-deploys.

   java -jar clients/build/libs/clients-0.1.jar --server.port=10050 --config.rpc.host=localhost --config.rpc.port=10013 --config.rpc.username=user1 --config.rpc.password=test

#### Running the client

##### SpringBoot
See above, but the SpringBoot application encapsulates both the REST and React applications.


##### Via the command line

Run the `runTemplateClient` Gradle task. By default, it connects to the node with RPC address `localhost:10006` with 
the username `user1` and the password `test`.

##### Via IntelliJ

Run the `Run Template Client` run configuration. By default, it connects to the node with RPC address `localhost:10006` 
with the username `user1` and the password `test`.

### Webserver

`clients/src/main/java/com/template/webserver/` defines a simple Spring webserver that connects to a node via RPC and 
allows you to interact with the node over HTTP.

The API endpoints are defined here:

     clients/src/main/java/com/template/webserver/Controller.java

And a static webpage is defined here:

     clients/src/main/resources/static/

#### Running the webserver

##### Via the command line

Run the `runTemplateServer` Gradle task. By default, it connects to the node with RPC address `localhost:10006` with 
the username `user1` and the password `test`, and serves the webserver on port `localhost:10050`.

##### Via IntelliJ

Run the `Run Template Server` run configuration. By default, it connects to the node with RPC address `localhost:10006` 
with the username `user1` and the password `test`, and serves the webserver on port `localhost:10050`.

