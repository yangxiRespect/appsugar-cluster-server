## Appsugar Cluster

Appsugar Cluster 是一款RPC框架. 基于[Akka](http://akka.io) 实现.

## Build with gradle

```

	repositories {
		maven{url "http://nexus.ggxueche.cn/nexus/content/groups/public/"}
	}
	dependencies{
		compile ("org.appsugar.cluster:appsugar-cluster-service-binding-spring:2.0.2-SNAPSHOT")
	}
```

# DEMO

## 启动应用

```

	public static void main(String[] args) {
		Config config = ConfigFactory.load();
		Optional<Config> localConfig = loadConfig("./application.conf");
		if (localConfig.isPresent()) {
			config = localConfig.get().resolve().withFallback(config);
		}
		ServiceClusterSystem clusterSystem = new AkkaServiceClusterSystem("c", config);
		DistributionRPCSystem system = new DistributionRPCSystemImpl(clusterSystem);
	}

	public static Optional<Config> loadConfig(String file) {
		File f = new File(file);
		if (f.isFile()) {
			return Optional.of(ConfigFactory.parseFile(f));
		}
		return Optional.empty();
	}
```

### 注册一个服务(producer)

```

	system.serviceFor(new ServiceDescriptor(Arrays.asList(new HelloFacadeImpl())), HelloFacade.name);

```

### 查找并调用一个服务(Consumer)

```

	system.require(HelloFacade.class); //启动程序时,消费者需告知系统所依赖的服务
	HelloFacade facade = system.serviceOf(HelloFacade.class);
	facade.sayHello("hello").thenAccept(System.out::println); //调用服务方法
```

### Producer config (application.conf)

```

	akka {
	  actor{
	    provider = "akka.cluster.ClusterActorRefProvider"
	    serializers {
	      java = "org.appsugar.cluster.service.akka.serialization.FstSerialization"
	    }
	  }
	  remote {
	    log-remote-lifecycle-events = off
	    netty.tcp {
	      hostname = "127.0.0.1"
	      port = 2551
	    }
	  }
	  cluster {
	     seed-nodes = ["akka.tcp://c@127.0.0.1:2551"]
	  }
	}
```

### Consumer config (application.conf)

```
	
	change port 2551 to 0.
	netty.tcp {
	      hostname = "127.0.0.1"
	      port = 0 //random port
	}
```

## About conf

seed-nodes = ["akka.tcp://c@127.0.0.1:2551"] 正常情况下应该有一个以上的seed-nodes.注意不要重启seed-node.(seed-node 类似dubbo中的 zookeeper) 



## Annotations

```

	[ExecuteDefault] 服务启动后执行一次
	@ExecuteDefault
	public void init() {
		System.out.println("============================init============================ ");
	}
	[ExecuteOnClose] 服务关闭后执行一次
	@ExecuteOnClose
	public void close() {
		System.out.println("=========================service closed============================");
	}
	[ExecuteOnEvent] 集群事件触发执行
	@ExecuteOnEvent("play")
	public void event(String a) {
		System.out.println("============================event============================ " + a);
	}

	@ExecuteOnEvent("play")
	public void event1(Integer a) {
		System.out.println("============================event============================ " + a);
	}
	[ExecuteOnServiceReady] 服务准备好后执行
	@ExecuteOnServiceReady
	public void setHello(Hello hello, Status status) {
		System.out.println("============================service ready============================  " + status);
	}
	[ExecuteRepeat] 重复执行
	@ExecuteRepeat(3000)
	public void repeat(){
		//3秒执行一次
	}
```
