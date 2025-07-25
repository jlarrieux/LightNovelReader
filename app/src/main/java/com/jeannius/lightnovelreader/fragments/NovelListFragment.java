package com.jeannius.lightnovelreader.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jeannius.lightnovelreader.MainActivityWithBottomNav;
import com.jeannius.lightnovelreader.R;
import com.jeannius.lightnovelreader.adapter.NovelAdapter;
import com.jeannius.lightnovelreader.database.NovelDatabaseHelper;
import com.jeannius.lightnovelreader.model.Novel;
import com.jeannius.lightnovelreader.utils.CloudBackupManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.jeannius.lightnovelreader.utils.SortPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NovelListFragment extends Fragment implements NovelAdapter.OnNovelClickListener, NovelAdapter.OnNovelLongClickListener {
    
    private static final String ARG_STATUS = "status";
    
    private RecyclerView recyclerView;
    private TextView emptyTextView;
    private ImageButton sortButton;
    private NovelAdapter adapter;
    private NovelDatabaseHelper dbHelper;
    private Novel.Status filterStatus;
    private SortPreferences sortPreferences;
    private SortPreferences.SortType currentSortType;
    private CloudBackupManager cloudBackupManager;
    
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
        sortPreferences = new SortPreferences(getContext());
        cloudBackupManager = new CloudBackupManager(getContext());
        
        // Initialize drive service if user is signed in
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());
        if (account != null) {
            cloudBackupManager.initializeDriveService(account);
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_novel_list, container, false);
        
        recyclerView = view.findViewById(R.id.recycler_view);
        emptyTextView = view.findViewById(R.id.empty_text_view);
        sortButton = view.findViewById(R.id.sort_button);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new NovelAdapter(getContext(), new ArrayList<>());
        adapter.setOnNovelClickListener(this);
        adapter.setOnNovelLongClickListener(this);
        recyclerView.setAdapter(adapter);
        
        // Load saved sort preference
        String tabKey = filterStatus != null ? filterStatus.name() : "ALL";
        currentSortType = sortPreferences.getSortType(tabKey);
        
        // Setup sort button
        sortButton.setOnClickListener(v -> showSortMenu(v));
        
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
            sortNovels(novels);
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
        
        // Use the activity's fragment manager instead of parent fragment manager
        requireActivity().getSupportFragmentManager().beginTransaction()
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
        
        // Backup to cloud after status change
        backupToCloudIfSignedIn();
    }
    
    private void deleteNovel(Novel novel) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Novel")
                .setMessage("Are you sure you want to delete \"" + novel.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteNovel(novel.getUrl());
                    Toast.makeText(getContext(), "Novel deleted", Toast.LENGTH_SHORT).show();
                    loadNovels(); // Refresh the list
                    
                    // Backup to cloud after deletion
                    backupToCloudIfSignedIn();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void showSortMenu(View anchor) {
        PopupMenu popup = new PopupMenu(getContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.menu_sort_options, popup.getMenu());
        
        popup.setOnMenuItemClickListener(item -> {
            SortPreferences.SortType newSortType = null;
            int itemId = item.getItemId();
            
            if (itemId == R.id.sort_alphabetical_asc) {
                newSortType = SortPreferences.SortType.ALPHABETICAL_ASC;
            } else if (itemId == R.id.sort_alphabetical_desc) {
                newSortType = SortPreferences.SortType.ALPHABETICAL_DESC;
            } else if (itemId == R.id.sort_date_added_newest) {
                newSortType = SortPreferences.SortType.DATE_ADDED_NEWEST;
            } else if (itemId == R.id.sort_date_added_oldest) {
                newSortType = SortPreferences.SortType.DATE_ADDED_OLDEST;
            }
            
            if (newSortType != null && newSortType != currentSortType) {
                currentSortType = newSortType;
                String tabKey = filterStatus != null ? filterStatus.name() : "ALL";
                sortPreferences.saveSortType(tabKey, currentSortType);
                loadNovels(); // Reload with new sort
                Toast.makeText(getContext(), "Sorted by " + currentSortType.getDisplayName(), Toast.LENGTH_SHORT).show();
            }
            
            return true;
        });
        
        popup.show();
    }
    
    private void sortNovels(List<Novel> novels) {
        switch (currentSortType) {
            case ALPHABETICAL_ASC:
                Collections.sort(novels, (a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
                break;
            case ALPHABETICAL_DESC:
                Collections.sort(novels, (a, b) -> b.getTitle().compareToIgnoreCase(a.getTitle()));
                break;
            case DATE_ADDED_NEWEST:
                Collections.sort(novels, (a, b) -> Long.compare(b.getDateAdded(), a.getDateAdded()));
                break;
            case DATE_ADDED_OLDEST:
                Collections.sort(novels, (a, b) -> Long.compare(a.getDateAdded(), b.getDateAdded()));
                break;
        }
    }
    
    private void backupToCloudIfSignedIn() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());
        if (account != null) {
            // User is signed in, proceed with backup
            dbHelper.backupToCloud(cloudBackupManager, new CloudBackupManager.BackupCallback() {
                @Override
                public void onSuccess() {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Auto backup successful", Toast.LENGTH_SHORT).show();
                    });
                }
                
                @Override
                public void onError(String error) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "Auto backup failed: " + error, Toast.LENGTH_SHORT).show();
                    });
                }
                
                @Override
                public void onProgress(String message) {
                    // Silent progress for automatic backups
                }
            });
        } else {
            // User not signed in, prompt to sign in
            showCloudBackupSignInDialog();
        }
    }
    
    private void showCloudBackupSignInDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Cloud Backup")
                .setMessage("Sign in to Google Drive to automatically backup your library changes to the cloud?")
                .setPositiveButton("Sign In", (dialog, which) -> {
                    // Navigate to Settings to sign in
                    ((com.jeannius.lightnovelreader.MainActivityWithBottomNav) requireActivity()).navigateToSettings();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    @Override
    public void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        if (cloudBackupManager != null) {
            cloudBackupManager.shutdown();
        }
        super.onDestroy();
    }
}