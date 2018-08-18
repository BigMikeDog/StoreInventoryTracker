package com.example.android.storeinventorytracker.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class InventoryProvider extends ContentProvider {

    private static final int INVENTORY = 100;
    private static final int INVENTORY_ID = 101;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY,InventoryContract.PATH_INVENTORY,INVENTORY);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY,InventoryContract.PATH_INVENTORY+"/#",INVENTORY_ID);
    }

    InventoryDatabaseHelper mInventoryDatabaseHelper;

    @Override
    public boolean onCreate() {
        mInventoryDatabaseHelper = new InventoryDatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri,
                        @Nullable String[] projection,
                        @Nullable String selection,
                        @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        SQLiteDatabase db = mInventoryDatabaseHelper.getReadableDatabase();
        Cursor cursor;

        switch (sUriMatcher.match(uri)){
            case INVENTORY:{
                cursor = db.query(InventoryContract.InventoryEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            }
            case INVENTORY_ID:{
                selection = InventoryContract.InventoryEntry._ID+"=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                cursor = db.query(InventoryContract.InventoryEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            }
            default:{
                throw new IllegalArgumentException("Can not query unknown URI: "+uri);
            }
        }
        //Set notification URI for the cursor
        //so we know what content URI the cursor was created for
        //If the data at this URI changes then we know we need to update the cursor
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        switch (sUriMatcher.match(uri)){
            case INVENTORY:{
                return insertProduct(uri,contentValues);
            }
            default:{
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
            }
        }
    }

    private Uri insertProduct(Uri uri, ContentValues contentValues){
        SQLiteDatabase db = mInventoryDatabaseHelper.getWritableDatabase();

        String productName = contentValues.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
        Integer price = contentValues.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRICE);
        String supplierName = contentValues.getAsString(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME);
        Integer supplierPhone = contentValues.getAsInteger(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

        if (productName == null){
            throw new IllegalArgumentException("Product Requires A Name!");
        }
        if (price == null){
            throw new IllegalArgumentException("Product Requires A Price!");
        }
        if (supplierName == null){
            throw new IllegalArgumentException("Product Requires Supplier Name!");
        }
        if (supplierPhone == null){
            throw new IllegalArgumentException("Product Requires Supplier Phone Number!");
        }

        long id = db.insert(InventoryContract.InventoryEntry.TABLE_NAME,null,contentValues);
        if (id == -1){
            Log.e("InventoryProvider", "Failed to insert row for: "+uri);
            return null;
        }

        //Notify all listeners that the data has changed for the inventory content uri
        getContext().getContentResolver().notifyChange(uri,null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(@NonNull Uri uri,ContentValues contentValues,@Nullable String selection,@Nullable String[] selectionArgs) {
        switch (sUriMatcher.match(uri)){
            case INVENTORY:{
                return updateInventory(uri,contentValues,selection,selectionArgs);
            }
            case INVENTORY_ID:{
                selection = InventoryContract.InventoryEntry._ID+"=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return updateInventory(uri,contentValues,selection,selectionArgs);
            }
            default: {
                throw new IllegalArgumentException("Update is not supported for " + uri);
            }
        }
    }

    private int updateInventory(Uri uri,ContentValues values,String selection,String[] selectionArgs){
        SQLiteDatabase db = mInventoryDatabaseHelper.getWritableDatabase();

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME)){
            String productName = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
            if (productName == null){
                throw new IllegalArgumentException("Product Requires A Name!");
            }
        }
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRICE)){
            String price = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRICE);
            if (price == null){
                throw new IllegalArgumentException("Product Requires A Price!");
            }
        }
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME)){
            String supplierName = values.getAsString(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME);
            if (supplierName == null){
                throw new IllegalArgumentException("Product Requires A Supplier Name!");
            }
        }

        int rowsUpdated = db.update(InventoryContract.InventoryEntry.TABLE_NAME,values,selection,selectionArgs);
        if (rowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        SQLiteDatabase db = mInventoryDatabaseHelper.getWritableDatabase();

        int rowsDeleted;

        switch (sUriMatcher.match(uri)){
            case INVENTORY:{
                rowsDeleted = db.delete(InventoryContract.InventoryEntry.TABLE_NAME,selection,selectionArgs);
                break;
            }
            case INVENTORY_ID:{
                selection = InventoryContract.InventoryEntry._ID+"=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(InventoryContract.InventoryEntry.TABLE_NAME,selection,selectionArgs);
                break;
            }
            default:{
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
            }
        }
        if (rowsDeleted!=0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)){
            case INVENTORY:{
                return InventoryContract.InventoryEntry.CONTENT_LIST_TYPE;
            }
            case INVENTORY_ID:{
                return InventoryContract.InventoryEntry.CONTENT_ITEM_TYPE;
            }
            default:{
                throw new IllegalStateException("Unknown URI "+uri+" with match "+sUriMatcher.match(uri));
            }
        }
    }
}
