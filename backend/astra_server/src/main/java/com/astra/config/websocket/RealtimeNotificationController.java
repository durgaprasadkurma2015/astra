package com.astra.config.websocket;
import org.springframework.messaging.handler.annotation.*; import org.springframework.stereotype.Controller;
@Controller public class RealtimeNotificationController { @MessageMapping("/ping") @SendTo("/topic/notifications") public String ping(String message){return message==null?"pong":message;} }
