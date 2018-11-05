# 基于Netty的简易RPC框架
一个基于Netty,Zookeeper和SpringBoot的RPC框架

作者CSDN主页：[鲸临于空](https://blog.csdn.net/alinyua)

### 注意：
1.0版本实现了原型功能，达到初步可用状态，但性能较差，仅作学习交流用

后续将进行优化

### 特性:
* 基于Spring Boot 2的自动发现,添加Starter依赖即可快速集成，开箱即用
* 基于ZooKeeper的服务发现，支持ZooKeeper集群
* 基于Netty的底层通信

### 设计:
![design](https://images2015.cnblogs.com/blog/434101/201603/434101-20160316102651631-1816064105.png)
### 快速启动

以下例子可参考：[examples](https://github.com/linshenkx/rpc-netty-spring-boot-starter/tree/master/examples)

1. 定义公共接口:

		public interface HelloService { 
			String hello(String name); 
		}

2. 添加对应依赖(注意更新到最新版本)
    - 服务提供者：
    ```xml
            <dependency>
                <groupId>com.github.linshenkx</groupId>
                <artifactId>rpc-netty-server-spring-boot-starter</artifactId>
                <version>1.0.3.RELEASE</version>
            </dependency>
    ```

    - 服务消费者：
    ```xml
        <dependency>
            <groupId>com.github.linshenkx</groupId>
            <artifactId>rpc-netty-client-spring-boot-starter</artifactId>
            <version>1.0.3.RELEASE</version>
        </dependency>
    ``` 
    
3. 在Spring配置文件里配置zookeeper地址列表和rpc端口,如下例，

    其中connectTimeOut和sessionTimeOut都有默认值，应根据网络环境配置修改
    
    服务提供者和服务消费者应使用相同 registryPath，一般无需配置，使用默认值即可
    
    ```yml
    zk:
      addressList:
        - 127.0.0.1:2181
        - 127.0.0.1:2182
        - 127.0.0.1:2183
      connectTimeOut: 5000
      sessionTimeOut: 1000
      registryPath: "/defaultRegistry"
    rpc:
      server:
        port: 9991
    ```

4. 服务提供方使用@RpcService标注接口实现 :
    ```java
		@RpcService(HelloService.class)
		public class HelloServiceImpl implements HelloService {
			public HelloServiceImpl(){}
			
			@Override
			public String hello(String name) {
				return "Hello! " + name;
			}
		}
    ```

5. 服务消费者使用
    
    使用@Autowired注入 RpcClient，执行rpcClient.create(Service.class)即可生成Service的代理类
    
    ```java
           @Autowired
           private RpcClient rpcClient;
       
           @GetMapping("/hello")
           public String sayHello(@RequestParam(defaultValue = "lin") String name){
               HelloService helloService= rpcClient.create(HelloService.class);
               String returnString= helloService.say(name);
               return returnString;
           }
    ```

