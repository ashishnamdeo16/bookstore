package com.bookstore.notification.util;

import com.bookstore.notification.event.OrderCreatedEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EmailTemplateBuilder {

    private EmailTemplateBuilder() {
    }

    public static String buildSubject(OrderCreatedEvent event) {
        return "Order Confirmation - " + event.getOrderId();
    }

    public static String buildBody(OrderCreatedEvent event) {

        StringBuilder builder = new StringBuilder();

        builder.append("Dear ")
                .append(event.getFirstName())
                .append(",\n\n");

        builder.append("Thank you for shopping with BookStore!\n\n");

        builder.append("Your order has been placed successfully.\n\n");

        builder.append("----------------------------------------\n");
        builder.append("Order Details\n");
        builder.append("----------------------------------------\n");
        builder.append("Order ID      : ").append(event.getOrderId()).append("\n");
        builder.append("Total Amount  : $").append(event.getTotalAmount()).append("\n");
        builder.append("Status        : ").append(event.getStatus()).append("\n");
        builder.append("Order Date    : ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")))
                .append("\n\n");

        builder.append("Items:\n");

        event.getItems().forEach(item -> {
            builder.append("• Book Title : ")
                    .append(item.getBookTitle())
                    .append("\n");
            builder.append("  Quantity : ")
                    .append(item.getQuantity())
                    .append("\n");
            builder.append("  Price    : $")
                    .append(item.getPrice())
                    .append("\n\n");
        });

        builder.append("----------------------------------------\n");
        builder.append("We appreciate your purchase and hope you enjoy your books.\n\n");

        builder.append("If you have any questions, simply reply to this email.\n\n");

        builder.append("Best Regards,\n");
        builder.append("BookStore Team");

        return builder.toString();
    }

}