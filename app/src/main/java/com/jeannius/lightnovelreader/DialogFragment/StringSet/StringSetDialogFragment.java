package com.jeannius.lightnovelreader.DialogFragment.StringSet;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.jeannius.lightnovelreader.CustomAdapter;
import com.jeannius.lightnovelreader.JeanniusLogger;
import com.jeannius.lightnovelreader.SaverLoaderUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class StringSetDialogFragment extends DialogFragment {

    private final String FILENAME;
    private final String POSITIVE_DIALOG_TITLE;

    public abstract void reload();

    protected StringSetDialogFragment(String filename, String positiveDialogTitle){
        this.FILENAME = filename;
        this.POSITIVE_DIALOG_TITLE = positiveDialogTitle;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        Set<String> stringSet = SaverLoaderUtils.loadSetFromLocal(FILENAME, getContext());
        JeanniusLogger.log(FILENAME , stringSet.toString());
        builder.setTitle(FILENAME);
        List<String> items = new ArrayList<>();
        if(!stringSet.isEmpty()){
            items = new ArrayList<>(stringSet);
        }
        CustomAdapter adapter = new CustomAdapter(getContext(), items);
        ListView listView = new ListView(getContext());
        listView.setAdapter(adapter);
        builder.setView(listView);
        setNegativeButton(builder);
        setPositiveButton(builder, stringSet, items, adapter);
        if(!stringSet.isEmpty()){
            setItemLongClickListener(listView, stringSet, items);
        }
        return builder.create();
    }

    private void setNegativeButton(androidx.appcompat.app.AlertDialog.Builder builder) {
        builder.setNegativeButton("Cancel", null);
    }

    private void setPositiveButton(AlertDialog.Builder builder,
                                   Set<String> stringSet,
                                   List<String> items,
                                   CustomAdapter adapter){
        builder.setPositiveButton("Add", (dialog, which) -> {
            Context context = requireContext();
            EditText input = new EditText(context);
            new AlertDialog.Builder(context)
                    .setTitle(POSITIVE_DIALOG_TITLE)
                    .setView(input)
                    .setPositiveButton("OK", (dialog1, which1) -> {
                        String newItem = input.getText().toString();
                        stringSet.add(newItem);
                        SaverLoaderUtils.saveLocally(stringSet, FILENAME, context );
                        items.add(newItem);
                        adapter.notifyDataSetChanged();
                        reload();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void setItemLongClickListener(ListView listView, Set<String> set, List<String> items) {
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            String message = String.format("about to delete: %s", items.get(position));
            JeanniusLogger.log("deleting", message);
            AlertDialog.Builder confirmDeletion = new AlertDialog.Builder(requireContext());
            confirmDeletion.setTitle("Confirm Deletion");
            confirmDeletion.setMessage(message);

            confirmDeletion.setPositiveButton("Delete", (dialog, which) -> {
                set.remove(items.get(position));
                SaverLoaderUtils.saveLocally(set, FILENAME, parent.getContext());
                reload();
                dismiss();
            });

            setNegativeButton(confirmDeletion);
            confirmDeletion.create().show();
            return false;
        });
    }
}
