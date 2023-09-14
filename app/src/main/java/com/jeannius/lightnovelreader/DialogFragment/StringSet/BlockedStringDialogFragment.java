package com.jeannius.lightnovelreader.DialogFragment.StringSet;

import com.jeannius.lightnovelreader.Interface.OnBlockedStringSetUpdated;
import com.jeannius.lightnovelreader.Interface.OnFreeWebNovelSynonymSetUpdated;

public class BlockedStringDialogFragment extends StringSetDialogFragment{

    private OnBlockedStringSetUpdated onBlockedStringSetUpdated;

    public BlockedStringDialogFragment(String filename, OnBlockedStringSetUpdated onBlockedStringSetUpdated, String positiveDialogTitle) {
        super(filename,  positiveDialogTitle);
        this.onBlockedStringSetUpdated = onBlockedStringSetUpdated;
    }

    @Override
    public void reload() {
        this.onBlockedStringSetUpdated.reloadBlockedStrings();
    }
}
