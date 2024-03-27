package com.jeannius.lightnovelreader.DialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.jeannius.lightnovelreader.R;

public class SimpleDialogFragment extends DialogFragment {
    private String title;
    private String message;

    public SimpleDialogFragment(String title, String message) {
        this.title = title;
        this.message = message;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.ok, (dialog, id) -> {});
        return builder.create();
    }
}
