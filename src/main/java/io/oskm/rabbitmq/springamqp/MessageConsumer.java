package io.oskm.rabbitmq.springamqp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sungkyu.eo on 2014-08-04.
 */
public class MessageConsumer {
	private static final Logger LOG = LoggerFactory.getLogger(MessageConsumer.class);
	private static int listenCount = 0;
	private static int eavesdropCount = 0;

	public void listen(Whistle message) {
		LOG.debug("# listen, Received : '" + message.getMessage() + "' : " + (++listenCount));

	}

	public void eavesdrop(Whistle message) {
		LOG.debug("# eavesdrop, Received : '" + message.getMessage() + "' : " + (++eavesdropCount));
	}
}
