package com.bookstore.books.author.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthorResponse {

    private UUID id;

    private String firstName;

    private String lastName;

    private String biography;

    private String country;

}
