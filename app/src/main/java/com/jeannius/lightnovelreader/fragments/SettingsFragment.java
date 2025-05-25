package com.jeannius.lightnovelreader.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.jeannius.lightnovelreader.DialogFragment.StringSet.BlockedStringDialogFragment;
import com.jeannius.lightnovelreader.DialogFragment.StringSet.FreeWebNovelSynonymsDialogFragment;
import com.jeannius.lightnovelreader.Interface.OnBlockedStringSetUpdatedListener;
import com.jeannius.lightnovelreader.Interface.OnFreeWebNovelSynonymSetUpdatedListener;
import com.jeannius.lightnovelreader.R;
import com.jeannius.lightnovelreader.SaverLoaderUtils;

import java.util.HashSet;
import java.util.Set;

public class SettingsFragment extends Fragment implements OnBlockedStringSetUpdatedListener {
    
    private static final String FREE_WEB_NOVEL_SYNONYMS = "freeWebNovelSynonyms";
    private static final String BLOCKED_STRINGS = "blockedStrings";
    
    private Button blockedStringsButton;
    private Button freeWebNovelSynonymsButton;
    
    private Set<String> freeNovelSynonyms = new HashSet<>();
    private Set<String> blockedStringsSet = new HashSet<>();
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        
        blockedStringsButton = view.findViewById(R.id.blocked_strings_button);
        freeWebNovelSynonymsButton = view.findViewById(R.id.free_web_novel_synonyms_button);
        
        loadData();
        setupButtonListeners();
        
        return view;
    }
    
    private void loadData() {
        freeNovelSynonyms = SaverLoaderUtils.loadSetFromLocal(FREE_WEB_NOVEL_SYNONYMS, getContext());
        blockedStringsSet = SaverLoaderUtils.loadSetFromLocal(BLOCKED_STRINGS, getContext());
    }
    
    private void setupButtonListeners() {
        blockedStringsButton.setOnClickListener(v -> {
            showBlockedStrings();
        });
        
        freeWebNovelSynonymsButton.setOnClickListener(v -> {
            showFreeWebNovelSynonyms();
        });
    }
    
    private void showBlockedStrings() {
        DialogFragment dialogFragment = new BlockedStringDialogFragment(BLOCKED_STRINGS, this, "Blocked Strings");
        dialogFragment.show(getParentFragmentManager(), "BlockedStrings");
    }
    
    private void showFreeWebNovelSynonyms() {
        DialogFragment dialogFragment = new FreeWebNovelSynonymsDialogFragment(FREE_WEB_NOVEL_SYNONYMS, this, "FreeWebNovel Synonyms");
        dialogFragment.show(getParentFragmentManager(), "FreeWebNovelSynonyms");
    }
    
    @Override
    public void reloadSynonyms() {
        freeNovelSynonyms = SaverLoaderUtils.loadSetFromLocal(FREE_WEB_NOVEL_SYNONYMS, getContext());
    }
    
    @Override
    public void reloadBlockedStrings() {
        blockedStringsSet = SaverLoaderUtils.loadSetFromLocal(BLOCKED_STRINGS, getContext());
    }
}