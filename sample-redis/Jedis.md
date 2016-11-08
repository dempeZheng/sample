##Jedis

### 关于pipeline
之前认为pipeline是一次把所有的请求打包发送到服务端，统一处理完，然后拉取所有的返回信息。
今天看到源代码 才发现实际上不是这么一回事。
pipeline 的实现依然是循环的调用sendCommand到服务端，并且将返回结果丢到返回队列里面，然后一次性从流中读取所有的返回结果。

 非pipleline模式：
 
```
   Request---->执行
    ---->Response
    Request---->执行
    ---->Response
```

Pipeline模式下：
  
```
  Request---->执行，Server将响应结果队列化
    Request---->执行，Server将响应结果队列化
    ---->Response
    ---->Response
```

**所以redis的pipeline不是一次将所有命令打包发送到服务端**，并没有减少服务端sendCommand的网络耗时，仅仅是减少服务返回结果的等待时间和循环拉取结果的网络耗时。
pipeline的使用场景是：对于大量返回结果没有相互依赖的command，通过pipeline对返回结果进行批量拉取，可以显著提升效率。

### 关于jedis同步阻塞IO

看源代码发现jedis的实现是同步阻塞的io，都说NIO的性能比较好，到底redis的client有没有必要用NIO呢，网上也有少许基于netty 实现的redis异步客户端，这种到底有没有必要呢？

要弄清楚这个问题，首先要搞清楚NIO实现的客户端相比如同步IO客户端有什么优点？

对于NIO客户端而言，NIO主要是节省了IO等待的时间，但是如果对于redis这种内存操作非常快的，IO等待的时间可以忽略不计，NIO想比如同步IO的优势不大。

### jedis非线程安全
jedis是非线程安全的，所以不要在多线程下使用同一个jedis实例。但是如果每次使用都创建一个jedis连接，肯定不行。（1.创建jedis的时候建立连接会耗时；2.大量的建立连接可能会耗掉服务器的可用端口，不仅会占用部分系统资源，还可能造成time_wait的问题）
jedis提供jedis pool，一切问题就迎刃而解了。不仅能保证每个线程都使用的不同的jedis实例，也能保证connection不会膨胀。

### redis协议
redis协议比较简单，详情请看
http://weizhifeng.net/redis-protocol.html

### jedis socket参数的一些疑问？

```
socket = new Socket();
	// ->@wjw_add
	socket.setReuseAddress(true);// 这个设置会复用time_wait的连接，对于redis客户端为什么需要服用这些端口，不是已经有连接池管理连接，连接数应该是有限的，为什么还需要复用？
	socket.setKeepAlive(true); // Will monitor the TCP connection is
				   // valid
	socket.setTcpNoDelay(true); // Socket buffer Whetherclosed, to
				    // ensure timely delivery of data
	socket.setSoLinger(true, 0); // Control calls close () method,
				     // the underlying socket is closed
				     // immediately
				     //这个是在连接close的时候，直接关闭，没发送完的数据不要发送，不明白为什么需要这个？
	// <-@wjw_add

	socket.connect(new InetSocketAddress(host, port), timeout);
	socket.setSoTimeout(timeout);
```

