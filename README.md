# RabbitMQ Clustering 과 HA ( High Available ) Queue 구성
_sungkyu.eo@samsung.com_ 

[TOC]

## RabbitMQ
RabbitMQ는 표준 AMQP (Advanced Message Queueing Protocol) 메세지 브로커 소프트웨어(message broker software) 오픈소스이다

### AMQP protocol
#### AMQP Protocol entity
	• Message - the actual data sent from a producer.
	• Exchange - the thing in the AMQP broker which receives the messages and decides where to route them on to. Perhaps best to think of this as a 'sorting office' for messages.
	• Queue - messages are placed into queues by the exchange. A consumer listens to a queue.
	• Channel - best way I can think of to describe this is an open connection to the broker. Not sure it matters too much to be honest, but I'm still learning at this point.

#### AMQP란?
- Advanced Message Queing Protocol의 약자로, 흔히 알고 있는 MQ의 오픈소스에 기반한 표준 프로토콜을 의미한다. AMQP 자체는 프로토콜을 의미하기 때문에 이 프로토콜에 따른 실제 MQ 제품들은 여러가지가 존재할 수 있으나 최근 가장 많이 사용되는것은 아무래도 Erlang( https://mirror.enha.kr/wiki/Erlang )과 자바로 작성된 RabbitMQ라고 할 수 있다.

#### AMQP 등장배경
- 이전 MQ 제품들은 많았으나, 대부분 플랫폼에 종속적인 제품들이었다.
- 그렇기 때문에 메세지 교환에 있어, 플랫폼을 통일하거나, 변환 작업이 필요해 성능 저하가 발생했다.
- 이런 기존 MQ 솔루션의 약점을 보완하기 위해 나온 것이 AMQP 이다.
- 즉, 서로 다른 시스템간에서 효율적으로 메세지를 교환하기 위한 것이 AMQP 이다.

#### Why AMQP, RabbitMQ ?
1. Interoperability - like TCP , unlike JMS
2. 여러 제품군과 open license, 무료 - 여러 제품군이 있기 때문에 비교하여 사용 가능
3. 효율적 - 모든 major language 지원, 대부분의 OS 지원
4. 이미 많은 회사에서 사용 중

#### Messaging senario
> todo...

## rabbitmq version
rabbit-mq-3.4.2 ( 포스팅 작성 기준 latest version )
== rabbit-mq-3.3.4 도 테스트 동일 ==

## 기본 command
`rabbitmqctrl -n [노드명] [command]`

## Clustering
node1, node2, node3, node4

위 4개의 노드 생성하고, clustering 구성을 진행해 본다.

```
start_node.bat \[노드명\] \[포트\] \[모니터링포트\]
```

```code
start_node.bat node1 5672 15672

start_node.bat node2 5673 15673

start_node.bat node3 5674 15674

start_node.bat node4 5675 15675
```

node 를 구동한 후 아래 url 을 통해 모니터링 페이지에 접근할 수 있다.

http://localhost:15672
http://localhost:15673
http://localhost:15674
http://localhost:15675

#### 모니터링 페이지
![monitor](https://github.com/oskmkr/rabbitmq-cluster-ha/blob/master/rabbitmq_monitor.jpg)

single machine 에서 여러개를 띄우기 위한 예제 bat ( windows )

**start_node.bat**
```code
@echo off
REM  The contents of this file are subject to the Mozilla Public License
set RABBITMQ_NODE_PORT=%2
set RABBITMQ_NODENAME=%1
set RABBITMQ_MNESIA_DIR=mnesia-%1
set RABBITMQ_LOG_BASE=log-%1
set RABBITMQ_SERVER_START_ARGS=-rabbitmq_management listener [{port,%3}]
rabbitmq-server.bat -detached
```

```code
rabbitmqctrl -n node1 stop_app // 노드 중단
Stopping node 'node1@XXXXXX' ...

rabbitmqctrl -n node1 start_app // 노드 시작
Starting node 'node1@XXXXXX' ...
```
#### clustering 설정

클러스터링을 구성하면 각 노드들의 데이터, 상태가 모두 복제가 된다.
> All data/state required for the operation of a RabbitMQ broker is replicated across all nodes, for reliability and scaling, with full ACID properties
rabbitMQ broker 가 동작하기 위한 데이터, 상태가 모두 복제된다. 이를 통해 신뢰성, 독립적 확장성이 가능해진다.
> An exception to this are message queues, which by default reside on the node that created them, though they are visible and reachable from all nodes.
복제 항목 중 Message Queue 는 제외된다. 각 node 에서 해당 queue 에 모두 접근할 수는 있지만, queue 는 기본적으로 최초에 생성된 node 에 의존된다. 따라서, 해당 node 가 상태 이상이 발생했을 경우 해당 queue 는 더 이상 동작이 불가능하다. 이를 위해서는 High Available Queue 설정을 통해 [mirrored queue](#mirrored-queue) 설정이 필요하다.

###### mirrored queue ( mirrored queue 와 replication 의 차이 )
replication 의 경우 cluster 구성 요소가 추가되었을 때 추가되기 전의 상태와 동일하게 맞추기 위한 복제 작업을 진행하지만,
mirrored queue 의 경우 ==cluster 구성 요소가 추가된 시점 이후의 데이터만 동일하게 유지==한다.

각 node 들은 각자 서로 통신 허용 여부를 cookie 를 이용 해 결정한다. 여기에서 cookie 는 문자,숫자로 이루어 진 문자열을 의미한다.
따라서 clustering 을 구성할 때 이 값을 동일하게 설정해야 한다.

```language
/var/lib/rabbitmq/.erlang.cookie
or
$HOME/.erlang.cookie
```

```language
C:\Users\Current User\.erlang.cookie (%HOMEDRIVE% + %HOMEPATH%\.erlang.cookie)
or 
C:\Documents and Settings\Current User\.erlang.cookie, and C:\Windows\.erlang.cookie
```

clustering 설정 방법에는 총 3가지가 지원된다.
1. CLI 를 통한 동적 구성
2. rabbitMQ configuration
3. rabbitMQ monitoring admin page 를 통한 동적 구성

```code
rabbitmqctrl -n node2 stop_app // 노드 중단
Stopping node 'node3@XXXXXX' ...
rabbitmqctrl -n node2 join_cluster node1@XXXXXXX // node2 를 node1 의 cluster 구성원으로 추가
Clustering node 'node3@XXXXXX' with 'node2@XXXXXX' ...
rabbitmqctrl -n node2 start_app // 노드 시작
// node3 과 node4 도 위 과정과 동일하게 추가한다.
```

#### clustering via the RabbitMQ configuration file
...soon...

1. before clustering
![monitor](https://github.com/oskmkr/rabbitmq-cluster-ha/blob/master/rabbitmq_clustering_before.jpg)

2. after clustering
![monitor](https://github.com/oskmkr/rabbitmq-cluster-ha/blob/master/rabbitmq_clustering_after.jpg)

## HA (High Availability)

RabbitMQ 2.x 대에서는 ha 구성 관련하여, application level에서 아래의 설정을 통해 구성 하였으나,

* deprecated configuration
```xml
<rabbit:queue-arguments>
        <entry key="x-ha-policy" value="all"></entry>
</rabbit:queue-arguments>
```

* latest configuration
* @see http://www.rabbitmq.com/ha.html
| **rabbitmqctl **|
|-|
|rabbitmqctl set_policy ha-all "^ha\." '{"ha-mode":"all"}'|
|**rabbitmqctl (Windows)**|
|rabbitmqctl set_policy ha-all "^ha\." "{""ha-mode"":""all""}"|
|** HTTP API ** |
|PUT /api/policies/%2f/ha-all {"pattern":"^ha\.", "definition":{"ha-mode":"all"}}|
|** Web UI**	|
|Navigate to Admin > Policies > Add / update a policy.
Enter "ha-all" next to Name, "^ha\." next to Pattern, and "ha-mode" = "all" in the first line next to Policy.
Click Add policy.|

```language
rabbitmqctl set_policy ha-all "^ha\." '{"ha-mode":"all"}'
```


RabbitMQ 3.0.x 에서부터는 서버 configuration 을 통해 구성하도록 변경된 것을 확인하였습니다.

관련하여 어플리케이션 레벨에서 설정 ( deprecated configuration ) 은 더이상 의미가 없으며,

서버 configuration ( latedst configuration )에서 설정하도록 처리하게 되면, 특정 node 다운 시 fail-over 동작이 잘 이루어 진다.

#### HA queue 설정 완료 화면
![ha](https://github.com/oskmkr/rabbitmq-cluster-ha/blob/master/rabbitmq_ha.jpg)

## load balancing

clustering 이 부하 분산을 의미하지는 않는다.
따라서 각 노드에 적절히 부하가 분산될 수 있도록, 매우 짧은 TTL 을 가진 Dynamic DNS service 혹은, TCP load balancer, pacemaker, HAproxy 등을 통해 load balancing 을 해주어야 한다.

가령 아래와 같은 형태로 load balancer 구성하여 클라이언트에서 connection 시 부하 분산을 고려할 수 있다.

```language
rabbitMQ cluster [ node1, node2 ... nodeN ]
```

```language
{mq.lb1.address} -- rabbitMQ cluster [ node1, node2 ... nodeN ]

or

{mq.lb1.address} -- rabbitMQ cluster [ node1, node2 ... node5 ]
{mq.lb2.address} -- rabbitMQ cluster [ node6, node7 ... nodeN ]
```

- simple tcp load balancer : https://github.com/oskmkr/nodejs-proxy/

#### application connection configuration

```xml
<rabbit:connection-factory id="connectionFactory" addresses="{mq.lb1.address}, {mq.lb2.address}" username="guest" password="guest" virtual-host="/"  />
```

## reference
http://projects.spring.io/spring-amqp/
http://www.rabbitmq.com/clustering.html
http://www.rabbitmq.com/ha.html
