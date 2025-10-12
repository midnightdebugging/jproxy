# 0.项目特色
* 使用Java语言写的基于Netty的代理程序；
* 提供Socks5和HTTP proxy的接入访问；
* 'local server' 和 'remote server' 程序之间使用双向TLS认证；
* 集成 BouncyCastle 以及在第一次运行程序时会生成TLS认证证书；
* 使用SQLite作为存储，无需手工配置数据库；
* 有定制的名单可以以灵活的配置 'local server'如何响应, 以适应复杂的网络环境。

# 1.如何编译及运行？
你需要安装以下组件:
* 最新稳定版的 [OpenJDK 18](https://adoptium.net/)
* 最新稳定版的 [Apache Maven](https://maven.apache.org/)

执行以下命令进行编译:
```
git clone https://github.com/cauchynie/jproxy.git
cd jproxy
./01.build.sh
#启动'local server'
./02.start_local.sh
#启动'remote server'
./03.start_local.sh
```

* 注意: 请确保已经替换 ${version} 为你实际编译的版本.<br>

# 2.如何部署
第一步, 执行以下命令以执行 "local-server"程序.<br>
```shell
java -jar -Dlocal-server.link-out.address=${target-address} -Dlocal-server.remote-websocket-link-out.port=${target-port} local-server-${version}.jar
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
# 3.如何使用?
代理监听端口,可以在这个文件(`local-server/src/main/resources/application.properties`)配置或者在启动时指定JVM参数(`-Dxx=yy`)。默认的监听类型和端口如下<br>

| 代理方式   | 所监听的端口 | 偏移量                                         | 功能                                                 | 备注                                                            |
|--------|--------|---------------------------------------------|----------------------------------------------------|---------------------------------------------------------------|
| Socks5 | 20080  | `${local-server.socks.link-in.port}`+0      | 完全将流量接出到`remote-server`                            | 可通过配置`local-server.socks.link-in.port`指定监听端口                  |
| Socks5 | 20081  | `${local-server.socks.link-in.port}`+1      | 读取`NameList`表，根据名单规则判断流量是否接出到`remote-server`       | 数据库位置在`$HOME/.jproxy`中,参见`NameList`表                          |
| Socks5 | 20082  | `${local-server.socks.link-in.port}` +2     | 读取`name-list.txt`文件，根据名单规则判断流量是否接出到`remote-server` | 文件在`local-server/src/main/resources/name-list.txt`中           |
| Socks5 | 20083  | `${local-server.socks.link-in.port}`+3      | 读取` GFWList`表，根据名单规则判断流量是否接出到`remote-server`       | GFWList 项目详情，参见 [GFWList](https://github.com/gfwlist/gfwlist) |
| HTTP   | 21080  | `${local-server.http-proxy.link-in.port}`+0 | 完全将流量接出到`remote-server`                            | 可通过配置`local-server.http-proxy.link-in.port`指定监听端口             |
| HTTP   | 21081  | `${local-server.http-proxy.link-in.port}`+1 | 读取`NameList`表，根据名单规则判断流量是否接出到`remote-server`       | 数据库位置在`$HOME/.jproxy`中,参见`NameList`表                          |
| HTTP   | 21082  | `${local-server.http-proxy.link-in.port}`+2 | 读取`name-list.txt`文件，根据名单规则判断流量是否接出到`remote-server` | 文件在`local-server/src/main/resources/name-list.txt`中           |
| HTTP   | 21083  | `${local-server.http-proxy.link-in.port}`+3 | 读取` GFWList`表，根据名单规则判断流量是否接出到`remote-server`       | GFWList 项目详情，参见 [GFWList](https://github.com/gfwlist/gfwlist) |

# 4.如何配置 GFWList?
GFWList 项目详情，参见 [GFWList](https://github.com/gfwlist/gfwlist).<br>

当 'local-server' 程序启动时, 程序将读取这个文件 `"$HOME/.jproxy/${env}/gfw-list.txt` , 因此，我们需要把文件更新到这个路径.<br>
* 注意: env 变量配置在这个文件下 `src/main/resources/application.properties`. 当 "local-server" 程序启动时，程序将打印env的值在这个文件上 "/tmp/logs/local-server.log".<br>

使用如下命令更新GFWList文件。<br>


```shell
#以下${local_server_address}为实际部署'local-server'的地址，可以在任意一台有通讯权限的地址上执行
local_server_address=127.0.0.1
curl -v http://${local_server_address}:8081/cli --data-urlencode "gfw-list -d -R -l -v"
#观察返回，无报错即成功

```

或者可以以下命令手动更新

```shell
# Set up the env you are using
env=dev

curl https://gitlab.com/gfwlist/gfwlist/raw/master/gfwlist.txt > "$HOME/.jproxy/${env}/gfw-list.txt"

```
经过以上配置需要重启 'local-server' 以使配置生效。

# 5.如何配置 DBList
DBList为本项目推荐使用的名单方式。通过配置名单可以告诉'local-server'如何处理收到的请求。可以使用以下的命令查看帮助

```shell
#以下${local_server_address}为实际部署'local-server'的地址，可以在任意一台有通讯权限的地址上执行
local_server_address=127.0.0.1
curl -v http://${local_server_address}:8081/cli --data-urlencode "db-list -h"

#使用方法: db-list URL [-0 | -1 | -2 | -3]    [-e | -r | -s] [-h] [-l]  [-R] [-v] 
#非选项参数:
#  URL    要增加规则的URL

#选项:
# -0,--ban         收到请求后：禁止链接
# -1,--local       收到请求后：本地直接链出，不经过remote-server
# -2,--half        收到请求后：先发请求给remote-server进行域名解析，然后再确定使用什么方式进行链接
# -3,--full        收到请求后：全部流量经过remote-server
# -e,--equals      匹配方式：相等
# -h,--help        显示帮助信息
# -l,--list        列举
# -r,--regular     匹配方式：正则表达式匹配(默认)
# -R,--reload      重新加载
# -s,--subdomain   匹配方式：所有子域名
# -v,--verbose     详细输出模式

#示例:
#收到以example.com为结尾的全部流量，全部转发给remote-server处理
curl -v http://${local_server_address}:8081/cli --data-urlencode "db-list example.com$ -R"
#收到以example.com为结尾的全部流量，全部禁止链接
curl -v http://${local_server_address}:8081/cli --data-urlencode "db-list -0 example.com$ -R"
#收到以example.com为结尾的全部流量，先发请求给remote-server进行域名解析，然后再确定使用什么方式进行链接
curl -v http://${local_server_address}:8081/cli --data-urlencode "db-list -2 example.com$ -R"
#查看当前名单规则
curl -v http://${local_server_address}:8081/cli --data-urlencode "db-list -l"
#收到以example.com域名全部流量，先发请求给remote-server进行域名解析，然后再确定使用什么方式进行链接
curl -v http://${local_server_address}:8081/cli --data-urlencode "db-list -2 example.com -R"
```










