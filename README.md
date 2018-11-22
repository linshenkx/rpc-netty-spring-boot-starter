# 基于Netty的简易RPC框架
一个基于Netty,Zookeeper和SpringBoot的RPC框架

作者CSDN主页：[鲸临于空](https://blog.csdn.net/alinyua)

### 特性:
* 基于Spring Boot 2的自动发现,添加Starter依赖即可快速集成，开箱即用
* 基于ZooKeeper的服务发现，支持ZooKeeper集群
* 基于Netty的底层通信

### 版本：2.0

### 版本说明：
1.0版本实现了原型功能，达到初步可用状态，但性能较差，仅作学习交流用

2.0版本针对性能做了优化，并添加一些新功能：

* 增加负载均衡路由策略引擎（含随机、轮询、哈希等及其带权形式）
* 增加序列化引擎（支持 JDK默认、Hessian、Json、Protostuff、Xml、Avro、ProtocolBuffer、Thrift等序列化方式）
* 服务提供者提供服务时可注解参数指定最大工作线程数来限流
* 服务消费者对服务地址列表进行缓存，并监听变化
* 传输协议修改，使用消息头+消息体的模式以支持新特性并避免粘包半包问题和留下扩展空间

后续将对核心进行改造，使其支持异步模型

### 设计:
![design](https://img-blog.csdnimg.cn/20181106001830876.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2FsaW55dWE=,size_16,color_FFFFFF,t_70)
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
                <version>2.0.0.RELEASE</version>
            </dependency>
    ```

    - 服务消费者：
    ```xml
        <dependency>
            <groupId>com.github.linshenkx</groupId>
            <artifactId>rpc-netty-client-spring-boot-starter</artifactId>
            <version>2.0.0.RELEASE</version>
        </dependency>
    ``` 
    
3. 在Spring配置文件里配置zookeeper地址列表和rpc端口,如下例，

    其中connectTimeOut和sessionTimeOut都有默认值，应根据网络环境配置修改
    
    服务提供者和服务消费者应使用相同 registryPath，一般无需配置，使用默认值即可（如果有多个不同系统则可修改，达到隔离目的）
    
    1. 服务提供者 application 文件
    
        ```yml
            zk:
              address: 127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183
              connectTimeOut: 10000
              sessionTimeOut: 10000
              registryPath: "/defaultRegistry"
            rpc:
              server:
                port: 9991
            ```
    2. 服务消费者 application 文件
    
        ```yml
            server:
              port: 9090
            zk:
              address: 127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183
              connectTimeOut: 20000
              sessionTimeOut: 10000
              registryPath: "/defaultRegistry"
            rpc:
              client:
                routeStrategy: Polling
                serializeType: ProtoStuff
            ```
4. 服务提供方使用@RpcService标注接口实现 :
   其中weight代表权重，默认为1。workerThreads代表工作线程数，可用于限流，默认为10.
    ```java
    @RpcService(value = HelloService.class,weight = 2,workerThreads = 3)
    public class HelloServiceImpl implements HelloService {
    
      @Override
      public String say(String name) {
        return "hello " + name;
      }
    
    }
    ```

5. 服务消费者使用@Autowired注入 RpcClient，执行rpcClient.create(Service.class)即可生成Service的代理类
    
    ```java
    @RestController
    public class HelloController {
        @Autowired
        private RpcClient rpcClient;
    
        @GetMapping("/hello")
        public String sayHello(@RequestParam(defaultValue = "lin") String name){
            HelloService helloService= rpcClient.create(HelloService.class);
            return helloService.say(name);
        }
    
    }
    ```

