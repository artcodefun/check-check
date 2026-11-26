package ru.checkscan.app.domain.model;

import java.util.List;

public class SpendingReport {
    public enum Period {
        MONTH,
        YEAR,
        ALL
    }

    public final Period period;
    public final long total, previousTotal;
    public final int count, previousCount;
    public final List<Bucket> buckets;
    public final List<Product> products;
    public final List<Merchant> merchants;
    public final List<PriceChange> priceChanges;

    public SpendingReport(
            Period period,
            long total,
            int count,
            long previousTotal,
            int previousCount,
            List<Bucket> buckets,
            List<Product> products,
            List<Merchant> merchants,
            List<PriceChange> priceChanges) {
        this.period = period;
        this.total = total;
        this.count = count;
        this.previousTotal = previousTotal;
        this.previousCount = previousCount;
        this.buckets = buckets;
        this.products = products;
        this.merchants = merchants;
        this.priceChanges = priceChanges;
    }

    public static class Bucket {
        public final String label, key;
        public final long total;
        public final List<Receipt> receipts;

        public Bucket(String label, String key, long total, List<Receipt> receipts) {
            this.label = label;
            this.key = key;
            this.total = total;
            this.receipts = receipts;
        }
    }

    public static class Product {
        public final String name;
        public final long total;
        public final int count;

        public Product(String name, long total, int count) {
            this.name = name;
            this.total = total;
            this.count = count;
        }
    }

    public static class Merchant {
        public final String name;
        public final long total;
        public final int count;

        public Merchant(String name, long total, int count) {
            this.name = name;
            this.total = total;
            this.count = count;
        }
    }

    public static class PriceChange {
        public final String name, date;
        public final long previous, current;

        public PriceChange(String name, String date, long previous, long current) {
            this.name = name;
            this.date = date;
            this.previous = previous;
            this.current = current;
        }
    }
}
