package com.bookstore.analytics.repository;

import com.bookstore.analytics.entity.BookSales;
import com.bookstore.analytics.entity.ProcessedEvent;
import com.bookstore.analytics.support.MySQLTestcontainers;
import com.bookstore.analytics.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CatalogAnalyticsRepositoryTest {

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        MySQLTestcontainers.registerDatasourceProperties(registry);
    }

    @Autowired private BookSalesRepository bookSalesRepository;
    @Autowired private ProcessedEventRepository processedEventRepository;

    @BeforeEach
    void clean() {
        bookSalesRepository.deleteAll();
        processedEventRepository.deleteAll();
    }

    @Test
    void GivenBookSales_WhenSaveAndFind_ThenRoundTrip() {
        UUID bookId = UUID.randomUUID();
        BookSales saved = bookSalesRepository.save(
                TestDataFactory.bookSales(bookId, "Clean Code", 5, "149.95"));

        assertThat(saved.getBookId()).isEqualTo(bookId);
        BookSales loaded = bookSalesRepository.findById(bookId).orElseThrow();
        assertThat(loaded.getBookTitle()).isEqualTo("Clean Code");
        assertThat(loaded.getQuantitySold()).isEqualTo(5);
        assertThat(loaded.getRevenue()).isEqualByComparingTo("149.95");
    }

    @Test
    void GivenManyBooks_WhenFindTop10ByQuantitySoldDesc_ThenReturnHighestFirstCappedAt10() {
        for (int i = 1; i <= 12; i++) {
            bookSalesRepository.save(TestDataFactory.bookSales(
                    UUID.randomUUID(), "Book " + i, i, String.valueOf(i * 10) + ".00"));
        }

        List<BookSales> top = bookSalesRepository.findTop10ByOrderByQuantitySoldDesc();

        assertThat(top).hasSize(10);
        assertThat(top.get(0).getQuantitySold()).isEqualTo(12);
        assertThat(top.get(9).getQuantitySold()).isEqualTo(3);
        assertThat(top).extracting(BookSales::getQuantitySold)
                .isSortedAccordingTo((a, b) -> Long.compare(b, a));
    }

    @Test
    void GivenExistingBookSales_WhenUpdate_ThenPersistIncrement() {
        UUID bookId = UUID.randomUUID();
        BookSales sales = bookSalesRepository.save(
                TestDataFactory.bookSales(bookId, "DDD", 2, "80.00"));
        sales.setQuantitySold(5);
        sales.setRevenue(sales.getRevenue().add(new java.math.BigDecimal("120.00")));
        bookSalesRepository.saveAndFlush(sales);

        BookSales reloaded = bookSalesRepository.findById(bookId).orElseThrow();
        assertThat(reloaded.getQuantitySold()).isEqualTo(5);
        assertThat(reloaded.getRevenue()).isEqualByComparingTo("200.00");
    }

    @Test
    void GivenProcessedEvent_WhenSave_ThenExistsById() {
        String key = "order-created:" + UUID.randomUUID();
        processedEventRepository.save(ProcessedEvent.builder().eventKey(key).build());

        assertThat(processedEventRepository.existsById(key)).isTrue();
        assertThat(processedEventRepository.findById(key)).isPresent();
        assertThat(processedEventRepository.findById(key).orElseThrow().getProcessedAt()).isNotNull();
    }
}
