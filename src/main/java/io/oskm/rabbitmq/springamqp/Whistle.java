package io.oskm.rabbitmq.springamqp;

/**
 * Created by sungkyu.eo on 2014-08-11.
 */
public class Whistle {
    private String userId;
    private String message;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
