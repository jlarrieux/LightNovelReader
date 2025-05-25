package com.jeannius.lightnovelreader.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SortPreferences {
    
    public enum SortType {
        ALPHABETICAL_ASC("Title (A-Z)"),
        ALPHABETICAL_DESC("Title (Z-A)"),
        DATE_ADDED_NEWEST("Date Added (Newest First)"),
        DATE_ADDED_OLDEST("Date Added (Oldest First)");
        
        private final String displayName;
        
        SortType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private static final String PREFS_NAME = "SortPreferences";
    private static final String KEY_SORT_TYPE = "sort_type";
    private static final String KEY_SORT_TYPE_PREFIX = "sort_type_";
    
    private final SharedPreferences preferences;
    
    public SortPreferences(Context context) {
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    /**
     * Get sort type for a specific status tab
     */
    public SortType getSortType(String tabKey) {
        String key = KEY_SORT_TYPE_PREFIX + tabKey;
        String sortTypeName = preferences.getString(key, SortType.ALPHABETICAL_ASC.name());
        try {
            return SortType.valueOf(sortTypeName);
        } catch (IllegalArgumentException e) {
            return SortType.ALPHABETICAL_ASC;
        }
    }
    
    /**
     * Save sort type for a specific status tab
     */
    public void saveSortType(String tabKey, SortType sortType) {
        String key = KEY_SORT_TYPE_PREFIX + tabKey;
        preferences.edit().putString(key, sortType.name()).apply();
    }
    
    /**
     * Get global sort type (for backwards compatibility)
     */
    public SortType getGlobalSortType() {
        String sortTypeName = preferences.getString(KEY_SORT_TYPE, SortType.ALPHABETICAL_ASC.name());
        try {
            return SortType.valueOf(sortTypeName);
        } catch (IllegalArgumentException e) {
            return SortType.ALPHABETICAL_ASC;
        }
    }
    
    /**
     * Save global sort type
     */
    public void saveGlobalSortType(SortType sortType) {
        preferences.edit().putString(KEY_SORT_TYPE, sortType.name()).apply();
    }
}