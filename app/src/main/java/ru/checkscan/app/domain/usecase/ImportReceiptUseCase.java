package ru.checkscan.app.domain.usecase;

import ru.checkscan.app.domain.repository.ReceiptProvider;
import ru.checkscan.app.domain.repository.ReceiptRepository;

public class ImportReceiptUseCase {
    private final ReceiptRepository repository;
    private final ReceiptProvider provider;

    public ImportReceiptUseCase(ReceiptRepository repository, ReceiptProvider provider) {
        this.repository = repository;
        this.provider = provider;
    }

    public void execute(String qr) throws Exception {
        if (qr == null || qr.trim().isEmpty())
            throw new IllegalArgumentException("Введите данные QR-кода");
        qr = qr.trim();
        if (repository.exists(qr)) throw new IllegalArgumentException("Этот чек уже добавлен");
        repository.save(provider.fetch(qr));
    }
}
