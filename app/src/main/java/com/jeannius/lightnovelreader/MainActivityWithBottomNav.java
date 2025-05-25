package com.jeannius.lightnovelreader;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jeannius.lightnovelreader.databinding.ActivityMainWithBottomNavBinding;
import com.jeannius.lightnovelreader.fragments.LibraryFragment;
import com.jeannius.lightnovelreader.fragments.ParsersFragment;
import com.jeannius.lightnovelreader.fragments.ReaderFragment;
import com.jeannius.lightnovelreader.fragments.SettingsFragment;

public class MainActivityWithBottomNav extends AppCompatActivity {
    
    private ActivityMainWithBottomNavBinding binding;
    private BottomNavigationView bottomNavigationView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityMainWithBottomNavBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        
        bottomNavigationView = binding.bottomNavigation;
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return handleNavigationItemSelected(item);
            }
        });
        
        // Set default fragment
        if (savedInstanceState == null) {
            loadFragment(new ReaderFragment());
        }
    }
    
    private boolean handleNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        
        int itemId = item.getItemId();
        if (itemId == R.id.navigation_reader) {
            fragment = new ReaderFragment();
        } else if (itemId == R.id.navigation_library) {
            fragment = new LibraryFragment();
        } else if (itemId == R.id.navigation_parsers) {
            fragment = new ParsersFragment();
        } else if (itemId == R.id.navigation_settings) {
            fragment = new SettingsFragment();
        }
        
        if (fragment != null) {
            loadFragment(fragment);
            return true;
        }
        
        return false;
    }
    
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    
    public void navigateToReader() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_reader);
    }
}