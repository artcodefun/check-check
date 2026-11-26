package ru.checkscan.app.domain.usecase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;

import ru.checkscan.app.domain.model.ProductStats;
import ru.checkscan.app.domain.model.Receipt;
import ru.checkscan.app.domain.repository.ReceiptRepository;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ImportReceiptUseCaseTest {
    private static class FakeRepository implements ReceiptRepository {
        boolean exists;
        Receipt saved;

        @Override
        public boolean exists(String qr) {
            return exists;
        }

        @Override
        public void save(Receipt receipt) {
            saved = receipt;
        }

        @Override
        public List<Receipt> getReceipts() {
            return Collections.emptyList();
        }

        @Override
        public Receipt getReceipt(long id) {
            return null;
        }

        @Override
        public void delete(long id) {}

        @Override
        public List<ProductStats> getProductStats() {
            return Collections.emptyList();
        }

        @Override
        public ProductStats getProductStats(String name) {
            return null;
        }
    }

    @Test
    public void importsAndSavesReceipt() throws Exception {
        FakeRepository repository = new FakeRepository();
        Receipt receipt =
                new Receipt(0, "qr", "2026-01-01", "Магазин", 100, Collections.emptyList());
        new ImportReceiptUseCase(
                        repository,
                        qr -> {
                            assertEquals("qr", qr);
                            return receipt;
                        })
                .execute(" qr ");
        assertSame(receipt, repository.saved);
    }

    @Test
    public void duplicateDoesNotCallProviderOrSave() throws Exception {
        FakeRepository repository = new FakeRepository();
        repository.exists = true;
        try {
            new ImportReceiptUseCase(
                            repository,
                            qr -> {
                                fail("provider called");
                                return null;
                            })
                    .execute("qr");
            fail("expected error");
        } catch (IllegalArgumentException expected) {
            assertNull(repository.saved);
        }
    }

    @Test
    public void providerFailureDoesNotSave() throws Exception {
        FakeRepository repository = new FakeRepository();
        try {
            new ImportReceiptUseCase(
                            repository,
                            qr -> {
                                throw new IOException("network");
                            })
                    .execute("qr");
            fail("expected error");
        } catch (IOException expected) {
            assertNull(repository.saved);
        }
    }

    @Test
    public void emptyQrDoesNotCallProvider() throws Exception {
        FakeRepository repository = new FakeRepository();
        try {
            new ImportReceiptUseCase(
                            repository,
                            qr -> {
                                fail("provider called");
                                return null;
                            })
                    .execute("  ");
            fail("expected error");
        } catch (IllegalArgumentException expected) {
            assertNull(repository.saved);
        }
    }
}
