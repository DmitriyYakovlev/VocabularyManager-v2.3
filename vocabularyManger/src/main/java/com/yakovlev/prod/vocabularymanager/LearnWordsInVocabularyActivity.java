package com.yakovlev.prod.vocabularymanager;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.yakovlev.prod.vocabularymanager.adapters.LearnWordsCursorAdapter;
import com.yakovlev.prod.vocabularymanager.constants.Const;
import com.yakovlev.prod.vocabularymanager.cursor_loaders.HardWordsCursorLoader;
import com.yakovlev.prod.vocabularymanager.cursor_loaders.WordsCursorLoader;
import com.yakovlev.prod.vocabularymanager.dialogs.AlertDialogsHolder;
import com.yakovlev.prod.vocabularymanager.dialogs.DialogAskCallback;
import com.yakovlev.prod.vocabularymanager.support.SharedPreferencesHelper;
import com.yakovlev.prod.vocabularymanager.support.ToastHelper;
import com.yakovlev.prod.vocabularymanger.R;

public class LearnWordsInVocabularyActivity extends FragmentActivity implements
        BaseActivityStructure, LoaderManager.LoaderCallbacks<Cursor>, DialogAskCallback {

	private LearnWordsCursorAdapter adapter;
    private ImageButton btnSwitchMode;
    private ListView listContent;
    private TextView tvHeader;
    private int vocabularyId;
    private CursorAdapter baseCursorAdapter;
    private boolean isOpenHardWordsModeActive = false;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_learn_select_vocab);

        Intent intent = getIntent();
        vocabularyId = intent.getIntExtra(Const.VOCAB_ID, -1);
        if (vocabularyId == Const.OPEN_LEARN_WORDS_ACTIVITY_FOR_HARD_WORDS){
            isOpenHardWordsModeActive = true;
        }


        processLastVocabularyIdFromPreferences(vocabularyId);

        findAllViews();
        setOnClickListeners();
        tvHeader.setText("Learn");

        listContent = (ListView) findViewById(R.id.lvVocabs);
        getSupportLoaderManager().initLoader(0, null, this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (baseCursorAdapter != null) {
            baseCursorAdapter.notifyDataSetChanged();
            listContent.refreshDrawableState();
        }
        btnSwitchMode = (ImageButton)findViewById(R.id.imgBtnSwitch);
        btnSwitchMode.setOnClickListener(this);
    }

    private int lastVocabularyId;
    private void processLastVocabularyIdFromPreferences(int vocabularyId) {
        if (!isOpenHardWordsModeActive) {
            if (vocabularyId != -1) {
                SharedPreferencesHelper.saveNumberInSharedPreferences(vocabularyId, this, SharedPreferencesHelper.KEY_LAST_USED_VOCABULARY_ID);
            } else {
                lastVocabularyId = SharedPreferencesHelper.loadNumberFromSharedPreferences(this, SharedPreferencesHelper.KEY_LAST_USED_VOCABULARY_ID);
                if (lastVocabularyId != -1 && lastVocabularyId != vocabularyId)
                    AlertDialogsHolder.openAskDialog(this, "Restart last vocabulary ?", this);
            }
        }
    }

    @Override
    public void onNegativeAskButtonPress() {

    }

    @Override
    public void onPossitiveAskButtonPress() {

    }

    public int getVocabularyId() {
        return vocabularyId;
    }

    @Override
    public void findAllViews() {
        tvHeader = (TextView) findViewById(R.id.tvActName);
    }

    @Override
    public void setOnClickListeners() {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        if (isOpenHardWordsModeActive)
            return new HardWordsCursorLoader(this);
        else
            return new WordsCursorLoader(getVocabularyId(), this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
        baseCursorAdapter = setCursorAdapter(cursor);
        listContent.setAdapter(baseCursorAdapter);
    }

    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        super.onActivityResult(arg0, arg1, arg2);
        getSupportLoaderManager().restartLoader(0, null, this);
        baseCursorAdapter.notifyDataSetChanged();
    };

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
        // some code :)
    }

	public CursorAdapter setCursorAdapter(Cursor cursor) {
		adapter = new LearnWordsCursorAdapter(this, cursor, this, vocabularyId);
        int wordsCount = adapter.getCount();
        String message = "Number of words in vocabulary : " + Integer.toString(wordsCount);
        ToastHelper.doInUIThreadShort(message, this);
		return adapter;
	}




	@Override
	public void onClick(View v) {
        switch (v.getId()){
            case R.id.imgBtnSwitch:
                adapter.changeSwitcher();
            break;
        }
	}

}