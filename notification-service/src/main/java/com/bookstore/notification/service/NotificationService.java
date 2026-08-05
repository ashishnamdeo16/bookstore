package com.bookstore.notification.service;

import com.bookstore.notification.event.OrderCreatedEvent;

public interface NotificationService {

    void process(OrderCreatedEvent event);

}
