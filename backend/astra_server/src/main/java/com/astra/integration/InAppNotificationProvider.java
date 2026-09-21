package com.astra.integration;

import com.astra.entity.Notification;

import org.springframework.stereotype.Component;

@Component
public class InAppNotificationProvider
        implements NotificationProvider {

    @Override
    public void send(Notification notification) {

        /*
         * In-app notification is persisted in the
         * notifications table.
         *
         * Future Phase can push it using WebSocket/SSE.
         */

        System.out.println(
                "ASTRA IN-APP NOTIFICATION CREATED: "
                        + notification.getSubject()
        );
    }
}