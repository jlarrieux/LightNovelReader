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
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.jeannius.lightnovelreader.MainActivityWithBottomNav;
import com.jeannius.lightnovelreader.R;
import com.jeannius.lightnovelreader.adapter.LibraryPagerAdapter;
import com.jeannius.lightnovelreader.adapter.NovelAdapter;
import com.jeannius.lightnovelreader.database.NovelDatabaseHelper;
import com.jeannius.lightnovelreader.model.Novel;

import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment {
    
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private NovelDatabaseHelper dbHelper;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_library_with_tabs, container, false);
        
        viewPager = view.findViewById(R.id.view_pager);
        tabLayout = view.findViewById(R.id.tab_layout);
        
        dbHelper = new NovelDatabaseHelper(getContext());
        
        setupViewPager();
        
        return view;
    }
    
    private void setupViewPager() {
        LibraryPagerAdapter pagerAdapter = new LibraryPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Reading");
                    break;
                case 1:
                    tab.setText("Completed");
                    break;
                case 2:
                    tab.setText("Dropped");
                    break;
                case 3:
                    tab.setText("Plan to Read");
                    break;
                case 4:
                    tab.setText("All");
                    break;
            }
        }).attach();
    }
    
    @Override
    public void onDestroy() {
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}