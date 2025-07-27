# 0.Project Features
* Netty based proxy program written in Java language
* Provide access to socks5 and HTTP proxy.
* Communication between the 'local server' and 'remote server' uses bidirectional TLS authentication.
* Integrate BouncyCastle and automatically generate TLS certificates on the first run.
* Use the embedded database SQLite without the need to manually configure table creation.
* Use a custom list to control how the local server responds, adapting to harsh network conditions.

# 1.How to build
You require the following to build jproxy:
* Latest stable [OpenJDK 18](https://adoptium.net/)
* Latest stable [Apache Maven](https://maven.apache.org/)

Execute the following command to build:
```
git clone https://github.com/cauchynie/jproxy.git
cd jproxy
mvn install package
```

# 2.How to run
Execute the following command to run "local-server":<br>

```shell
java -jar  local-server/target/local-server-${version}.jar
```

* Note: Please be sure to replace ${version} with the version you are building.<br>

Execute the following command to run "remote-server":<br>


```shell
java -jar  remote-server/target/local-server-${version}.jar
```

* Note: Please be sure to replace ${version} with the version you are building.<br>

# 3.How to deploy
step1, Use the following command to start the "local-server".<br>
```shell
java -jar -Dlocal-server.link-out.address=${target-address} -Dlocal-server.link-out.port=${target-port} local-server-${version}.jar
```

* Note: Please replace \${target-address} with the address where you will deploy the "remote-server". Also, replace ${target-port} with the listening port of the "remote-server" you will deploy.<br>

* Note: Please be sure to replace \${version} with the version you are building.<br>

step2, After you successfully run the "local-server", a configuration file will be generated in the specified directory. On Windows operating system, use the following command to package the configuration files.<br>

```cmd
rem Run the following command on "local-server" machine to package configure file
cd "%HOMEDRIVE%%HOMEPATH%\.jproxy"
tar -cvf /path/to/jproxy-config.tar .
```

* Note: Please replace "/path/to/jproxy-config.tar" with the actual path to the configuration file you want to package.

When you are using a unix-like operating system, you can use the following command to package the configuration files.<br>

```shell
# Run the following command on "local-server" machine to package configure file
cd "$HOME/.jproxy"
tar -cvf /path/to/jproxy-config.tar .
```

* Note: Please replace "/path/to/jproxy-config.tar" with the actual path to the configuration file you want to package.

step3, Copy the "/path/to/jproxy-config.tar" file to the "remote-server", and then execute the following command to extract the configuration files. 

```shell
mkdir "$HOME/.jproxy" && cd "$HOME/.jproxy" && tar -xvf /path/to/jproxy-config.tar
```

step4, Execute the following command to run "remote-server":<br>

```shell
java -jar  remote-server/target/local-server-${version}.jar
```

# 4.How to deploy GFWList?
GFWLis tProject see [GFWList](https://github.com/gfwlist/gfwlist).<br>

When the 'local-server' starts, it will read `"$HOME/.jproxy/${env}/gfw-list.txt`, so we need to update the configuration file to the following file.<br>
* Note: The env variable is configured in "src/main/resources/application.properties". When "local-server" starts, it will print to "/tmp/logs/local-server.log".<br>

Update your file using the following command。<br>

```shell
# Set up the env you are using
env=dev

curl https://gitlab.com/gfwlist/gfwlist/raw/master/gfwlist.txt > "$HOME/.jproxy/${env}/gfw-list.txt.0"

```








