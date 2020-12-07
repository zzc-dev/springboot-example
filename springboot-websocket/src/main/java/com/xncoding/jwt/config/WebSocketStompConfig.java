package com.xncoding.jwt.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import java.security.Principal;

/**
 * STOMP协议的WebStocket
 *
 * @author XiongNeng
 * @version 1.0
 * @since 2018/2/28
 */
@Configuration
@EnableWebSocketMessageBroker  // 开启使用STOMP协议来传输基于代理的消息，Broke:代理
public class WebSocketStompConfig extends AbstractWebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
        stompEndpointRegistry.addEndpoint("/ws/stomp")
                .setAllowedOrigins("*") //解决跨域问题
                .withSockJS();
    }

    /**
     * topic: 广播通信
     * queue: 点对点通信
     * */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/singleTalkClient");   // 服务器向客户端推送消息的前缀
        registry.setApplicationDestinationPrefixes("/app");         //  服务器接受客户端消息的前缀
    }
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
//                    String uid = accessor.getNativeHeader("uid").get(0);
//                    String onDisconnectTopic = accessor.getNativeHeader("onDisconnectTopic").get(0);
//                    String clientId = accessor.getNativeHeader("clientId").get(0);
                    Principal principal = () -> "1";
                    //设置用户信息
                    accessor.setUser(principal);
                    accessor.getSessionAttributes().put("onDisconnectTopic","ond");
                    accessor.getSessionAttributes().put("clientId","1");
                    return message;
                }
                return message;
            }
        });
    }



}
