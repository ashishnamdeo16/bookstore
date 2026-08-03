package com.bookstore.user.dto;

import lombok.*;

import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntialResponseDto {

    private UUID userId;

    private String email;
}
