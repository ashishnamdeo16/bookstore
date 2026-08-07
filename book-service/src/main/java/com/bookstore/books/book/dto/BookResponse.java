package com.bookstore.books.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookResponse {

    private UUID id;

    private String isbn;

    private String title;

    private String description;

    private BigDecimal price;

    private String language;

    private LocalDate publishedDate;

    private String coverImageUrl;

    private UUID publisherId;

    private UUID categoryId;

    private Set<UUID> authorIds;
}
