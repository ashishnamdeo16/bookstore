package com.bookstore.analytics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * One row per book. Updated on payment success for top-seller queries.
 */
@Entity
@Table(name = "book_sales")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSales {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36)
    private UUID bookId;

    @Column(nullable = false)
    private String bookTitle;

    @Builder.Default
    @Column(nullable = false)
    private long quantitySold = 0;

    @Builder.Default
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal revenue = BigDecimal.ZERO;
}
