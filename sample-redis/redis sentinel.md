## redis sentinel

Redis-Sentinel是Redis官方推荐的高可用性(HA)解决方案，当用Redis做Master-slave的高可用方案时，假如master宕机了，Redis本身(包括它的很多客户端)都没有实现自动进行主备切换，而Redis-sentinel本身也是一个独立运行的进程，它能监控多个master-slave集群，发现master宕机后能进行自懂切换。

### 主要功能点：

- 不时地监控redis是否按照预期良好地运行;
- 如果发现某个redis节点运行出现状况，能够通知另外一个进程(例如它的客户端);
- 能够进行自动切换。当一个master节点不可用时，能够选举出master的多个slave(如果有超过一个slave的话)中的一个来作为新的master,其它的slave节点会将它所追随的master的地址改为被提升为master的slave的新地址


### Sentinel机制与用法

详见：https://segmentfault.com/a/1190000002680804


### Sentinel Leader选举算法Raft

注意：Sentinel集群正常运行的时候每个节点epoch相同，当需要故障转移的时候会在集群中选出Leader执行故障转移操作。Sentinel采用了Raft协议实现了Sentinel间选举Leader的算法，不过也不完全跟论文描述的步骤一致。Sentinel集群运行过程中故障转移完成，所有Sentinel又会恢复平等。Leader仅仅是故障转移操作出现的角色。


### 生产环境推荐

对于一个最小集群，Redis 应该是一个 master 带上两个 slave，并且开启下列选项：

```
min-slaves-to-write 1
min-slaves-max-lag 10
```

这样能保证写入 master 的同时至少写入一个 slave，如果出现网络分区阻隔并发生 failover 的时候，可以保证写入的数据最终一致而不是丢失，写入老的 master 会直接失败，参考 Consistency under partitions。

Slave 可以适当设置优先级，除了 0 之外（0 表示永远不提升为 master），越小的优先级，越有可能被提示为 master。如果 slave 分布在多个机房，可以考虑将和 master 同一个机房的 slave 的优先级设置的更低以提升他被选为新的 master 的可能性。

考虑到可用性和选举的需要，Sentinel 进程至少为 3 个(**sentinel数量低于3，如果发生故障转移，需要选举一个sentinel作为leader来主导故障转移，鉴于Raft算法得票必须要大于n/2+1才能被选为leader，否则重复选举**)，推荐为 5 个，如果有网络分区，应当适当分布（比如 2 个在 A 机房， 2 个在 B 机房，一个在 C 机房）等。


延伸阅读：

http://weizijun.cn/2015/04/30/Raft%E5%8D%8F%E8%AE%AE%E5%AE%9E%E6%88%98%E4%B9%8BRedis%20Sentinel%E7%9A%84%E9%80%89%E4%B8%BELeader%E6%BA%90%E7%A0%81%E8%A7%A3%E6%9E%90/

http://www.solinx.co/archives/415

https://github.com/maemual/raft-zh_cn/blob/master/raft-zh_cn.md