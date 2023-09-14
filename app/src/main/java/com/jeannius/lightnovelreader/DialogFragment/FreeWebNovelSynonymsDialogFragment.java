package com.jeannius.lightnovelreader.DialogFragment;


import androidx.appcompat.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.jeannius.lightnovelreader.CustomAdapter;
import com.jeannius.lightnovelreader.Interface.OnFreeWebNovelSynonymsUpdated;
import com.jeannius.lightnovelreader.JeanniusLogger;
import com.jeannius.lightnovelreader.SaverLoaderUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class FreeWebNovelSynonymsDialogFragment extends DialogFragment {

    private final String SYNONYMS_SET_FILENAME;
    private OnFreeWebNovelSynonymsUpdated onFreeWebNovelSynonymsUpdated;

    public FreeWebNovelSynonymsDialogFragment(String synonymsSetFileName, OnFreeWebNovelSynonymsUpdated onFreeWebNovelSynonymsUpdated){
        this.SYNONYMS_SET_FILENAME = synonymsSetFileName;
        this.onFreeWebNovelSynonymsUpdated = onFreeWebNovelSynonymsUpdated;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        Set<String> freeWebNovelSynonyms = SaverLoaderUtils.loadSetFromLocal(SYNONYMS_SET_FILENAME, getContext());
        JeanniusLogger.log("FreeWebNovelsSynonyms" , freeWebNovelSynonyms.toString());
        builder.setTitle("FreeWebNovel Synonyms");
        List<String> items = new ArrayList<>();
        if(!freeWebNovelSynonyms.isEmpty()){
            items = new ArrayList<>(freeWebNovelSynonyms);
        }
        CustomAdapter adapter = new CustomAdapter(getContext(), items);
        ListView listView = new ListView(getContext());
        listView.setAdapter(adapter);
        builder.setView(listView);
        setNegativeButton(builder);
        setPositiveButton(builder, freeWebNovelSynonyms, items, adapter);
        if(!freeWebNovelSynonyms.isEmpty()){
            setItemLongClickListener(listView, freeWebNovelSynonyms, items);
        }
        return builder.create();
    }

    private void setNegativeButton(androidx.appcompat.app.AlertDialog.Builder builder) {
        builder.setNegativeButton("Cancel", null);
    }

    private void setPositiveButton(AlertDialog.Builder builder,
                                   Set<String> freeWebNovelSynonyms,
                                   List<String> items,
                                   CustomAdapter adapter){
        builder.setPositiveButton("Add", (dialog, which) -> {
            Context context = requireContext();
            EditText input = new EditText(context);
            new AlertDialog.Builder(context)
                    .setTitle("Add a new synonym")
                    .setView(input)
                    .setPositiveButton("OK", (dialog1, which1) -> {
                        String newItem = input.getText().toString();
                        freeWebNovelSynonyms.add(newItem);
                        SaverLoaderUtils.saveLocally(freeWebNovelSynonyms, SYNONYMS_SET_FILENAME, context );
                        items.add(newItem);
                        adapter.notifyDataSetChanged();
                        onFreeWebNovelSynonymsUpdated.reloadSynonyms();
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
                SaverLoaderUtils.saveLocally(set, SYNONYMS_SET_FILENAME, parent.getContext());
                onFreeWebNovelSynonymsUpdated.reloadSynonyms();
                dismiss();
            });

            setNegativeButton(confirmDeletion);
            confirmDeletion.create().show();
            return false;
        });
    }
}
