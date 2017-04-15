package com.example.android.pets;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.pets.data.PetContract;

/**
 * Created by terz99 on 4/13/17.
 */

public class PetAdapter extends CursorAdapter{

    /**
     * Public constructor to create an instance of the PetAdapter
     * @param context is the context where the adapter is created from
     * @param cursor is a Cursor object containing all the received datat from the database
     */
    public PetAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    // Overridden method to create and infalte a new item in the list view
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.pet_list_item, viewGroup, false);
    }


    // Overriden method that sets new data to a recycled pet_list_item view
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Get link from the item view text views
        TextView nameTextView = (TextView) view.findViewById(R.id.name_text_view);
        TextView breedTextView = (TextView) view.findViewById(R.id.breed_text_view);

        // Set the appropriate name and breed to this item view
        nameTextView.setText(cursor.getString(
                cursor.getColumnIndex(PetContract.PetEntry.COLUMN_NAME)));
        breedTextView.setText(cursor.getString(
                cursor.getColumnIndex(PetContract.PetEntry.COLUMN_BREED)));
    }
}
