package com.jeannius.lightnovelreader.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jeannius.lightnovelreader.MainActivityWithBottomNav;
import com.jeannius.lightnovelreader.R;
import com.jeannius.lightnovelreader.adapter.NovelAdapter;
import com.jeannius.lightnovelreader.database.NovelDatabaseHelper;
import com.jeannius.lightnovelreader.model.Novel;

import java.util.ArrayList;
import java.util.List;

public class NovelListFragment extends Fragment implements NovelAdapter.OnNovelClickListener, NovelAdapter.OnNovelLongClickListener {
    
    private static final String ARG_STATUS = "status";
    
    private RecyclerView recyclerView;
    private TextView emptyTextView;
    private NovelAdapter adapter;
    private NovelDatabaseHelper dbHelper;
    private Novel.Status filterStatus;
    
    public static NovelListFragment newInstance(@Nullable Novel.Status status) {
        NovelListFragment fragment = new NovelListFragment();
        Bundle args = new Bundle();
        if (status != null) {
            args.putString(ARG_STATUS, status.name());
        }
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getArguments() != null && getArguments().containsKey(ARG_STATUS)) {
            String statusName = getArguments().getString(ARG_STATUS);
            filterStatus = Novel.Status.valueOf(statusName);
        }
        
        dbHelper = new NovelDatabaseHelper(getContext());
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_novel_list, container, false);
        
        recyclerView = view.findViewById(R.id.recycler_view);
        emptyTextView = view.findViewById(R.id.empty_text_view);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new NovelAdapter(getContext(), new ArrayList<>());
        adapter.setOnNovelClickListener(this);
        adapter.setOnNovelLongClickListener(this);
        recyclerView.setAdapter(adapter);
        
        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadNovels();
    }
    
    private void loadNovels() {
        List<Novel> novels;
        if (filterStatus != null) {
            novels = dbHelper.getNovelsByStatus(filterStatus);
        } else {
            novels = dbHelper.getAllNovels();
        }
        
        if (novels.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyTextView.setVisibility(View.VISIBLE);
            
            if (filterStatus != null) {
                emptyTextView.setText("No " + filterStatus.getDisplayName().toLowerCase() + " novels");
            } else {
                emptyTextView.setText("No novels saved yet.\n\nStart reading a novel from the Reader tab to add it to your library.");
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyTextView.setVisibility(View.GONE);
            adapter.updateNovels(novels);
        }
    }
    
    @Override
    public void onNovelClick(Novel novel) {
        // Navigate to reader with the novel URL
        ReaderFragment readerFragment = new ReaderFragment();
        Bundle args = new Bundle();
        args.putString("url", novel.getUrl());
        readerFragment.setArguments(args);
        
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, readerFragment)
                .addToBackStack(null)
                .commit();
        
        // Update bottom navigation
        if (getActivity() instanceof MainActivityWithBottomNav) {
            ((MainActivityWithBottomNav) getActivity()).navigateToReader();
        }
    }
    
    @Override
    public void onNovelLongClick(Novel novel, View view) {
        showPopupMenu(novel, view);
    }
    
    private void showPopupMenu(Novel novel, View anchor) {
        PopupMenu popup = new PopupMenu(getContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.menu_novel_actions, popup.getMenu());
        
        // Hide current status option
        MenuItem currentStatusItem = null;
        switch (novel.getStatus()) {
            case READING:
                currentStatusItem = popup.getMenu().findItem(R.id.action_move_to_reading);
                break;
            case COMPLETED:
                currentStatusItem = popup.getMenu().findItem(R.id.action_mark_completed);
                break;
            case DROPPED:
                currentStatusItem = popup.getMenu().findItem(R.id.action_move_to_dropped);
                break;
            case PLAN_TO_READ:
                currentStatusItem = popup.getMenu().findItem(R.id.action_plan_to_read);
                break;
        }
        if (currentStatusItem != null) {
            currentStatusItem.setVisible(false);
        }
        
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_mark_completed) {
                updateNovelStatus(novel, Novel.Status.COMPLETED);
                return true;
            } else if (itemId == R.id.action_move_to_dropped) {
                updateNovelStatus(novel, Novel.Status.DROPPED);
                return true;
            } else if (itemId == R.id.action_plan_to_read) {
                updateNovelStatus(novel, Novel.Status.PLAN_TO_READ);
                return true;
            } else if (itemId == R.id.action_move_to_reading) {
                updateNovelStatus(novel, Novel.Status.READING);
                return true;
            } else if (itemId == R.id.action_delete) {
                deleteNovel(novel);
                return true;
            }
            return false;
        });
        
        popup.show();
    }
    
    private void updateNovelStatus(Novel novel, Novel.Status newStatus) {
        dbHelper.updateNovelStatus(novel.getUrl(), newStatus);
        Toast.makeText(getContext(), "Moved to " + newStatus.getDisplayName(), Toast.LENGTH_SHORT).show();
        loadNovels(); // Refresh the list
    }
    
    private void deleteNovel(Novel novel) {
        dbHelper.deleteNovel(novel.getUrl());
        Toast.makeText(getContext(), "Novel deleted", Toast.LENGTH_SHORT).show();
        loadNovels(); // Refresh the list
    }
    
    @Override
    public void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}