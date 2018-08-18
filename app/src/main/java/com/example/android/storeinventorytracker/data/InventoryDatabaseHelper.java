package com.example.android.storeinventorytracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class InventoryDatabaseHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "store.db";
    private static final int DATABASE_VERSION = 1;

    public InventoryDatabaseHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SQLCreateInventoryTable = "CREATE TABLE "+
                InventoryContract.InventoryEntry.TABLE_NAME+" ( "+
                InventoryContract.InventoryEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+ //Product id automatically generated and incremented
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME+" TEXT NOT NULL, "+ //Required product name
                InventoryContract.InventoryEntry.COLUMN_PRICE+" INTEGER NOT NULL, "+ //Required price stored in cents
                InventoryContract.InventoryEntry.COLUMN_QUANTITY+" INTEGER DEFAULT 0, "+ //Optional quantity in stock; defaults to 0 if not specified
                InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME+ " TEXT NOT NULL, "+ //Required supplier name
                InventoryContract.InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER+" INTEGER );"; //Optional Supplier phone number
        Log.d("InventoryDatabaseHelper", "onCreate: SQL Executed With: "+SQLCreateInventoryTable);
        sqLiteDatabase.execSQL(SQLCreateInventoryTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        //No Upgrade Available Currently.
    }
}
