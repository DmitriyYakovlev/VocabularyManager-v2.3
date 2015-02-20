package com.yakovlev.prod.vocabularymanager.adapters;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.app.LoaderManager;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yakovlev.prod.vocabularymanager.LearnWordsInVocabularyActivity;
import com.yakovlev.prod.vocabularymanager.cursor_loaders.WordsCursorLoader;
import com.yakovlev.prod.vocabularymanager.dialogs.AlertDialogsHolder;
import com.yakovlev.prod.vocabularymanager.dialogs.DialogButtonsCallback;
import com.yakovlev.prod.vocabularymanager.dialogs.OperateWordDialog;
import com.yakovlev.prod.vocabularymanager.dialogs.OperateWordDialogCallback;
import com.yakovlev.prod.vocabularymanager.ormlite.CursorHelper;
import com.yakovlev.prod.vocabularymanager.ormlite.DatabaseHelper;
import com.yakovlev.prod.vocabularymanager.ormlite.WordStatusEnum;
import com.yakovlev.prod.vocabularymanager.ormlite.WordTable;
import com.yakovlev.prod.vocabularymanager.ormlite.WordTableHelper;
import com.yakovlev.prod.vocabularymanger.R;

public class LearnWordsCursorAdapter extends CursorAdapter implements OperateWordDialogCallback, DialogButtonsCallback{

	private LayoutInflater inflater;
	private TextView tvKey, tvValue;
	public Set<Integer> checkedItemsList = new HashSet<Integer>();
    private boolean hideRightSide = true;
    private Context context;
	private LoaderManager.LoaderCallbacks<Cursor> cursorLoaderCallbacks;
    private int vocabId;

	public LearnWordsCursorAdapter(Context context, Cursor cursor, LoaderManager.LoaderCallbacks<Cursor> cursorLoaderCallbacks, int vocabId) {
		super(context, cursor);
		this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.cursorLoaderCallbacks = cursorLoaderCallbacks;
        this.vocabId = vocabId;
	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {
		final int id = CursorHelper.getNumberByField(cursor, "_id");
		String key = CursorHelper.getStringByField(cursor, "wKey");
		String value = CursorHelper.getStringByField(cursor, "wValue");
		String wTranscription = CursorHelper.getStringByField(cursor, "wTranscription");
		int wStatus = CursorHelper.getNumberByField(cursor, "wordStatus");

		tvKey = (TextView) view.findViewById(R.id.tvKey);
		tvValue = (TextView) view.findViewById(R.id.tvValue);
		
		tvKey.setText(key);
		tvValue.setText(value);

		final View viewRightHide =  (View) view.findViewById(R.id.hideViewRight);
		final View viewLeftHide =  (View) view.findViewById(R.id.hideViewLeft);

		if (checkedItemsList.contains(id)) {
            setVisibilityForLeftHideViewAndRightHideView(viewLeftHide, viewRightHide, View.GONE, View.GONE);
        }
		else {
            if (hideRightSide)
                setVisibilityForLeftHideViewAndRightHideView(viewLeftHide, viewRightHide, View.GONE, View.VISIBLE);
            else
                setVisibilityForLeftHideViewAndRightHideView(viewLeftHide, viewRightHide, View.VISIBLE, View.GONE);
        }
		
		LinearLayout itemParent = (LinearLayout) view.findViewById(R.id.itemWordLearn);
		itemParent.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                processLinearLayoutProcessing(viewLeftHide, viewRightHide, id);
            }
        });

        processWordStatus(wStatus, tvKey, tvValue);

        setOnLongClickListenerForItem(id, itemParent, tvKey, tvValue);
    }

    @Override
    public void onDeleteWordButtonPressed(int wordId) {
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            WordTableHelper.deleteWordFromDb(wordId, dbHelper);
            reloadCursorAndChangeForAdapter();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEditWordButtonPressed(int wordId) {
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            WordTable wordTable = WordTableHelper.getWordById(wordId, dbHelper);
            AlertDialogsHolder.openEditWordDialog(context, this, wordTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void possitiveButtonPress(WordTable wTable) {
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            WordTableHelper.updateWordFromDb(wTable, dbHelper);
            reloadCursorAndChangeForAdapter();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void negativeButtonPress(String key, String value) {

    }

    @Override
    public void onChangeStatusButtonPressed(TextView tvKey, TextView tvValue, int wordId) {
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            int wordStatusNew = WordTableHelper.changeWordStatus(wordId, dbHelper);
            processWordStatus(wordStatusNew,  tvKey, tvValue);
            reloadCursorAndChangeForAdapter();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void reloadCursorAndChangeForAdapter(){
        WordsCursorLoader loader = new WordsCursorLoader(vocabId, context);
        changeCursor(loader.getCursor());
    }

    private void processWordStatus(int wStatus, TextView tvKey, TextView tvValue){
        if (wStatus == WordStatusEnum.getNormal()){
            setTvForNormalWord(tvKey);
            setTvForNormalWord(tvValue);
        }
        else if (wStatus == WordStatusEnum.getHard()) {
            setTvForHardWord(tvKey);
            setTvForHardWord(tvValue);
        }

    }

    private void setTvForHardWord(TextView textView){
        textView.setTextColor(Color.RED);
        textView.setTypeface(null,Typeface.BOLD);
    }

    private void setTvForNormalWord(TextView textView){
        textView.setTextColor(Color.WHITE);
        textView.setTypeface(null,Typeface.NORMAL);
    }

    private void setOnLongClickListenerForItem(final int wordId, View itemParent, final TextView tvKey, final TextView tvValue){
        View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                OperateWordDialog dialog = new OperateWordDialog(context, LearnWordsCursorAdapter.this, tvKey, tvValue, wordId );
                dialog.show();
                return false;
            }
        };
        itemParent.setOnLongClickListener(onLongClickListener);
    }

    private void processLinearLayoutProcessing(View viewLeftHide, View viewRightHide, int id){
        if (hideRightSide) {
            if (viewRightHide.getVisibility() == View.VISIBLE)
                setVisibilityForLeftHideViewAndRightHideView(viewLeftHide, viewRightHide, View.GONE, View.GONE);
            else
                setVisibilityForLeftHideViewAndRightHideView(viewLeftHide, viewRightHide, View.GONE, View.VISIBLE);
        }
        else {
            if (viewLeftHide.getVisibility() == View.VISIBLE)
                setVisibilityForLeftHideViewAndRightHideView(viewLeftHide, viewRightHide, View.GONE, View.GONE);
            else
                setVisibilityForLeftHideViewAndRightHideView(viewLeftHide, viewRightHide, View.VISIBLE, View.GONE);
        }
        processCheckedItemList(id);
    }

    private void setVisibilityForLeftHideViewAndRightHideView(View viewLeftHide, View viewRightHide, int leftVisibility, int rightVisibility){
        viewLeftHide.setVisibility(leftVisibility);
        viewRightHide.setVisibility(rightVisibility);
    }

    private void processCheckedItemList(int id){
        if (checkedItemsList.contains(id))
            checkedItemsList.remove(id);
        else
            checkedItemsList.add(id);
    }

    public void changeSwitcher(){
        if (hideRightSide)
            hideRightSide = false;
        else
            hideRightSide = true;
        notifyDataSetChanged();
    }

	public static String getFirstNSymbols(String s, int n){
		return s.substring(0, Math.min(s.length(), n));
	}
	
	@Override
	public View newView(Context arg0, Cursor cursor, ViewGroup parent) {
		return inflater.inflate(R.layout.item_learn_words_adapter, parent, false);
	}

}
