package ru.checkscan.app.domain.usecase;

import ru.checkscan.app.domain.model.ProductStats;
import ru.checkscan.app.domain.model.Receipt;
import ru.checkscan.app.domain.repository.ReceiptRepository;

import java.util.List;

public class ReceiptQueries {
    private final ReceiptRepository repository;

    public ReceiptQueries(ReceiptRepository repository) {
        this.repository = repository;
    }

    public List<Receipt> history() {
        return repository.getReceipts();
    }

    public Receipt receipt(long id) {
        return repository.getReceipt(id);
    }

    public void delete(long id) {
        repository.delete(id);
    }

    public List<ProductStats> products() {
        return repository.getProductStats();
    }

    public ProductStats product(String name) {
        return repository.getProductStats(name);
    }
}
