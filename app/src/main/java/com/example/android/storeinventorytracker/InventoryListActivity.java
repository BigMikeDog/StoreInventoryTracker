package com.example.android.storeinventorytracker;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.example.android.storeinventorytracker.data.InventoryContract;
import com.example.android.storeinventorytracker.data.InventoryCursorAdapter;

public class InventoryListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final int EXISTING_INVENTORY_LOADER = 0;

    InventoryCursorAdapter inventoryCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_list);

        //Make FAB to open the EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab_add_product);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addProduct = new Intent(InventoryListActivity.this,EditorActivity.class);
                startActivity(addProduct);
            }
        });

        //Create a private instance of the CursorAdapter
        inventoryCursorAdapter = new InventoryCursorAdapter(this,null);

        //Setup the ListView
        ListView inventoryList = findViewById(R.id.listView_inventory);
        View emptyInventoryView = findViewById(R.id.empty_view);
        inventoryList.setEmptyView(emptyInventoryView);
        inventoryList.setAdapter(inventoryCursorAdapter);
        //set the onclick listener for the items
        inventoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent openEditView = new Intent(InventoryListActivity.this,EditorActivity.class);
                openEditView.setData(ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI,id));
                startActivity(openEditView);
            }
        });

        //Get a new loader or reconnect with an existing one.
        getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER,null,this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_inventory, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_item_add_dummy_product:{
                insertDummyProduct();
                break;
            }
            case R.id.action_delete_table:{
                deleteTable();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertDummyProduct(){
        //Insert dummy products into the database for testing
        ContentValues dummyValues1 = new ContentValues();
        dummyValues1.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME,"Golden Potato Chips");
        dummyValues1.put(InventoryContract.InventoryEntry.COLUMN_PRICE,12345);
        dummyValues1.put(InventoryContract.InventoryEntry.COLUMN_QUANTITY,5);
        dummyValues1.put(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME,"Target");
        dummyValues1.put(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER,1234567890);

        Uri newUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI,dummyValues1);
        if (newUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, "Test Product Insertion Failed",Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, "Inserted Test Product Successfully",Toast.LENGTH_LONG).show();
        }
    }

    private void deleteTable(){
        int rowsDeleted = getContentResolver().delete(InventoryContract.InventoryEntry.CONTENT_URI,null,null);
        Log.d("InventoryListActivity", "deleteTable: Deleted "+rowsDeleted+" row(s)");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String projection[] = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.InventoryEntry.COLUMN_PRICE,
                InventoryContract.InventoryEntry.COLUMN_QUANTITY,
                InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME,
                InventoryContract.InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER
        };
        return new CursorLoader(this, InventoryContract.InventoryEntry.CONTENT_URI,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Swap the new cursor in. (The framework will take care of closing the old cursor once we return.)
        inventoryCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished() above is about to be closed. We need to make sure we are no longer using it.
        inventoryCursorAdapter.swapCursor(null);
    }
}