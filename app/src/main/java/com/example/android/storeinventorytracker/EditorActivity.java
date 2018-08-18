package com.example.android.storeinventorytracker;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.example.android.storeinventorytracker.data.InventoryContract;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private EditText productNameView;
    private EditText productPriceView;
    private EditText productQuantityView;
    private EditText supplierNameView;
    private EditText supplierPhoneNumberView;

    private Uri mCurrentProductUri;

    private boolean mProductHasChanged;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged =true;
            return false;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        productNameView = findViewById(R.id.editText_product_name);
        productPriceView = findViewById(R.id.editText_product_price);
        productQuantityView = findViewById(R.id.editText_product_quantity);
        ImageButton incrementQuantityButton = findViewById(R.id.imageButton_add_product_quantity);
        ImageButton decrementQuantityButton = findViewById(R.id.imageButton_remove_product_quantity);
        supplierNameView = findViewById(R.id.editText_supplier_name);
        supplierPhoneNumberView = findViewById(R.id.editText_supplier_phone);

        //Set onTouchListener for all things that edit information
        productNameView.setOnTouchListener(mTouchListener);
        productPriceView.setOnTouchListener(mTouchListener);
        productQuantityView.setOnTouchListener(mTouchListener);
        incrementQuantityButton.setOnTouchListener(mTouchListener);
        decrementQuantityButton.setOnTouchListener(mTouchListener);
        supplierNameView.setOnTouchListener(mTouchListener);
        supplierPhoneNumberView.setOnTouchListener(mTouchListener);

        incrementQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (productQuantityView.getText().toString().equals("")){
                    productQuantityView.setText(R.string.value_one);
                    return;
                }
                int quantity = Integer.valueOf(productQuantityView.getText().toString())+1;
                productQuantityView.setText(String.valueOf(quantity));
            }
        });
        decrementQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (productQuantityView.getText().toString().equals("")){
                    productQuantityView.setText(R.string.value_zero);
                }else if (Integer.valueOf(productQuantityView.getText().toString())>=1){
                    int quantity = Integer.valueOf(productQuantityView.getText().toString())-1;
                    productQuantityView.setText(String.valueOf(quantity));
                }else{
                    Toast.makeText(getApplicationContext(),R.string.negative_quantity_error,Toast.LENGTH_LONG).show();
                }
            }
        });

        mCurrentProductUri = getIntent().getData();
        if (mCurrentProductUri!=null){
            setTitle(getString(R.string.edit_product_title));
            invalidateOptionsMenu();
            Log.d("EditorActivity", "onCreate: Received Uri = "+mCurrentProductUri.toString());
            //Get a new loader or reconnect with an existing one.
            getLoaderManager().initLoader(InventoryListActivity.EXISTING_INVENTORY_LOADER,null,this);
        }else{
            setTitle(getString(R.string.new_product_title));
            Log.d("EditorActivity","onCreate: No URI passed.");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentProductUri==null){
            MenuItem deleteItem = menu.findItem(R.id.action_delete_product);
            MenuItem callItem = menu.findItem(R.id.action_call_supplier);
            deleteItem.setVisible(false);
            callItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save_product:{
                saveProduct();
                return true;
            }
            case R.id.action_delete_product:{
                showDeleteConfirmationDialog();
                return true;
            }
            case R.id.action_call_supplier:{
                Intent callSupplier = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+supplierPhoneNumberView.getText().toString().trim()));
                if (callSupplier.resolveActivity(getPackageManager())!=null){
                    startActivity(callSupplier);
                }else{
                    Toast.makeText(this, R.string.cant_call,Toast.LENGTH_LONG).show();
                }
                return true;
            }
            case android.R.id.home:{
                if (!mProductHasChanged){
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveProduct(){
        if (
                TextUtils.isEmpty(productNameView.getText().toString().trim())||
                TextUtils.isEmpty(productPriceView.getText().toString().trim())||
                TextUtils.isEmpty(supplierNameView.getText().toString().trim())||
                TextUtils.isEmpty(supplierPhoneNumberView.getText().toString().trim())){
            //If this is a new item and a required field is empty then don't save and return
            Toast.makeText(this, R.string.missing_field,Toast.LENGTH_LONG).show();
            return;
        }else if (TextUtils.isEmpty(productQuantityView.getText().toString().trim())){
            //If it's ok to save but there is no quantity then make it a zero and continue saving
            productQuantityView.setText(R.string.value_zero);
        }

        String phone = supplierPhoneNumberView.getText().toString().trim();
        StringBuilder sb = new StringBuilder();
        sb.append(phone);
        for (int i=0;i<sb.length();i++){
            String c = String.valueOf(sb.charAt(i));
            if (!c.equals("0")
                    &&!c.equals("1")
                    &&!c.equals("2")
                    &&!c.equals("3")
                    &&!c.equals("4")
                    &&!c.equals("5")
                    &&!c.equals("6")
                    &&!c.equals("7")
                    &&!c.equals("8")
                    &&!c.equals("9"))
            {
                Toast.makeText(this,getString(R.string.numbers_only),Toast.LENGTH_LONG).show();
                return;
            }
        }

        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME,productNameView.getText().toString().trim());
        values.put(InventoryContract.InventoryEntry.COLUMN_PRICE,Float.valueOf(productPriceView.getText().toString().trim())*100);
        values.put(InventoryContract.InventoryEntry.COLUMN_QUANTITY,Integer.valueOf(productQuantityView.getText().toString().trim()));
        values.put(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME,supplierNameView.getText().toString().trim());
        values.put(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER,Integer.valueOf(supplierPhoneNumberView.getText().toString().trim()));

        if (mCurrentProductUri==null){
            //Create new table row
            Uri newUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI,values);
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, R.string.saving_failed,Toast.LENGTH_LONG).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, R.string.product_saved,Toast.LENGTH_LONG).show();
                finish();
            }
        }else{
            //Update existing product
            int rowsUpdated = getContentResolver().update(mCurrentProductUri,values,null,null);
            if (rowsUpdated==0){
                Toast.makeText(this, R.string.update_failed,Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, R.string.product_updated,Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void deleteProduct(){
        int rowsDeleted = getContentResolver().delete(mCurrentProductUri,null,null);
        if (rowsDeleted==0){
            Toast.makeText(this,"Product Deletion Failed", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this,"Product Deleted",Toast.LENGTH_LONG).show();
        }
        finish();
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
        return new CursorLoader(this,mCurrentProductUri,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()){
            productNameView.setText(cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME)));
            productPriceView.setText(String.valueOf(cursor.getFloat(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRICE))/100));
            productQuantityView.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_QUANTITY))));
            supplierNameView.setText(cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME)));
            supplierPhoneNumberView.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_PHONE_NUMBER))));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        productNameView.setText(null);
        productPriceView.setText(null);
        productQuantityView.setText(null);
        supplierNameView.setText(null);
        supplierPhoneNumberView.setText(null);
    }

    @Override
    public void onBackPressed() {
        if (!mProductHasChanged){
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface !=null){
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_product_dialog);
        builder.setPositiveButton(R.string.delete_confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
