# Ohm-o-matic
An open-source distributed and pervasive system for peer-to-peer control of electricity consumption in house.


## Implementation
### Communication
* gRPC: an open source remote procedure call (RPC) system
### P2P
* Chord: a protocol and algorithm for a peer-to-peer distributed hash table
### Web service
* Project Jersey: an open source framework for developing RESTful Web Services.
* Project Grizzly: a framework for build scalable and robust servers using NIO as well as offering extended framework components.
### Mutual exclusion
* Ricart-Agrawala Algorithm: an algorithm for mutual exclusion on a distributed system.
### Charting
* Knowm XChart: a light-weight and convenient library for plotting data.


## Minimum system requirements
1. Oracle Java 10
2. Apache Maven 3.6
3. Google Protocol Buffers 3.7


## Recommended IDE
IntelliJ IDEA Community 2019.1 (http://www.jetbrains.com/idea)


## Build the software
1. Run the Maven package command:

	```mvn package```



## Run the software
1. Run the REST Server:

	```java -cp target\Ohm-o-matic-jar-with-dependencies.jar OhmOMatic.REST.RestServer```

2. Run the Admin client:

	```java -cp target\Ohm-o-matic-jar-with-dependencies.jar OhmOMatic.Cli.CliAdmin -r http://localhost:8080/OOM/OOM```

3. Run the first Chord client:

	```java -cp target\Ohm-o-matic-jar-with-dependencies.jar OhmOMatic.Cli.CliCasa -r http://localhost:8080/OOM/OOM -i Naruto -k 127.0.0.1 -q 9001```

4. Run some Chord clients:

	```java -cp target\Ohm-o-matic-jar-with-dependencies.jar OhmOMatic.Cli.CliCasa -r http://localhost:8080/OOM/OOM -i Sasuke -k 127.0.0.1 -q 9002 -j 127.0.0.1 -p 9001```

5. Run some Chord clients:

	```java -cp target\Ohm-o-matic-jar-with-dependencies.jar OhmOMatic.Cli.CliCasa -r http://localhost:8080/OOM/OOM -i Sakura -k 127.0.0.1 -q 9003 -j 127.0.0.1 -p 9001```

6. Run some Chord clients:

	```java -cp target\Ohm-o-matic-jar-with-dependencies.jar OhmOMatic.Cli.CliCasa -r http://localhost:8080/OOM/OOM -i Kakashi -k 127.0.0.1 -q 9004 -j 127.0.0.1 -p 9001```


## License
Copyright 2019 (c) [MAIONE MIKY]. All rights reserved.

Licensed under the [MIT](LICENSE) License.