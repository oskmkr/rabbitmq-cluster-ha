package io.oskm.rabbitmq.springamqp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by sungkyu.eo on 2014-08-01.
 */
public class Producer {
	private static final Logger LOG = Logger.getLogger(Producer.class);

	public static void main(String[] args) throws InterruptedException, IOException {
		AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(
				"classpath:applicationContext-amqp-producer.xml");

		AmqpTemplate amqpTemplate = ctx.getBean(AmqpTemplate.class);
		MessageConverter messageConverter = ctx.getBean("jsonMessageConverter", MessageConverter.class);

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

		while (true) {
			String msg = in.readLine();
			if (msg == null) {
				break;
			}

			Whistle whistle = new Whistle();
			whistle.setUserId("me");
			whistle.setMessage(msg);

			MessageProperties messageProperties = MessagePropertiesBuilder.newInstance().build();
			Message messageObj = messageConverter.toMessage(whistle, messageProperties);

			// Message messageObj =
			// MessageBuilder.withBody(message.getBytes()).build();
			// Message messageObj =
			// MessageBuilder.withBody(message.getBytes()).setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN).setMessageId("1").setHeader("tx-header",
			// "transactionHeader").build();

			try {
				amqpTemplate.convertAndSend(messageObj);

				LOG.debug(" [#] Sent '" + msg + "'");
			} catch (AmqpException e) {
				LOG.error("Error while sending message. keeping looping...");
			}

			if ("bye".equals(msg.toLowerCase())) {
				break;
			}
		}
	}
}
