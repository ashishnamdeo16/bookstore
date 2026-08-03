package com.bookstore.books.author.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class AuthorRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;

    private String biography;

    private String country;
}
