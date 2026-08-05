package com.bookstore.analytics.controller;

import com.bookstore.analytics.dto.BooksAnalyticsResponse;
import com.bookstore.analytics.dto.DailyRevenueItem;
import com.bookstore.analytics.dto.DashboardResponse;
import com.bookstore.analytics.dto.MonthlyRevenueItem;
import com.bookstore.analytics.dto.OrdersAnalyticsResponse;
import com.bookstore.analytics.dto.PaymentsAnalyticsResponse;
import com.bookstore.analytics.dto.RevenueResponse;
import com.bookstore.analytics.service.AnalyticsQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsQueryService analyticsQueryService;

    public AnalyticsController(AnalyticsQueryService analyticsQueryService) {
        this.analyticsQueryService = analyticsQueryService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> dashboard() {
        return ResponseEntity.ok(analyticsQueryService.getDashboard());
    }

    @GetMapping("/revenue")
    public ResponseEntity<RevenueResponse> revenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(analyticsQueryService.getRevenue(from, to));
    }

    @GetMapping("/revenue/daily")
    public ResponseEntity<List<DailyRevenueItem>> revenueDaily(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(analyticsQueryService.getDailyRevenue(from, to));
    }

    @GetMapping("/revenue/monthly")
    public ResponseEntity<List<MonthlyRevenueItem>> revenueMonthly(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(analyticsQueryService.getMonthlyRevenue(from, to));
    }

    @GetMapping("/orders")
    public ResponseEntity<OrdersAnalyticsResponse> orders(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(analyticsQueryService.getOrders(from, to));
    }

    @GetMapping("/books")
    public ResponseEntity<BooksAnalyticsResponse> books() {
        return ResponseEntity.ok(analyticsQueryService.getBooks());
    }

    @GetMapping("/payments")
    public ResponseEntity<PaymentsAnalyticsResponse> payments() {
        return ResponseEntity.ok(analyticsQueryService.getPayments());
    }
}
