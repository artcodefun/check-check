package ru.checkscan.app.domain.model;

import java.util.List;

public class ProductStats {
    public final String name;
    public final long total, min, max, average;
    public final int count;
    public final List<PriceEntry> history;

    public ProductStats(
            String name,
            long total,
            long min,
            long max,
            long average,
            int count,
            List<PriceEntry> history) {
        this.name = name;
        this.total = total;
        this.min = min;
        this.max = max;
        this.average = average;
        this.count = count;
        this.history = history;
    }

    public static class PriceEntry {
        public final String date;
        public final String merchant;
        public final long price;
        public final double quantity;

        public PriceEntry(String date, long price, double quantity, String merchant) {
            this.date = date;
            this.merchant = merchant;
            this.price = price;
            this.quantity = quantity;
        }
    }
}
