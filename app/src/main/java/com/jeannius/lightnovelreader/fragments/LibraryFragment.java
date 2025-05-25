package com.jeannius.lightnovelreader.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jeannius.lightnovelreader.DialogFragment.NovelDialogFragment;
import com.jeannius.lightnovelreader.Interface.NovelListActionListener;
import com.jeannius.lightnovelreader.MainActivityWithBottomNav;
import com.jeannius.lightnovelreader.R;
import com.jeannius.lightnovelreader.SaverLoaderUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class LibraryFragment extends Fragment implements NovelListActionListener {
    
    private RecyclerView recyclerView;
    private TextView emptyTextView;
    private static final String NOVEL_MAP_FILE_NAME = "novelMapFileName";
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library, container, false);
        
        recyclerView = view.findViewById(R.id.recycler_view);
        emptyTextView = view.findViewById(R.id.empty_text_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // For now, show the dialog with the novel list
        view.findViewById(R.id.empty_text_view).setOnClickListener(v -> showNovels());
        
        loadNovels();
        
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadNovels();
    }
    
    private void loadNovels() {
        HashMap<String, String> novelMap = SaverLoaderUtils.loadNovelMapFromLocal(NOVEL_MAP_FILE_NAME, getContext());
        
        if (novelMap.isEmpty()) {
            // Show empty state
            recyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
        } else {
            // For now, just show text indicating there are novels
            recyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
            emptyTextView.setText("You have " + novelMap.size() + " novel(s) saved.\n\nTap here to view them.");
            emptyTextView.setOnClickListener(v -> showNovels());
        }
    }
    
    private void showNovels() {
        DialogFragment dialogFragment = new NovelDialogFragment(NOVEL_MAP_FILE_NAME, this);
        dialogFragment.show(getParentFragmentManager(), "NovelDialog");
    }
    
    @Override
    public void onNovelSelected(String url, String chapterPattern) {
        // Navigate to reader fragment with the selected novel
        ReaderFragment readerFragment = new ReaderFragment();
        Bundle args = new Bundle();
        args.putString("url", url);
        readerFragment.setArguments(args);
        
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, readerFragment)
                .addToBackStack(null)
                .commit();
        
        // Update bottom navigation to show Reader tab as selected
        if (getActivity() instanceof MainActivityWithBottomNav) {
            ((MainActivityWithBottomNav) getActivity()).navigateToReader();
        }
    }
}