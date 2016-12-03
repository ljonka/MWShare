package de.rosapavian.mwshare;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/**
 * Created by leonid on 26.11.16.
 */

public class PageAdapter extends CursorAdapter {

    public PageAdapter(Context context, Cursor c,
                             int flags) {
        super(context, c, flags);
        //sync pages store

    }
    @Override
    public View newView(Context context, Cursor cursor,
                        ViewGroup parent) {
        return LayoutInflater.from(context).inflate(
                android.R.layout.simple_list_item_1,parent,
                false);
    }
    @Override
    public void bindView(View view, Context context, Cursor
            cursor) {
        TextView textView = (TextView)view.findViewById(
                android.R.id.text1);
        textView.setText(cursor.getString(
                getCursor().getColumnIndex("title")));
    }
}
