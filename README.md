# Dependencies
* JavaFX - OpenJFX 11.0.2
* Netty - Netty 4.1.60.Final
* Apache Commons IO - Apache Commons IO 2.8.0

# Getting Started
Compiling and building the project is made seamless by using [Maven](https://maven.apache.org/) which will handle all the project's dependencies. 

On Ubuntu/Debian, running `sudo apt-get install maven` should be sufficient. For other operating systems, please refer to Maven's homepage. 

Make sure to `cd` into the directory `elansa-pong` which holds the source code. All Maven commands should be executed inside that directory. 
```bash
mvn compile exec:java -Dexec.mainClass="Main"
```

will download all dependencies, compile, and execute the program without any program arguments. This will allow you to play the game locally.

# Exporting Maps
The game comes prepackaged with maps. Running the command below will export the prepackaged maps to `.map` files in your current directory.
```bash
mvn compile exec:java -Dexec.mainClass="Main" -Dexec.args="export"
```

# Hosting A Server
The command below will host a server at `server_ip` and bind to the ports `tcp_port` and `udp_port`. The server will host a game using the `.map` file (which can be generated using the above instructions) at `map_file`. Make sure to not include the angle brackets (`< >`).
```bash
mvn compile exec:java -Dexec.mainClass="Main" -Dexec.args="host <server_ip> <tcp_port> <udp_port> <map_file>"
```