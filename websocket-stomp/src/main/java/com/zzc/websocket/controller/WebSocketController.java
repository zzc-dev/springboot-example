package com.zzc.websocket.controller;


import com.zzc.websocket.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@Slf4j
public class WebSocketController {
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /*页面入口*/
    @RequestMapping("/chat")
    public String chat() {
        return "chat";
    }
    /**
     * 群发
     *
     * @param message
     * @return
     * @throws Exception
     */
    @MessageMapping("/hello") //接收/app/hello路径发来的信息：/app被@MessageMapping拦截，/hello被注解内参数拦截
    @SendTo("/topic/greetings")//接收上面路径发来的消息后在发送到广播的路径上 即会被代理进行广播群发
    public Message messageHandling(Message message) throws Exception {
        return message;
    }
}
