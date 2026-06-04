package com.exam.config;

import com.exam.common.constant.RabbitMQConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 */
@Configuration
public class RabbitMQConfig {

    // ==================== 死信交换机和队列 ====================

    /**
     * 死信交换机
     */
    @Bean
    public DirectExchange dlxExchange() {
        return ExchangeBuilder.directExchange(RabbitMQConstant.DLX_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 死信队列
     */
    @Bean
    public Queue gradingDeadLetterQueue() {
        return QueueBuilder.durable(RabbitMQConstant.GRADING_DLQ).build();
    }

    /**
     * 死信队列绑定到死信交换机
     */
    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(gradingDeadLetterQueue())
                .to(dlxExchange())
                .with(RabbitMQConstant.GRADING_DLQ_ROUTING_KEY);
    }

    // ==================== 主交换机和队列 ====================

    /**
     * 主交换机
     */
    @Bean
    public DirectExchange examExchange() {
        return ExchangeBuilder.directExchange(RabbitMQConstant.EXAM_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 自动阅卷主队列（绑定死信交换机，设置消息TTL）
     */
    @Bean
    public Queue gradingQueue() {
        return QueueBuilder.durable(RabbitMQConstant.GRADING_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMQConstant.DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMQConstant.GRADING_DLQ_ROUTING_KEY)
                .withArgument("x-message-ttl", 60000)
                .build();
    }

    /**
     * 主队列绑定到主交换机
     */
    @Bean
    public Binding gradingBinding() {
        return BindingBuilder.bind(gradingQueue())
                .to(examExchange())
                .with(RabbitMQConstant.GRADING_ROUTING_KEY);
    }

    // ==================== 消息转换器 ====================

    /**
     * Jackson JSON 消息转换器
     */
    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置 RabbitTemplate 使用 Jackson 消息转换器
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

    /**
     * 配置 RabbitListenerContainerFactory 使用 Jackson 消息转换器
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter());
        return factory;
    }
}
