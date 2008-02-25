package com.life.alerts;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBHelper {
    class Row extends Object {
        public long rowId;
        public String name;
        public String phoneNumber;
    }

    private static final String DATABASE_CREATE =
        "create table contacts (rowid integer primary key autoincrement, "
            + "name text not null, phoneNumber text not null);";

    private static final String DATABASE_NAME = "phoneData";

    private static final String DATABASE_TABLE = "contacts";

    private static final int DATABASE_VERSION = 1;

    private SQLiteDatabase db;

    public DBHelper(Context ctx) {
        try {
            db = ctx.openDatabase(DATABASE_NAME, null);
        } catch (FileNotFoundException e) {
            try {
                db =
                    ctx.createDatabase(DATABASE_NAME, DATABASE_VERSION, 0,
                        null);
                db.execSQL(DATABASE_CREATE);
            } catch (FileNotFoundException e1) {
                db = null;
            }
        }
    }

    public void close() {
        db.close();
    }

    public void createRow(String name, String phoneNumber) {
        ContentValues initialValues = new ContentValues();
        initialValues.put("name", name);
        initialValues.put("phoneNumber", phoneNumber);
        db.insert(DATABASE_TABLE, null, initialValues);
    }

    public void deleteRow(long rowId) {
        db.delete(DATABASE_TABLE, "rowid=" + rowId, null);
    }

    public List<Row> fetchAllRows() {
        ArrayList<Row> ret = new ArrayList<Row>();
        try {
            Cursor c =
                db.query(DATABASE_TABLE, new String[] {
                    "rowid", "name", "phoneNumber"}, null, null, null, null, null);
            int numRows = c.count();
            c.first();
            for (int i = 0; i < numRows; ++i) {
                Row row = new Row();
                row.rowId = c.getLong(0);
                row.name = c.getString(1);
                row.phoneNumber = c.getString(2);
                ret.add(row);
                c.next();
            }
        } catch (SQLException e) {
            Log.e("Can't fetch all contact rows from DB", e.toString());
        }
        return ret;
    }

    public Row fetchRow(long rowId) {
        Row row = new Row();
        Cursor c =
            db.query(true, DATABASE_TABLE, new String[] {
                "rowid", "name", "phoneNumber"}, "rowid=" + rowId, null, null,
                null, null);
        if (c.count() > 0) {
            c.first();
            row.rowId = c.getLong(0);
            row.name = c.getString(1);
            row.phoneNumber = c.getString(2);
            return row;
        } else {
            row.rowId = -1;
            row.phoneNumber = row.name = null;
        }
        return row;
    }

    public void updateRow(long rowId, String name, String phoneNumber) {
        ContentValues args = new ContentValues();
        args.put("name", name);
        args.put("phoneNumber", phoneNumber);
        db.update(DATABASE_TABLE, args, "rowid=" + rowId, null);
    }
}


