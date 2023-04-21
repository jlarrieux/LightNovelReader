package com.example.ttsexample.DialogFragment;

import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.ttsexample.CustomAdapter;
import com.example.ttsexample.JeanniusLogger;
import com.example.ttsexample.SaverLoaderUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NovelDialogFragment extends DialogFragment {
    private String NOVEL_MAP_FILENAME;
    private EditText urlEditText;
    private EditText fullTextEditText;

    public NovelDialogFragment(String novelMapFilename, EditText urlEditText, EditText fullTextEditText) {
        this.NOVEL_MAP_FILENAME = novelMapFilename;
        this.urlEditText = urlEditText;
        this.fullTextEditText = fullTextEditText;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        HashMap<String, String> novelMap = SaverLoaderUtils.loadNovelMapFromLocal(NOVEL_MAP_FILENAME, getContext());
        JeanniusLogger.log("nOVLES", novelMap.toString());
        builder.setTitle("Novel Maps");
        if (!novelMap.isEmpty()) {
            List<String> items = new ArrayList<>(novelMap.keySet());
            CustomAdapter adapter = new CustomAdapter(getContext(), items);
            ListView listView = new ListView(getContext());
            listView.setAdapter(adapter);
            builder.setView(listView);
            builder.setNegativeButton("Cancel", null);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    JeanniusLogger.log("which clicked", items.get(position));
                    urlEditText.setText(novelMap.get(items.get(position)));
                    fullTextEditText.setText("");
                    dismiss();
                }
            });

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    JeanniusLogger.log("deleting", "about to delete: " + items.get(position));
                    AlertDialog.Builder confirmDeletion = new AlertDialog.Builder(getContext());
                    confirmDeletion.setTitle("Confirm deletion");
                    confirmDeletion.setMessage("About to delete: " + items.get(position));

                    confirmDeletion.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            novelMap.remove(items.get(position));
                            SaverLoaderUtils.saveLocally(novelMap, NOVEL_MAP_FILENAME, parent.getContext());
                            dismiss();
                        }
                    });

                    confirmDeletion.setNegativeButton("Cancel", null);
                    confirmDeletion.create().show();
                    return false;
                }
            });
        }
        return builder.create();
    }
}
