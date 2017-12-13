package com.shubham.rabbitmqdemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class RabbitmqDemoApplication {

    @Value("${spring.rabbitmq.queue.durable}")
    private boolean isDurable;
    @Value("${spring.rabbitmq.username}")
    private String amqpUsername;
    @Value("${spring.rabbitmq.password}")
    private String amqpPwd;
    @Value("${spring.rabbitmq.host}")
    private String amqpHost;
    @Value("${spring.rabbitmq.port}")
    private int amqpPort;
    @Value("${spring.rabbitmq.queue1}")
    private String queueA;
    @Value("${spring.rabbitmq.queue2}")
    private String queueB;
    @Value("${spring.rabbitmq.routingkeyA}")
    private String routingKeyA;
    @Value("${spring.rabbitmq.routingkeyB}")
    private String routingKeyB;
    @Value("${spring.rabbitmq.exchange}")
    private String exchangeName;

    @Autowired
    @Qualifier(value = "queueOneRabbitTemplate")
    private RabbitTemplate queueOneRabbitTemplate;

    @Autowired
    @Qualifier(value = "queueTwoRabbitTemplate")
    private RabbitTemplate queueTwoRabbitTemplate;

    private static final Logger LOG = LoggerFactory.getLogger(RabbitmqDemoApplication.class);

    /**
     * Main method to run the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(RabbitmqDemoApplication.class, args);
    }

    /**
     * Controller URL to access the application ---> START
     *
     * @param message
     * @return
     */
    @RequestMapping(value = "/message/{message}", method = RequestMethod.GET)
    public String getMessage(
            @PathVariable String message) {
        LOG.info("Received message {} via Controller. Publishing it on Queue One.", message);
        queueOneRabbitTemplate.convertAndSend(message);
        return "Message published.";
    }
    /*
      Controller URL to access the application ----> END
     */

    /**
     * RabbitMQ configuration to define two queues ----> START
     */
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(exchangeName);
    }

    /**
     * This method defines a Rabbit template bean which interacts only with Queue A
     */
    public @Bean(name = "queueOneRabbitTemplate")
    RabbitTemplate queueOneRabbitTemplate() {
        CachingConnectionFactory connectionfactory = new CachingConnectionFactory(amqpHost);
        connectionfactory.setUsername(amqpUsername);
        connectionfactory.setPassword(amqpPwd);
        connectionfactory.setHost(amqpHost);
        connectionfactory.setPort(amqpPort);
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionfactory);
        rabbitTemplate.setExchange(exchangeName);
        rabbitTemplate.setQueue(queueA);
        rabbitTemplate.setRoutingKey(routingKeyA);
        return rabbitTemplate;
    }

    /**
     * This method defines a Rabbit template bean which interacts only with Queue B
     */
    public @Bean(name = "queueTwoRabbitTemplate")
    RabbitTemplate queueTwoRabbitTemplate() {
        CachingConnectionFactory connectionfactory = new CachingConnectionFactory(amqpHost);
        connectionfactory.setUsername(amqpUsername);
        connectionfactory.setPassword(amqpPwd);
        connectionfactory.setHost(amqpHost);
        connectionfactory.setPort(amqpPort);
        RabbitTemplate rabbitTemplate = new RabbitTemplate();
        rabbitTemplate.setConnectionFactory(connectionfactory);
        rabbitTemplate.setExchange(exchangeName);
        rabbitTemplate.setQueue(queueB);
        rabbitTemplate.setRoutingKey(routingKeyB);
        return rabbitTemplate;
    }

    @Bean
    Queue queueA() {
        return new Queue(queueA, isDurable);
    }

    @Bean
    Queue queueB() {
        return new Queue(queueB, isDurable);
    }

    @Bean
    Binding binding1(TopicExchange exchange) {
        return BindingBuilder.bind(queueA()).to(exchange).with(routingKeyA);
    }

    @Bean
    Binding binding2(TopicExchange exchange) {
        return BindingBuilder.bind(queueB()).to(exchange).with(routingKeyB);
    }
    /*
      RabbitMQ configuration to define two queues ----> END
     */

    /**
     * RabbitMQ Queue Listener methods ---> START
     */
    @RabbitListener(queues = "${spring.rabbitmq.queue1}")
    public void initialConsumer(String message) {
        LOG.info("On queueA: {}.", message);
        queueTwoRabbitTemplate.convertAndSend(message);
    }

    @RabbitListener(queues = "${spring.rabbitmq.queue2}")
    public void secondConsumer(String message) {
        LOG.info("On queueB: {}.", message);
    }
    /*
      RabbitMQ Queue Listener methods ---> END
     */
}
