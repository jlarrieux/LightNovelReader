package com.jeannius.lightnovelreader.DialogFragment.StringSet;


import com.jeannius.lightnovelreader.Interface.OnFreeWebNovelSynonymSetUpdated;

public class FreeWebNovelSynonymsDialogFragment extends StringSetDialogFragment {

    private OnFreeWebNovelSynonymSetUpdated onFreeWebNovelSynonymSetUpdated;

    public FreeWebNovelSynonymsDialogFragment(String filename, OnFreeWebNovelSynonymSetUpdated onFreeWebNovelSynonymSetUpdated, String positiveDialogTitle) {
        super(filename,  positiveDialogTitle);
        this.onFreeWebNovelSynonymSetUpdated = onFreeWebNovelSynonymSetUpdated;
    }

    @Override
    public void reload() {
        this.onFreeWebNovelSynonymSetUpdated.reloadSynonyms();
    }
}
