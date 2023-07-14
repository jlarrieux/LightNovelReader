package com.jeannius.lightnovelreader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class CustomAdapter extends ArrayAdapter {
    List<String> items;

    public CustomAdapter(Context context, List<String> items){
        super(context, 0,  items);
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_layout, parent, false);
        }

        TextView itemText = convertView.findViewById(R.id.item_text);
        itemText.setText(items.get(position));

        return convertView;
    }
}
