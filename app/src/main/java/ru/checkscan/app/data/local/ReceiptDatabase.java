package ru.checkscan.app.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ReceiptDatabase extends SQLiteOpenHelper {
    public ReceiptDatabase(Context context) {
        super(context, "receipts.db", null, 1);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE receipts (id INTEGER PRIMARY KEY, qr TEXT NOT NULL UNIQUE, date TEXT"
                        + " NOT NULL, merchant TEXT, total INTEGER NOT NULL)");
        db.execSQL(
                "CREATE TABLE items (id INTEGER PRIMARY KEY, receipt_id INTEGER NOT NULL REFERENCES"
                    + " receipts(id) ON DELETE CASCADE, name TEXT NOT NULL, quantity REAL NOT NULL,"
                    + " price INTEGER NOT NULL, total INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
