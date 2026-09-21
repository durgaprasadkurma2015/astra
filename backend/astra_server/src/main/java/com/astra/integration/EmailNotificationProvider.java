package com.astra.integration;

import com.astra.entity.Notification;

import org.springframework.stereotype.Component;

@Component
public class EmailNotificationProvider
        implements NotificationProvider {

    @Override
    public void send(Notification notification) {

        /*
         * Phase 13 local implementation.
         *
         * Real SMTP/provider integration will be added
         * when production email credentials are configured.
         */

        System.out.println();
        System.out.println("======================================");
        System.out.println("ASTRA EMAIL NOTIFICATION");
        System.out.println("To      : " + notification.getRecipient());
        System.out.println("Subject : " + notification.getSubject());
        System.out.println("Message : " + notification.getMessage());
        System.out.println("======================================");
        System.out.println();
    }
}