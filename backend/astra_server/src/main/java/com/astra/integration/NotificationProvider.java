package com.astra.integration;

import com.astra.entity.Notification;

public interface NotificationProvider {

    void send(Notification notification);
}