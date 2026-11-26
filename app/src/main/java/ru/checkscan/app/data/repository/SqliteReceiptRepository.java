package ru.checkscan.app.data.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import ru.checkscan.app.data.local.ReceiptDatabase;
import ru.checkscan.app.domain.model.ProductStats;
import ru.checkscan.app.domain.model.Receipt;
import ru.checkscan.app.domain.model.ReceiptItem;
import ru.checkscan.app.domain.repository.ReceiptRepository;

import java.util.ArrayList;
import java.util.List;

public class SqliteReceiptRepository implements ReceiptRepository {
    private final ReceiptDatabase database;

    public SqliteReceiptRepository(ReceiptDatabase database) {
        this.database = database;
    }

    @Override
    public boolean exists(String qr) {
        try (Cursor c =
                database.getReadableDatabase()
                        .rawQuery("SELECT 1 FROM receipts WHERE qr=?", new String[] {qr})) {
            return c.moveToFirst();
        }
    }

    @Override
    public void save(Receipt receipt) {
        SQLiteDatabase db = database.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL(
                    "INSERT INTO receipts(qr,date,merchant,total) VALUES(?,?,?,?)",
                    new Object[] {receipt.qr, receipt.date, receipt.merchant, receipt.total});
            long id;
            try (Cursor c = db.rawQuery("SELECT last_insert_rowid()", null)) {
                c.moveToFirst();
                id = c.getLong(0);
            }
            for (ReceiptItem item : receipt.items)
                db.execSQL(
                        "INSERT INTO items(receipt_id,name,quantity,price,total) VALUES(?,?,?,?,?)",
                        new Object[] {id, item.name, item.quantity, item.price, item.total});
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private List<ReceiptItem> items(long id) {
        List<ReceiptItem> result = new ArrayList<>();
        try (Cursor c =
                database.getReadableDatabase()
                        .rawQuery(
                                "SELECT name,quantity,price,total FROM items WHERE receipt_id=?"
                                        + " ORDER BY id",
                                new String[] {String.valueOf(id)})) {
            while (c.moveToNext())
                result.add(
                        new ReceiptItem(
                                c.getString(0), c.getDouble(1), c.getLong(2), c.getLong(3)));
        }
        return result;
    }

    @Override
    public List<Receipt> getReceipts() {
        List<Receipt> result = new ArrayList<>();
        try (Cursor c =
                database.getReadableDatabase()
                        .rawQuery(
                                "SELECT id,qr,date,merchant,total FROM receipts ORDER BY date"
                                        + " DESC,id DESC",
                                null)) {
            while (c.moveToNext()) {
                long id = c.getLong(0);
                result.add(
                        new Receipt(
                                id,
                                c.getString(1),
                                c.getString(2),
                                c.getString(3),
                                c.getLong(4),
                                items(id)));
            }
        }
        return result;
    }

    @Override
    public Receipt getReceipt(long id) {
        try (Cursor c =
                database.getReadableDatabase()
                        .rawQuery(
                                "SELECT qr,date,merchant,total FROM receipts WHERE id=?",
                                new String[] {String.valueOf(id)})) {
            return c.moveToFirst()
                    ? new Receipt(
                            id,
                            c.getString(0),
                            c.getString(1),
                            c.getString(2),
                            c.getLong(3),
                            items(id))
                    : null;
        }
    }

    @Override
    public void delete(long id) {
        database.getWritableDatabase()
                .delete("receipts", "id=?", new String[] {String.valueOf(id)});
    }

    @Override
    public List<ProductStats> getProductStats() {
        List<ProductStats> result = new ArrayList<>();
        try (Cursor c =
                database.getReadableDatabase()
                        .rawQuery(
                                "SELECT name,SUM(total),MIN(price),MAX(price),AVG(price),COUNT(*)"
                                        + " FROM items GROUP BY name ORDER BY SUM(total) DESC",
                                null)) {
            while (c.moveToNext())
                result.add(
                        new ProductStats(
                                c.getString(0),
                                c.getLong(1),
                                c.getLong(2),
                                c.getLong(3),
                                Math.round(c.getDouble(4)),
                                c.getInt(5),
                                new ArrayList<>()));
        }
        return result;
    }

    @Override
    public ProductStats getProductStats(String name) {
        List<ProductStats.PriceEntry> history = new ArrayList<>();
        try (Cursor c =
                database.getReadableDatabase()
                        .rawQuery(
                                "SELECT receipts.date,items.price,items.quantity,receipts.merchant"
                                    + " FROM items JOIN receipts ON receipts.id=items.receipt_id"
                                    + " WHERE items.name=? ORDER BY receipts.date DESC,items.id"
                                    + " DESC",
                                new String[] {name})) {
            while (c.moveToNext())
                history.add(
                        new ProductStats.PriceEntry(
                                c.getString(0), c.getLong(1), c.getDouble(2), c.getString(3)));
        }
        try (Cursor c =
                database.getReadableDatabase()
                        .rawQuery(
                                "SELECT SUM(total),MIN(price),MAX(price),AVG(price),COUNT(*) FROM"
                                        + " items WHERE name=?",
                                new String[] {name})) {
            if (!c.moveToFirst() || c.getInt(4) == 0) return null;
            return new ProductStats(
                    name,
                    c.getLong(0),
                    c.getLong(1),
                    c.getLong(2),
                    Math.round(c.getDouble(3)),
                    c.getInt(4),
                    history);
        }
    }
}
