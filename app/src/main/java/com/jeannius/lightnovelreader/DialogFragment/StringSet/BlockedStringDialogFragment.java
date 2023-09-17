package com.jeannius.lightnovelreader.DialogFragment.StringSet;

import com.jeannius.lightnovelreader.Interface.OnBlockedStringSetUpdatedListener;

public class BlockedStringDialogFragment extends StringSetDialogFragment{

    private OnBlockedStringSetUpdatedListener onBlockedStringSetUpdated;

    public BlockedStringDialogFragment(String filename, OnBlockedStringSetUpdatedListener onBlockedStringSetUpdated, String positiveDialogTitle) {
        super(filename,  positiveDialogTitle);
        this.onBlockedStringSetUpdated = onBlockedStringSetUpdated;
    }

    @Override
    public void reload() {
        this.onBlockedStringSetUpdated.reloadBlockedStrings();
    }
}
