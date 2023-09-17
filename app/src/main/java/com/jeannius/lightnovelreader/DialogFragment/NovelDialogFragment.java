package com.jeannius.lightnovelreader.DialogFragment;

import android.app.Dialog;
import android.os.Bundle;

import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.jeannius.lightnovelreader.Interface.NovelListActionListener;
import com.jeannius.lightnovelreader.JeanniusLogger;
import com.jeannius.lightnovelreader.SaverLoaderUtils;
import com.jeannius.lightnovelreader.CustomAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class NovelDialogFragment extends DialogFragment {
    private String novelMapFilename;
    private NovelListActionListener listener;



    public NovelDialogFragment(String novelMapFilename, NovelListActionListener listener) {
        this.novelMapFilename = novelMapFilename;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        HashMap<String, String> novelMap = SaverLoaderUtils.loadNovelMapFromLocal(novelMapFilename, getContext());
        JeanniusLogger.log("novels", novelMap.toString());
        builder.setTitle("Novel Maps");
        if (!novelMap.isEmpty()) {
            List<String> items = new ArrayList<>(novelMap.keySet());
            Collections.sort(items);
            CustomAdapter adapter = new CustomAdapter(getContext(), items);
            ListView listView = new ListView(getContext());
            listView.setAdapter(adapter);
            builder.setView(listView);
            setNegativeButton(builder);
            setItemClickListener(listView, novelMap, items);
            setItemLongClickListener(listView, novelMap, items);
        }
        return builder.create();
    }

    private void setNegativeButton(AlertDialog.Builder builder) {
        builder.setNegativeButton("Cancel", null);
    }

    private void setItemClickListener(ListView listView, HashMap<String, String> novelMap, List<String> items) {
        listView.setOnItemClickListener((parent, view, position, id) -> {
            JeanniusLogger.log("which clicked", items.get(position));
            listener.onNovelSelected(novelMap.get(items.get(position)), "");
            dismiss();
        });
    }

    private void setItemLongClickListener(ListView listView, HashMap<String, String> novelMap, List<String> items) {
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            JeanniusLogger.log("deleting", "about to delete: " + items.get(position));
            AlertDialog.Builder confirmDeletion = new AlertDialog.Builder(requireContext());
            confirmDeletion.setTitle("Confirm deletion");
            confirmDeletion.setMessage("About to delete: " + items.get(position));

            confirmDeletion.setPositiveButton("Delete", (dialog, which) -> {
                novelMap.remove(items.get(position));
                SaverLoaderUtils.saveLocally(novelMap, novelMapFilename, parent.getContext());
                dismiss();
            });

            setNegativeButton(confirmDeletion);
            confirmDeletion.create().show();
            return false;
        });
    }
}
