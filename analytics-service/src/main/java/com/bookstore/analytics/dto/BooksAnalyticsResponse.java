package com.bookstore.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BooksAnalyticsResponse {

    private long booksSold;
    private List<TopBookItem> topBooks;
}
