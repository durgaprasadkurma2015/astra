package com.astra.integration;

import com.astra.entity.Notification;

import org.springframework.stereotype.Component;

@Component
public class SmsNotificationProvider
        implements NotificationProvider {

    @Override
    public void send(Notification notification) {

        /*
         * Local development provider.
         *
         * Production can later use:
         * Twilio / MSG91 / AWS SNS / etc.
         */

        System.out.println();
        System.out.println("======================================");
        System.out.println("ASTRA SMS NOTIFICATION");
        System.out.println("To      : " + notification.getRecipient());
        System.out.println("Message : " + notification.getMessage());
        System.out.println("======================================");
        System.out.println();
    }
}