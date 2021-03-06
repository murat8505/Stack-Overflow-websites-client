package com.prasanna.android.stacknetwork.fragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.SearchCriteria;
import com.prasanna.android.stacknetwork.service.QuestionsIntentService;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class SearchQuestionListFragment extends QuestionListFragment {
  private Intent intent;
  private SearchCriteria searchCriteria;
  private Menu menu;
  private boolean saved = false;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    this.menu = menu;
    if (saved || !AppUtils.savedSearchesMaxed(getActivity()))
      menu.findItem(R.id.menu_save).setVisible(true);
  }

  @Override
  protected void startIntentService() {
    if (isAdded()) {
      prepareIntent();
      showProgressBar();
      startService(intent);
    }
  }

  @Override
  public void onHiddenChanged(boolean hidden) {
    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
      getActivity().getActionBar().setDisplayHomeAsUpEnabled(!hidden);
  }

  public boolean hasResults() {
    return itemListAdapter != null && itemListAdapter.getCount() > 0;
  }

  public void search(SearchCriteria searchCriteria, boolean saved) {
    if (searchCriteria != null) {
      this.saved = saved;

      if (menu != null) {
        if (saved)
          menu.findItem(R.id.menu_save).setVisible(true);
        else {
          if (AppUtils.savedSearchesMaxed(getActivity()))
            menu.findItem(R.id.menu_save).setVisible(false);
          else
            menu.findItem(R.id.menu_save).setVisible(true);
        }
      }
      itemListAdapter.clear();
      itemListAdapter.notifyDataSetChanged();
      this.searchCriteria = searchCriteria;
      startIntentService();
    }
  }

  private void prepareIntent() {
    if (intent == null) {
      intent = getIntentForService(QuestionsIntentService.class, null);
      if (intent != null) {
        intent.putExtra(StringConstants.ACTION, QuestionsIntentService.SEARCH_ADVANCED);
        intent.putExtra(StringConstants.RESULT_RECEIVER, resultReceiver);
        intent.putExtra(StringConstants.SEARCH_CRITERIA, searchCriteria);
      }
    }
    else
      intent.putExtra(StringConstants.SEARCH_CRITERIA, searchCriteria);
  }

  @Override
  protected void loadNextPage() {
    if (searchCriteria != null)
      searchCriteria = searchCriteria.nextPage();

    startIntentService();
  }

}
