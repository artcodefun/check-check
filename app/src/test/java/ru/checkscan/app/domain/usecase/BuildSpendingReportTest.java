package ru.checkscan.app.domain.usecase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ru.checkscan.app.domain.model.Receipt;
import ru.checkscan.app.domain.model.ReceiptItem;
import ru.checkscan.app.domain.model.SpendingReport;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class BuildSpendingReportTest {
    private Date date(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(year, month - 1, day, 12, 0);
        return calendar.getTime();
    }

    private Receipt receipt(
            long id, String date, String store, long total, long price, long itemTotal) {
        return new Receipt(
                id,
                "qr" + id,
                date,
                store,
                total,
                Collections.singletonList(new ReceiptItem("Молоко", 1, price, itemTotal)));
    }

    @Test
    public void monthFiltersAndSeparatesReceiptAndItemTotals() {
        SpendingReport result =
                new BuildSpendingReport()
                        .execute(
                                Arrays.asList(
                                        receipt(1, "2026-01-31T12:00:00", "А", 5000, 1000, 1000),
                                        receipt(2, "2026-02-01T12:00:00", "А", 11000, 3000, 3000),
                                        receipt(3, "2026-02-25T12:00:00", "Б", 9000, 4000, 4000),
                                        receipt(4, "2026-03-01T12:00:00", "Б", 2000, 1000, 1000)),
                                SpendingReport.Period.MONTH,
                                date(2026, 2, 26));
        assertEquals(20000, result.total);
        assertEquals(2, result.count);
        assertEquals(5000, result.previousTotal);
        assertEquals(1, result.previousCount);
        assertEquals(28, result.buckets.size());
        assertEquals(11000, result.buckets.get(0).total);
        assertEquals(7000, result.products.get(0).total);
        assertEquals(2, result.merchants.size());
        assertEquals(3000, result.priceChanges.get(0).previous);
        assertEquals(4000, result.priceChanges.get(0).current);
    }

    @Test
    public void yearBoundaryAndAllTimeBuckets() {
        java.util.List<Receipt> receipts =
                Arrays.asList(
                        receipt(1, "2025-12-31T23:59:00", "А", 100, 100, 100),
                        receipt(2, "2026-01-01T00:00:00", "А", 200, 200, 200));
        SpendingReport year =
                new BuildSpendingReport()
                        .execute(receipts, SpendingReport.Period.YEAR, date(2026, 1, 1));
        assertEquals(200, year.total);
        assertEquals(100, year.previousTotal);
        assertEquals(12, year.buckets.size());
        SpendingReport all =
                new BuildSpendingReport()
                        .execute(receipts, SpendingReport.Period.ALL, date(2026, 1, 1));
        assertEquals(300, all.total);
        assertEquals(2, all.buckets.size());
        assertEquals(1, all.priceChanges.size());
    }

    @Test
    public void emptyPeriodAndZeroPriceAreHandled() {
        SpendingReport report =
                new BuildSpendingReport()
                        .execute(
                                Collections.singletonList(
                                        receipt(1, "2025-12-01T10:00:00", "А", 0, 0, 0)),
                                SpendingReport.Period.MONTH,
                                date(2026, 2, 1));
        assertEquals(0, report.count);
        assertEquals(0, report.previousCount);
        assertTrue(report.priceChanges.isEmpty());
    }
}
