package ru.checkscan.app.domain.model;

public class ReceiptItem {
    public final String name;
    public final double quantity;
    public final long price, total;

    public ReceiptItem(String name, double quantity, long price, long total) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.total = total;
    }
}
