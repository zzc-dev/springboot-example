package com.xncoding.jwt.controller;

import com.xncoding.jwt.model.RequestMessage;
import com.xncoding.jwt.model.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * WsController
 *
 * @author XiongNeng
 * @version 1.0
 * @since 2018/2/28
 */
@RestController
public class WsController {

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public WsController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/welcome")
    @SendTo("/topic/say")
    public ResponseMessage say(RequestMessage message) {
        System.out.println(message.getName());
        return new ResponseMessage("welcome," + message.getName() + " !");
    }

    /**
     * 点对点通信，前端订阅地址要以/user为前缀，标识是点对点通信，只有当前用户才会收到
     * stompClient.subscribe("/user/singleTalkClient",(resp) => {});
     * */
   @GetMapping("/point")
    public void say1(){
        this.messagingTemplate.convertAndSendToUser("zzc","/queue/point","testPoint");
    }

    /**
     * 定时推送消息
     */
    @Scheduled(fixedRate = 1000)
    public void callback() {
        // 发现消息
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        messagingTemplate.convertAndSend("/topic/callback", "定时推送消息时间: " + df.format(new Date()));
    }

    @GetMapping("/test")
    public String test(){
        messagingTemplate.convertAndSendToUser("zzc", "/singleTalkClient", 11);
        messagingTemplate.convertAndSendToUser("zzc1", "/singleTalkClient", 11);
        return "test";
    }

    @MessageMapping("/singleTalkServer")  //客户端发到服务端
//    @SendToUser(value = "/singleTalkClient", broadcast = true)  //服务端发到客户端，客户端订阅, broadcast是否推送到同一session的不同终端页面中
    public Map singleTalk(Map msg,
                          StompHeaderAccessor accessor, //所有消息头信息
                          @Headers Map<String, Object> headers, //所有头部值
                          Principal principal ,  //登录验证信息
                          @Header(name="simpSessionId") String sessionId, //指定头部的值 ，这里指sessionId
                          Message message,   //完整消息，包含消息头和消息体（即header和body）
                          @Payload String body){ //消息体内容

        //给指定客户端发消息
        messagingTemplate.convertAndSendToUser("zzc1", "/singleTalkClient", msg);

        //消息直接返回
        return msg;
    }

}
