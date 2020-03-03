package com.xncoding.jwt.listener;

import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

import javax.xml.bind.ValidationException;

/**
 * 该拦截器得到的为空 ？？？
 * */
//@Component
public class WebSocketOnConnectEventListener implements ApplicationListener<SessionConnectedEvent> {
    @Override
    public void onApplicationEvent(SessionConnectedEvent sessionConnectedEvent) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(sessionConnectedEvent.getMessage());
        String onDisconnectTopic = sha.getFirstNativeHeader("onDisconnectTopic");
        String clientId = sha.getFirstNativeHeader("clientId");

        if(StringUtils.isEmpty(onDisconnectTopic) || StringUtils.isEmpty(clientId)){
            try {
                throw new ValidationException("onDisconnectTopic or clientId is required");
            } catch (ValidationException e) {
                e.printStackTrace();
            }
        }
        System.out.println("clientId="+clientId);
        System.out.println("onDisconnectTopic="+onDisconnectTopic);

        sha.getSessionAttributes().put("onDisconnectTopic",onDisconnectTopic);
        sha.getSessionAttributes().put("clientId",clientId);
    }
}
