package ru.checkscan.app.domain.repository;

import ru.checkscan.app.domain.model.Receipt;

public interface ReceiptProvider {
    Receipt fetch(String qr) throws Exception;
}
