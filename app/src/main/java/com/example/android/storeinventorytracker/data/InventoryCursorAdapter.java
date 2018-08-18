package com.example.android.storeinventorytracker.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.storeinventorytracker.R;

public class InventoryCursorAdapter extends CursorAdapter{

    public InventoryCursorAdapter(Context context, Cursor cursor){
        super(context,cursor,0);
    }

    @Override
    public View newView(Context context, Cursor cursor, final ViewGroup viewGroup) {
        //Return a blank item layout to be filled out in the bindView method.
        return LayoutInflater.from(context).inflate(R.layout.list_item,viewGroup,false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView productNameView = view.findViewById(R.id.textView_item_name);
        TextView productQuantityTextView = view.findViewById(R.id.textView_item_quantity_text);
        TextView productQuantityView = view.findViewById(R.id.textView_item_quantity);
        TextView productPriceView = view.findViewById(R.id.textView_item_price);

        final Button sellButton = view.findViewById(R.id.button_sell_item);
        //Use the tag of the button to store the id of the product since all buttons get the same onClick method.
        sellButton.setTag(cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry._ID)));

        final String productName = cursor.getString(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME));
        final int productQuantity = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_QUANTITY));
        int productPrice = cursor.getInt(cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRICE));

        productNameView.setText(productName);
        productPriceView.setText(formatPrice(productPrice));
        if (productQuantity == 0){
           productQuantityTextView.setText(R.string.out_of_stock);
           productQuantityView.setVisibility(View.GONE);
        }else{
            productQuantityTextView.setText(R.string.in_stock);
            productQuantityView.setText(String.valueOf(productQuantity));
        }

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("InventoryCursorAdapter", "onClick: ID OF CLICKED ITEM = "+Integer.valueOf(sellButton.getTag().toString())+" QUANTITY LEFT: "+productQuantity);
                if (productQuantity>0){
                    int rowsUpdated;

                    ContentValues values = new ContentValues();
                    values.put(InventoryContract.InventoryEntry.COLUMN_QUANTITY,productQuantity-1);
                    rowsUpdated = context.getContentResolver().update(
                            ContentUris.withAppendedId(
                                    InventoryContract.InventoryEntry.CONTENT_URI,
                                    Integer.valueOf(sellButton.getTag().toString())),
                            values,
                            null,
                            null);

                    if (rowsUpdated == 0){
                        Toast.makeText(context,R.string.sale_failed,Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(context,R.string.sale_success,Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(context,"Out Of Stock!",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private String formatPrice(float price){
        return "$" + price / 100;
    }
}
