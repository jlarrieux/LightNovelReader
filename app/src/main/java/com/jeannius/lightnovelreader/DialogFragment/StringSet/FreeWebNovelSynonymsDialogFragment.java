package com.jeannius.lightnovelreader.DialogFragment.StringSet;


import com.jeannius.lightnovelreader.Interface.OnFreeWebNovelSynonymSetUpdatedListener;

public class FreeWebNovelSynonymsDialogFragment extends StringSetDialogFragment {

    private OnFreeWebNovelSynonymSetUpdatedListener onFreeWebNovelSynonymSetUpdatedListener;

    public FreeWebNovelSynonymsDialogFragment(String filename, OnFreeWebNovelSynonymSetUpdatedListener onFreeWebNovelSynonymSetUpdatedListener, String positiveDialogTitle) {
        super(filename,  positiveDialogTitle);
        this.onFreeWebNovelSynonymSetUpdatedListener = onFreeWebNovelSynonymSetUpdatedListener;
    }

    @Override
    public void reload() {
        this.onFreeWebNovelSynonymSetUpdatedListener.reloadSynonyms();
    }
}
