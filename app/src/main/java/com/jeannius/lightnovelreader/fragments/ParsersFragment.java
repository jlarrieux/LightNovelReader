package com.jeannius.lightnovelreader.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.jeannius.lightnovelreader.DialogFragment.ParserDialogFragment;
import com.jeannius.lightnovelreader.R;

import java.util.ArrayList;
import java.util.List;

public class ParsersFragment extends Fragment {
    
    private Button showParsersButton;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parsers, container, false);
        
        showParsersButton = view.findViewById(R.id.show_parsers_button);
        
        setupButtonListener();
        
        return view;
    }
    
    private void setupButtonListener() {
        showParsersButton.setOnClickListener(v -> showParsers());
    }
    
    private void showParsers() {
        DialogFragment dialogFragment = new ParserDialogFragment();
        dialogFragment.show(getParentFragmentManager(), "ParserDialog");
    }
}