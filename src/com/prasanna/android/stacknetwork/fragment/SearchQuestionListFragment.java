package com.prasanna.android.stacknetwork.fragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.SearchCriteria;
import com.prasanna.android.stacknetwork.service.QuestionsIntentService;
import com.prasanna.android.stacknetwork.utils.StackXIntentAction.QuestionIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class SearchQuestionListFragment extends QuestionListFragment
{
    private static final String TAG = SearchQuestionListFragment.class.getSimpleName();
    private Intent intent;
    private SearchCriteria searchCriteria;

    public void search(SearchCriteria searchCriteria)
    {
        Log.d(TAG, "Running search criteria");
        if (searchCriteria != null)
        {
            itemListAdapter.clear();
            itemListAdapter.notifyDataSetChanged();
            this.searchCriteria = searchCriteria;
            startIntentService();
        }
    }

    private void prepareIntent()
    {
        if (intent == null)
        {
            intent = getIntentForService(QuestionsIntentService.class, QuestionIntentAction.QUESTIONS.getAction());
            intent.putExtra(StringConstants.ACTION, QuestionsIntentService.SEARCH_ADVANCED);
            intent.putExtra(StringConstants.RESULT_RECEIVER, resultReceiver);
            intent.putExtra(StringConstants.SEARCH_CRITERIA, searchCriteria);
        }
        else
            intent.putExtra(StringConstants.SEARCH_CRITERIA, searchCriteria.nextPage());
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        Log.d(TAG, "onPrepareOptionsMenu");
        menu.findItem(R.id.menu_save).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_save:
                Toast.makeText(getActivity(), "Search criteria saved", Toast.LENGTH_SHORT).show();
                return true;
        }
        
        return false;
    }

    @Override
    protected void startIntentService()
    {
        if (isAdded())
        {
            prepareIntent();
            showProgressBar();
            startService(intent);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(!hidden);
    }

    @Override
    public void onStop()
    {
        Log.d(getLogTag(), "onStop");

        super.onStop();

        stopService(intent);
    }

    public boolean hasResults()
    {
        return itemListAdapter != null && itemListAdapter.getCount() > 0;
    }

}