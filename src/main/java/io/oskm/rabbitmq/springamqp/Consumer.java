package io.oskm.rabbitmq.springamqp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by sungkyu.eo on 2014-08-01.
 */
public class Consumer {
	private static final Logger LOG = LoggerFactory.getLogger(Consumer.class);

	public static void main(String[] args) {
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				"classpath:applicationContext-amqp-consumer.xml");
	}

}
