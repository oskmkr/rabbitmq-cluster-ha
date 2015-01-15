# RabbitMQ Clustering 과 HA 구성

[TOC]

## sample code
https://github.com/oskmkr/rabbitmq-cluster-ha/

## rabbitmq version
rabbit-mq-3.4.2 ( 포스팅 작성 기준 latest version )
== rabbit-mq-3.3.4 도 테스트 동일 ==

## 기본 command
`rabbitmqctrl -n [노드명] [command]`

## Clustering
node1, node2, node3, node4

위 4개의 노드 생성하고, clustering 구성을 진행해 본다.

start_node.bat [노드명] [포트] [모니터링포트]

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

// TODO 모니터링 페이지

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
```code
rabbitmqctrl -n node2 stop_app // 노드 중단
Stopping node 'node3@XXXXXX' ...
rabbitmqctrl -n node2 join_cluster node1@XXXXXXX // node2 를 node1 의 cluster 구성원으로 추가
Clustering node 'node3@XXXXXX' with 'node2@XXXXXX' ...
rabbitmqctrl -n node2 start_app // 노드 시작
// node3 과 node4 도 위 과정과 동일하게 추가한다.
```

1. before clustering

2. after clustering

*configuration 으로도 가능하다.*

## HA (High Availability)

RabbitMQ 2.x 대에서는 ha 구성 관련하여, application level에서 아래의 설정을 통해 구성 하였으나,

```xml
<rabbit:queue-arguments>
        <entry key="x-ha-policy" value="all"></entry>
</rabbit:queue-arguments>
```

RabbitMQ 3.0.x 에서부터는 서버 configuration 을 통해 구성하도록 변경된 것을 확인하였습니다.

관련하여 어플리케이션 레벨에서 위 설정은 더이상 의미가 없으니 삭제하는 것이 좋을 듯하며,

서버 configuration 에서 설정하도록 처리하면, 특정 node 다운 시 fail-over 동작이 잘 이루어 지는 것을 확인하였습니다.

```language
rabbitmqctl set_policy ha-all "^ha\." '{"ha-mode":"all"}'
```

* HA 설정 화면

## load balancing

clustering 이 부하 분산을 의미하지는 않는다.
따라서 각 노드에 적절히 부하가 분산될 수 있도록, 매우 짧은 TTL 을 가진 Dynamic DNS service 혹은, TCP load balancer, pacemaker, HAproxy 등을 통해 load balancing 을 해주어야 한다.

아래와 같은 형태로 구성하여 부하 분산을 고려할 수 있다.

#### lb - node 관계
mq.lb1 - node1, node2
mq.lb2 - node3, node4

#### application connection configuration

```xml
<rabbit:connection-factory id="connectionFactory" addresses="{mq.lb1.address}, {mq.lb2.address}" username="guest" password="guest" virtual-host="/"  />
```

```language
rabbitMQ cluster [ node1, node2 ... nodeN ]
```

```language
{mq.lb1.address} -- rabbitMQ cluster [ node1, node2 ... node5 ]
{mq.lb2.address} -- rabbitMQ cluster [ node6, node7 ... node10 ]
```

```language
fff
```

## reference
http://projects.spring.io/spring-amqp/
http://www.rabbitmq.com/clustering.html
http://www.rabbitmq.com/ha.html