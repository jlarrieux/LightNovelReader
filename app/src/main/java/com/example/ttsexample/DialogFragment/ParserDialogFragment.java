package com.example.ttsexample.DialogFragment;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.ttsexample.R;
import com.example.ttsexample.webparser.WebParser;

public class ParserDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder =  new AlertDialog.Builder(getActivity());
        builder.setTitle("Parser Lists")
                .setItems(WebParser.getParserList(), null);
        builder.setNeutralButton(R.string.ok, null);

        return builder.create();
    }
}
