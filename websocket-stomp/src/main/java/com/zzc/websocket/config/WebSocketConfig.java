package com.zzc.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker// 使用此注解来标识使能WebSocket的broker.即使用broker来处理消息.
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    /**
     * 连接路径配置
     *
     * @param registry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        /*
         * 路径"/websocket"被注册为STOMP端点，对外暴露，客户端通过该路径获取与socket的连接
         */
        registry.addEndpoint("/chat").setAllowedOrigins("*").withSockJS();
    }

    /**
     * 服务端接收消息路径配置
     *
     * @param config
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        //消息代理的前缀 该路径消息会被代理通过广播方式发给客户端（广播路径）
        config.enableSimpleBroker("/topic");

        /*
         * 过滤该路径集合发送过来的消息，被@MessageMapping注解的方法接收处理具体决定广播还是单点发送到客户端
         */
        config.setApplicationDestinationPrefixes("/app", "/queue");
    }


}
