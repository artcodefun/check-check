package ru.checkscan.app.domain.model;

import java.util.List;

public class Receipt {
    public final long id;
    public final String qr, date, merchant;
    public final long total;
    public final List<ReceiptItem> items;

    public Receipt(
            long id, String qr, String date, String merchant, long total, List<ReceiptItem> items) {
        this.id = id;
        this.qr = qr;
        this.date = date;
        this.merchant = merchant;
        this.total = total;
        this.items = items;
    }
}
