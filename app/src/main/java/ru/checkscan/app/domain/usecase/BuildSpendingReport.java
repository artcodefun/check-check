package ru.checkscan.app.domain.usecase;

import ru.checkscan.app.domain.model.Receipt;
import ru.checkscan.app.domain.model.ReceiptItem;
import ru.checkscan.app.domain.model.SpendingReport;
import ru.checkscan.app.domain.model.SpendingReport.Period;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/** Aggregates receipt data using calendar periods in the device's local time zone. */
public class BuildSpendingReport {
    public SpendingReport execute(List<Receipt> receipts, Period period, Date now) {
        Calendar start = Calendar.getInstance();
        start.setTime(now);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        start.set(Calendar.DAY_OF_MONTH, 1);
        if (period == Period.YEAR) start.set(Calendar.MONTH, Calendar.JANUARY);
        Calendar end = (Calendar) start.clone();
        if (period == Period.MONTH) end.add(Calendar.MONTH, 1);
        if (period == Period.YEAR) end.add(Calendar.YEAR, 1);
        Calendar previous = (Calendar) start.clone();
        if (period == Period.MONTH) previous.add(Calendar.MONTH, -1);
        if (period == Period.YEAR) previous.add(Calendar.YEAR, -1);
        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
        String startKey = keyFormat.format(start.getTime()),
                endKey = keyFormat.format(end.getTime()),
                previousKey = keyFormat.format(previous.getTime());
        Map<String, List<Receipt>> buckets = new TreeMap<>();
        if (period != Period.ALL) {
            Calendar cursor = (Calendar) start.clone();
            while (cursor.before(end)) {
                buckets.put(
                        bucketKey(keyFormat.format(cursor.getTime()), period), new ArrayList<>());
                cursor.add(period == Period.MONTH ? Calendar.DAY_OF_MONTH : Calendar.MONTH, 1);
            }
        }
        long total = 0;
        long previousTotal = 0;
        int count = 0;
        int previousCount = 0;
        Map<String, long[]> products = new LinkedHashMap<>();
        Map<String, long[]> merchants = new LinkedHashMap<>();
        Map<String, List<Purchase>> prices = new LinkedHashMap<>();
        for (Receipt receipt : receipts) {
            if (receipt.date == null || receipt.date.length() < 10) continue;
            String date = receipt.date.substring(0, 10);
            if (period != Period.ALL
                    && date.compareTo(previousKey) >= 0
                    && date.compareTo(startKey) < 0) {
                previousTotal += receipt.total;
                previousCount++;
            }
            if (period != Period.ALL
                    && (date.compareTo(startKey) < 0 || date.compareTo(endKey) >= 0)) continue;
            total += receipt.total;
            count++;
            String key = bucketKey(date, period);
            buckets.computeIfAbsent(key, k -> new ArrayList<>()).add(receipt);
            String merchant =
                    receipt.merchant == null || receipt.merchant.trim().isEmpty()
                            ? "Магазин"
                            : receipt.merchant;
            long[] m = merchants.computeIfAbsent(merchant, k -> new long[2]);
            m[0] += receipt.total;
            m[1]++;
            for (ReceiptItem item : receipt.items) {
                long[] p = products.computeIfAbsent(item.name, k -> new long[2]);
                p[0] += item.total;
                p[1]++;
                prices.computeIfAbsent(item.name, k -> new ArrayList<>())
                        .add(new Purchase(receipt.date, receipt.id, item.price));
            }
        }
        List<SpendingReport.Bucket> bucketList = new ArrayList<>();
        for (Map.Entry<String, List<Receipt>> entry : buckets.entrySet()) {
            long amount = 0;
            for (Receipt receipt : entry.getValue()) amount += receipt.total;
            bucketList.add(
                    new SpendingReport.Bucket(
                            label(entry.getKey(), period),
                            entry.getKey(),
                            amount,
                            entry.getValue()));
        }
        List<SpendingReport.Product> productList = new ArrayList<>();
        for (Map.Entry<String, long[]> e : products.entrySet())
            productList.add(
                    new SpendingReport.Product(e.getKey(), e.getValue()[0], (int) e.getValue()[1]));
        productList.sort((a, b) -> Long.compare(b.total, a.total));
        List<SpendingReport.Merchant> merchantList = new ArrayList<>();
        for (Map.Entry<String, long[]> e : merchants.entrySet())
            merchantList.add(
                    new SpendingReport.Merchant(
                            e.getKey(), e.getValue()[0], (int) e.getValue()[1]));
        merchantList.sort((a, b) -> Long.compare(b.total, a.total));
        List<SpendingReport.PriceChange> changes = new ArrayList<>();
        for (Map.Entry<String, List<Purchase>> e : prices.entrySet()) {
            List<Purchase> list = e.getValue();
            list.sort(
                    Comparator.comparing((Purchase p) -> p.date)
                            .thenComparingLong(p -> p.receiptId));
            if (list.size() >= 2) {
                Purchase last = list.get(list.size() - 1);
                Purchase before = list.get(list.size() - 2);
                changes.add(
                        new SpendingReport.PriceChange(
                                e.getKey(), last.date, before.price, last.price));
            }
        }
        changes.sort(
                (a, b) ->
                        Long.compare(
                                Math.abs(b.current - b.previous),
                                Math.abs(a.current - a.previous)));
        return new SpendingReport(
                period,
                total,
                count,
                previousTotal,
                previousCount,
                bucketList,
                productList,
                merchantList,
                changes);
    }

    private static String bucketKey(String date, Period period) {
        return period == Period.MONTH ? date : date.substring(0, 7);
    }

    private static String label(String key, Period period) {
        if (period == Period.MONTH) return key.substring(8, 10);
        int month = Integer.parseInt(key.substring(5, 7)) - 1;
        String[] months = {
            "янв", "фев", "мар", "апр", "май", "июн", "июл", "авг", "сен", "окт", "ноя", "дек"
        };
        return period == Period.ALL ? months[month] + " " + key.substring(2, 4) : months[month];
    }

    private static class Purchase {
        final String date;
        final long receiptId, price;

        Purchase(String date, long receiptId, long price) {
            this.date = date;
            this.receiptId = receiptId;
            this.price = price;
        }
    }
}
