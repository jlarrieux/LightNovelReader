package com.example.ttsexample.DialogFragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.ttsexample.JeanniusLogger;
import com.example.ttsexample.SaverLoaderUtils;

import java.util.HashMap;

public class NovelDialogFragment extends DialogFragment {
    private String NOVEL_MAP_FILENAME;
    private EditText urlEditText;
    private EditText fullTextEditText;

    public NovelDialogFragment(String novelMapFilename, EditText urlEditText, EditText fullTextEditText){
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
        if(!novelMap.isEmpty()){
            CharSequence[] keyCharSequence = novelMap.keySet().toArray(new String[0]);
            builder.setItems(keyCharSequence, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    JeanniusLogger.log("which clicked", String.valueOf(keyCharSequence[which]));
                    urlEditText.setText(novelMap.get(keyCharSequence[which]));
                    fullTextEditText.setText("");
                }
            });
        }
        return builder.create();
    }
}
