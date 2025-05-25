package com.jeannius.lightnovelreader.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.jeannius.lightnovelreader.fragments.NovelListFragment;
import com.jeannius.lightnovelreader.model.Novel;

public class LibraryPagerAdapter extends FragmentStateAdapter {
    
    private static final int NUM_TABS = 5;
    
    public LibraryPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }
    
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return NovelListFragment.newInstance(Novel.Status.READING);
            case 1:
                return NovelListFragment.newInstance(Novel.Status.COMPLETED);
            case 2:
                return NovelListFragment.newInstance(Novel.Status.DROPPED);
            case 3:
                return NovelListFragment.newInstance(Novel.Status.PLAN_TO_READ);
            case 4:
                return NovelListFragment.newInstance(null); // All novels
            default:
                return NovelListFragment.newInstance(Novel.Status.READING);
        }
    }
    
    @Override
    public int getItemCount() {
        return NUM_TABS;
    }
}