# 0.项目特色
* 使用Java语言写的基于Netty的代理程序；
* 提供Socks5和HTTP proxy的接入访问；
* 'local server' 和 'remote server' 程序之间使用双向TLS认证；
* 集成 BouncyCastle 以及在第一次运行程序时会生成TLS认证证书；
* 使用SQLite作为存储，无需手工配置数据库；
* 有定制的名单可以以灵活的配置 'local server'如何响应, 以适应复杂的网络环境。

# 1.如何编译？
你需要安装以下组件:
* 最新稳定版的 [OpenJDK 18](https://adoptium.net/)
* 最新稳定版的 [Apache Maven](https://maven.apache.org/)

执行以下命令进行编译:
```
git clone https://github.com/cauchynie/jproxy.git
cd jproxy
mvn install package
```

# 2.如何运行？
执行以下命令以运行 "local-server" 程序:<br>

```shell
java -jar  local-server/target/local-server-${version}.jar
```

* 注意: 请确保已经替换 ${version} 为你实际编译的版本.<br>

执行以下命令以运行 "remote-server"程序:<br>


```shell
java -jar  remote-server/target/local-server-${version}.jar
```

* 注意: 请确保已经替换 ${version} 为你实际编译的版本.<br>

# 3.如何部署
第一步, 执行以下命令以执行 "local-server"程序.<br>
```shell
java -jar -Dlocal-server.link-out.address=${target-address} -Dlocal-server.link-out.port=${target-port} local-server-${version}.jar
```

* 注意: 请替换 \${target-address} 为你将要部署 "remote-server" 的地址. 同时, 也要替换 ${target-port} 为你将要部署的程序的 "remote-server" 程序监听端口.<br>

* 注意: 请确保已经替换 ${version} 为你实际编译的版本.<br>

第二步, 在你成功运行 "local-server"程序之后, 在指定的文件会生成配置文件. 在 Windows 操作系统上, 我们可以使用以下命令打包这些配置文件.<br>

```cmd
rem 在 "local-server" 机上执行这个命令，从而打包这些配置文件
cd "%HOMEDRIVE%%HOMEPATH%\.jproxy"
tar -cvf /path/to/jproxy-config.tar .
```

* 注意: 请替换 "/path/to/jproxy-config.tar" 为你将要生成的文件路径.

当使用 unix-like 操作系统时, 我们可以使用以下命令打包这些配置文件.<br>

```shell
#  在 "local-server" 机上执行这个命令，从而打包这些配置文件
cd "$HOME/.jproxy"
tar -cvf /path/to/jproxy-config.tar .
```

* 注意: 请替换 "/path/to/jproxy-config.tar" 为你将要生成的文件路径.

第三步, 将 "/path/to/jproxy-config.tar" 文件拷贝到 "remote-server"机器上, 然后执行以下文件释放这些配置文件. 

```shell
mkdir "$HOME/.jproxy" && cd "$HOME/.jproxy" && tar -xvf /path/to/jproxy-config.tar
```

第四步, 执行一下命令从而运行 "remote-server" 程序:<br>

```shell
java -jar  remote-server/target/local-server-${version}.jar
```

# 4.如何配置 GFWList?
GFWList 项目详情，参见 [GFWList](https://github.com/gfwlist/gfwlist).<br>

当 'local-server' 程序启动时, 程序将读取这个文件 `"$HOME/.jproxy/${env}/gfw-list.txt` , 因此，我们需要把文件更新到这个路径.<br>
* 注意: env 变量配置在这个文件下 `src/main/resources/application.properties`. 当 "local-server" 程序启动时，程序将打印env的值在这个文件上 "/tmp/logs/local-server.log".<br>

使用如下命令更新GFWList文件。<br>

```shell
# Set up the env you are using
env=dev

curl https://gitlab.com/gfwlist/gfwlist/raw/master/gfwlist.txt > "$HOME/.jproxy/${env}/gfw-list.txt.0"

```








