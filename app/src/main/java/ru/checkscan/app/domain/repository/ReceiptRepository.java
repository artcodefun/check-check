package ru.checkscan.app.domain.repository;

import ru.checkscan.app.domain.model.ProductStats;
import ru.checkscan.app.domain.model.Receipt;

import java.util.List;

public interface ReceiptRepository {
    boolean exists(String qr);

    void save(Receipt receipt);

    List<Receipt> getReceipts();

    Receipt getReceipt(long id);

    void delete(long id);

    List<ProductStats> getProductStats();

    ProductStats getProductStats(String name);
}
