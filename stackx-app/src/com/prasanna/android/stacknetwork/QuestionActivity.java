/*
 Copyright (C) 2013 Prasanna Thirumalai

 This file is part of StackX.

 StackX is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 StackX is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with StackX.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.prasanna.android.stacknetwork;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.prasanna.android.http.HttpException;
import com.prasanna.android.stacknetwork.fragment.AnswerFragment;
import com.prasanna.android.stacknetwork.fragment.CommentFragment;
import com.prasanna.android.stacknetwork.fragment.CommentFragment.OnCommentChangeListener;
import com.prasanna.android.stacknetwork.fragment.PostCommentFragment;
import com.prasanna.android.stacknetwork.fragment.QuestionFragment;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Comment;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.WritePermission;
import com.prasanna.android.stacknetwork.model.WritePermission.ObjectType;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver;
import com.prasanna.android.stacknetwork.receiver.RestQueryResultReceiver.StackXRestQueryResultReceiver;
import com.prasanna.android.stacknetwork.service.QuestionDetailsIntentService;
import com.prasanna.android.stacknetwork.service.WriteIntentService;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.IntentUtils;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.SharedPreferencesUtil;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.stacknetwork.utils.WritePermissionUtil;
import com.prasanna.android.utils.LogWrapper;
import com.viewpagerindicator.TitlePageIndicator;

public class QuestionActivity extends AbstractUserActionBarActivity implements OnPageChangeListener,
                StackXRestQueryResultReceiver, PageSelectAdapter
{
    private static final String TAG = QuestionActivity.class.getSimpleName();

    private Question question;
    private Intent intent;
    private QuestionFragment questionFragment;
    private QuestionViewPageAdapter questionViewPageAdapter;
    private ViewPager viewPager;
    private TitlePageIndicator titlePageIndicator;
    private RestQueryResultReceiver resultReceiver;
    private boolean serviceRunningForAnswers = false;
    private CommentFragment commentFragment;
    private PostCommentFragment postCommentFragment;
    private HashMap<String, String> commentsDraft = new HashMap<String, String>();

    public class QuestionViewPageAdapter extends FragmentPagerAdapter
    {
        public QuestionViewPageAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public int getCount()
        {
            if (question == null)
                return 1;
            else
                return question.answers == null ? 1 : 1 + question.answers.size();
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            if (position == 0)
                return QuestionActivity.this.getString(R.string.question);
            else
            {
                if (question.answers.get(position - 1).accepted)
                    return QuestionActivity.this.getString(R.string.accepted) + " "
                                    + QuestionActivity.this.getString(R.string.answer);
                else
                    return QuestionActivity.this.getString(R.string.answer) + " " + position;
            }
        }

        @Override
        public Fragment getItem(int position)
        {
            if (position == 0)
                return QuestionActivity.this.questionFragment;
            else
            {
                Fragment fragment = getFragmentManager().findFragmentByTag(getViewPagerFragmentTag(position));

                if (fragment == null)
                    return AnswerFragment.newFragment(question.answers.get(position - 1), position,
                                    QuestionActivity.this);

                return fragment;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.viewpager_title_indicator);

        resultReceiver = new RestQueryResultReceiver(new Handler());
        resultReceiver.setReceiver(this);

        if (questionFragment == null)
            questionFragment = QuestionFragment.newFragment();

        setupViewPager();

        if (savedInstanceState != null && savedInstanceState.getSerializable(StringConstants.QUESTION) != null)
            question = (Question) savedInstanceState.getSerializable(StringConstants.QUESTION);
        else
            prepareIntentAndStartService();
    }

    @Override
    protected boolean shouldSearchViewBeEnabled()
    {
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        boolean ret = super.onPrepareOptionsMenu(menu);

        if (menu != null)
            enableAddCommentIfPermitted(menu);

        return ret;
    }

    private void enableAddCommentIfPermitted(Menu menu)
    {
        boolean canAddComment =
                        WritePermissionUtil.canAdd(getApplicationContext(), OperatingSite.getSite().apiSiteParameter,
                                        ObjectType.COMMENT);

        if (AppUtils.inAuthenticatedRealm(this) && canAddComment)
            setupAddComment(menu);
    }

    private void setupAddComment(Menu menu)
    {
        MenuItem newCommentMenuItem = menu.findItem(R.id.menu_new_comment);
        newCommentMenuItem.setVisible(true);
        newCommentMenuItem.getActionView().setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setupPostCommentFragment(null);
            }
        });
    }

    private void setupPostCommentFragment(String defaultText)
    {
        if (viewPager.getCurrentItem() == 0)
        {
            displayPostCommentFragment("Comment on question by " + question.owner.displayName, question.id, 0,
                            question.id + "-comment", defaultText);
        }
        else
        {
            Answer answer = question.answers.get(viewPager.getCurrentItem() - 1);
            displayPostCommentFragment("Comment on answer by " + answer.owner.displayName, answer.id,
                            viewPager.getCurrentItem(), answer.id + "-comment", defaultText);
        }
    }

    private void displayPostCommentFragment(String title, long id, int viewPagerPosition, String fragmentTag,
                    String defaultText)
    {
        postCommentFragment = new PostCommentFragment();
        postCommentFragment.setPostId(id);
        postCommentFragment.setViewPagerPosition(viewPagerPosition);
        postCommentFragment.setTitle(title);
        postCommentFragment.setResultReceiver(resultReceiver);

        if (commentsDraft.get(fragmentTag) != null)
            postCommentFragment.setDraftText(commentsDraft.get(fragmentTag));

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, postCommentFragment, fragmentTag);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.addToBackStack(fragmentTag);
        transaction.commit();
    }

    private void setupViewPager()
    {
        questionViewPageAdapter = new QuestionViewPageAdapter(getFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(questionViewPageAdapter);

        titlePageIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        titlePageIndicator.setViewPager(viewPager);
        titlePageIndicator.setOnPageChangeListener(this);
    }

    private void prepareIntentAndStartService()
    {
        intent = new Intent(this, QuestionDetailsIntentService.class);
        intent.putExtra(StringConstants.RESULT_RECEIVER, resultReceiver);

        setProgressBarIndeterminateVisibility(true);

        if (StringConstants.QUESTION_ID.equals(getIntent().getAction()))
        {
            long questionId = getIntent().getLongExtra(StringConstants.QUESTION_ID, 0);
            intent.setAction(StringConstants.QUESTION_ID);
            intent.putExtra(StringConstants.QUESTION_ID, questionId);
            intent.putExtra(StringConstants.SITE, getIntent().getStringExtra(StringConstants.SITE));
            startService(intent);
        }
        else
        {
            Question metaDetails = (Question) getIntent().getSerializableExtra(StringConstants.QUESTION);

            if (metaDetails != null)
                question = Question.copyMetaDeta(metaDetails);

            if (question != null)
            {
                questionFragment.setQuestion(question);

                intent.setAction(StringConstants.QUESTION);
                intent.putExtra(StringConstants.QUESTION_ID, question.id);
                intent.putExtra(StringConstants.QUESTION, question);
                intent.putExtra(StringConstants.SITE, getIntent().getStringExtra(StringConstants.SITE));
                intent.putExtra(StringConstants.REFRESH, getIntent().getBooleanExtra(StringConstants.REFRESH, false));
                startService(intent);
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        if (commentFragment != null && commentFragment.isVisible())
            commentFragment.onBackPressed();

        super.onBackPressed();
        dismissPostCommentFragmentIfVisible(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        if (question != null)
            outState.putSerializable(StringConstants.QUESTION, question);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void refresh()
    {
        Intent intent = getIntent();
        finish();
        intent.putExtra(StringConstants.REFRESH, true);
        startActivity(intent);
    }

    @Override
    public void onPageScrollStateChanged(int arg0)
    {
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2)
    {
    }

    @Override
    public void onPageSelected(int position)
    {
        int numAnswersDisplayed = questionViewPageAdapter.getCount() - 1;

        if (commentFragment != null && commentFragment.isVisible())
        {
            getFragmentManager().popBackStackImmediate();
            commentFragment = null;
        }

        dismissPostCommentFragmentIfVisible(false);

        if (numAnswersDisplayed < question.answerCount && numAnswersDisplayed - position < 2)
        {
            if (question.answers != null && question.answers.size() == numAnswersDisplayed && !serviceRunningForAnswers)
            {
                setProgressBarIndeterminateVisibility(true);
                startServiceForAnswers();
            }
        }
    }

    private void dismissPostCommentFragmentIfVisible(boolean finish)
    {
        if (postCommentFragment != null)
        {
            postCommentFragment.hideSoftKeyboard();

            if (!finish)
                commentsDraft.put(postCommentFragment.getTag(), postCommentFragment.getCurrentText());

            getFragmentManager().popBackStackImmediate();
            postCommentFragment = null;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        if (viewPager.getCurrentItem() == 0)
            return onContextItemSelectedForQuestion(item);
        else
            return onContextItemSelectedForAnswer(item, viewPager.getCurrentItem() - 1);
    }

    private boolean onContextItemSelectedForQuestion(MenuItem item)
    {
        if (item.getGroupId() == R.id.qContextMenuGroup)
        {
            LogWrapper.d(TAG, "Context item selected: " + item.getTitle());

            switch (item.getItemId())
            {
                case R.id.q_ctx_comments:
                    displayComments(question.id, question.comments);
                    return true;
                case R.id.q_ctx_similar:
                    startSimilarQuestionsActivity();
                    return true;
                case R.id.q_ctx_related:
                    startRelatedQuestionsActivity();
                    return true;
                case R.id.q_ctx_menu_user_profile:
                    viewUserProfile(question.owner.id);
                    return true;
                case R.id.q_ctx_menu_email:
                    emailQuestion();
                    return true;
                default:
                    LogWrapper.d(TAG, "Unknown item selected: " + item.getTitle());
                    return false;
            }
        }
        else if (item.getGroupId() == R.id.qContextTagsMenuGroup)
        {
            Intent questionsIntent = new Intent(this, QuestionsActivity.class);
            questionsIntent.setAction(StringConstants.TAG);
            questionsIntent.putExtra(StringConstants.TAG, item.getTitle());
            startActivity(questionsIntent);

            return true;
        }

        return false;
    }

    private void startRelatedQuestionsActivity()
    {
        Intent questionsIntent = new Intent(this, QuestionsActivity.class);
        questionsIntent.setAction(StringConstants.RELATED);
        questionsIntent.putExtra(StringConstants.QUESTION_ID, question.id);
        startActivity(questionsIntent);
    }

    private void startSimilarQuestionsActivity()
    {
        Intent questionsIntent = new Intent(this, QuestionsActivity.class);
        questionsIntent.setAction(StringConstants.SIMILAR);
        questionsIntent.putExtra(StringConstants.TITLE, question.title);
        startActivity(questionsIntent);
    }

    private boolean onContextItemSelectedForAnswer(MenuItem item, int answerPosition)
    {
        if (item.getGroupId() == R.id.qContextMenuGroup)
        {
            Answer answer = question.answers.get(answerPosition);

            switch (item.getItemId())
            {
                case R.id.q_ctx_comments:
                    displayComments(answer.id, answer.comments);
                    return true;
                case R.id.q_ctx_menu_user_profile:
                    viewUserProfile(answer.owner.id);
                    return true;
                default:
                    LogWrapper.d(TAG, "Unknown item selected: " + item.getTitle());
                    return false;
            }
        }

        return false;
    }

    private void displayComments(long postId, ArrayList<Comment> comments)
    {
        if (comments != null && !comments.isEmpty())
            displayCommentFragment(postId + "-" + StringConstants.COMMENTS, comments);
        else
            Toast.makeText(this, "No comments for answer", Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onReceiveResult(int resultCode, Bundle resultData)
    {
        switch (resultCode)
        {
            case QuestionDetailsIntentService.RESULT_CODE_Q_CACHED:
                setProgressBarIndeterminateVisibility(false);
            case QuestionDetailsIntentService.RESULT_CODE_Q:
                question = (Question) resultData.getSerializable(StringConstants.QUESTION);
                displayQuestion();
                if (question.answerCount > 0 && (question.answers == null || question.answers.isEmpty()))
                    startServiceForAnswers();
                else
                    setProgressBarIndeterminateVisibility(false);
                break;
            case QuestionDetailsIntentService.RESULT_CODE_Q_BODY:
                questionFragment.displayBody(resultData.getString(StringConstants.BODY));
                if (question.answerCount == 0)
                    setProgressBarIndeterminateVisibility(false);
                break;
            case QuestionDetailsIntentService.RESULT_CODE_Q_COMMENTS:
                question.comments = (ArrayList<Comment>) resultData.getSerializable(StringConstants.COMMENTS);
                questionFragment.setComments(question.comments);
                break;
            case QuestionDetailsIntentService.RESULT_CODE_ANSWERS:
                serviceRunningForAnswers = false;
                setProgressBarIndeterminateVisibility(false);
                displayAnswers((ArrayList<Answer>) resultData.getSerializable(StringConstants.ANSWERS), true);
                break;
            case WriteIntentService.ACTION_ADD_COMMENT:
                dismissPostCommentFragmentIfVisible(true);
                addMyCommentToPost(resultData);
                break;
            case QuestionDetailsIntentService.ERROR:
                handleError(resultData);
                break;
            default:
                break;
        }
    }

    private void handleError(Bundle resultData)
    {
        setProgressBarIndeterminateVisibility(false);

        HttpException e = (HttpException) resultData.getSerializable(StringConstants.EXCEPTION);
        int requestCode = resultData.getInt(StringConstants.REQUEST_CODE, -1);

        if (requestCode == WriteIntentService.ACTION_ADD_COMMENT)
        {
            if (postCommentFragment != null)
                postCommentFragment.setSendError(e.getErrorResponse());
        }
        else
            AppUtils.getErrorView(this, e);
    }

    protected void addMyCommentToPost(Bundle resultData)
    {
        SharedPreferencesUtil.setLong(this, WritePermission.PREF_LAST_COMMENT_WRITE, System.currentTimeMillis());
        Comment comment = (Comment) resultData.getSerializable(StringConstants.COMMENT);
        int viewPagerPosition = resultData.getInt(StringConstants.VIEW_PAGER_POSITION, -1);
        if (viewPagerPosition == 0)
        {
            if (question.comments == null)
                question.comments = new ArrayList<Comment>();

            question.comments.add(comment);
            questionFragment.setComments(question.comments);
        }
        else
        {
            LogWrapper.d(TAG, "View pager position : " + viewPagerPosition);
            AnswerFragment answerFragment =
                            (AnswerFragment) getFragmentManager().findFragmentByTag(
                                            getViewPagerFragmentTag(viewPagerPosition));
            answerFragment.onCommentAdd(comment);
        }
    }

    private void startServiceForAnswers()
    {
        intent = new Intent(this, QuestionDetailsIntentService.class);
        intent.putExtra(StringConstants.RESULT_RECEIVER, resultReceiver);
        intent.setAction(StringConstants.ANSWERS);
        intent.putExtra(StringConstants.SITE, getIntent().getStringExtra(StringConstants.SITE));
        intent.putExtra(StringConstants.QUESTION_ID, question.id);
        intent.putExtra(StringConstants.PAGE, getNextPageNumber());
        startService(intent);
        serviceRunningForAnswers = true;
    }

    private void displayQuestion()
    {
        if (questionFragment == null)
            questionFragment = QuestionFragment.newFragment();

        questionFragment.setAndDisplay(question);

        titlePageIndicator.notifyDataSetChanged();
        questionViewPageAdapter.notifyDataSetChanged();
    }

    private void displayCommentFragment(String fragmentTag, ArrayList<Comment> comments)
    {
        commentFragment = (CommentFragment) getFragmentManager().findFragmentByTag(fragmentTag);

        if (commentFragment == null)
        {
            commentFragment = new CommentFragment();
            commentFragment.setComments(comments);
            commentFragment.setResultReceiver(resultReceiver);

            String currentViewPagerFragmentTag =
                            "android:switcher:" + R.id.viewPager + ":" + viewPager.getCurrentItem();

            OnCommentChangeListener onCommentChangeListener =
                            (OnCommentChangeListener) getFragmentManager().findFragmentByTag(
                                            currentViewPagerFragmentTag);

            if (onCommentChangeListener != null)
                commentFragment.setOnCommentChangeListener(onCommentChangeListener);
        }

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, commentFragment, fragmentTag);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.addToBackStack(fragmentTag);
        transaction.commit();
    }

    private void displayAnswers(ArrayList<Answer> answers, boolean add)
    {
        if (answers != null && !answers.isEmpty())
        {
            if (question.answers == null)
                question.answers = new ArrayList<Answer>();

            if (add && !StringConstants.QUESTION_ID.equals(getIntent().getAction()))
                question.answers.addAll(answers);

            titlePageIndicator.notifyDataSetChanged();
            questionViewPageAdapter.notifyDataSetChanged();
        }
    }

    private void viewUserProfile(long userId)
    {
        Intent userProfileIntent = new Intent(this, UserProfileActivity.class);
        userProfileIntent.putExtra(StringConstants.USER_ID, userId);
        startActivity(userProfileIntent);
    }

    private void emailQuestion()
    {
        Intent emailIntent = IntentUtils.createEmailIntent(question.title, question.link);
        startActivity(Intent.createChooser(emailIntent, ""));
    }

    private int getNextPageNumber()
    {
        int numAnswersDisplayed = questionViewPageAdapter.getCount() - 1;
        return (numAnswersDisplayed / 10) + 1;
    }

    @Override
    public void selectQuestionPage()
    {
        if (viewPager != null && viewPager.getAdapter().getCount() > 0)
        {
            final int currentItem = viewPager.getCurrentItem();

            questionFragment.enableNavigationBack(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    viewPager.setCurrentItem(currentItem);
                }
            });

            viewPager.setCurrentItem(0);
        }
    }

    private String getViewPagerFragmentTag(int position)
    {
        String currentViewPagerFragmentTag = "android:switcher:" + R.id.viewPager + ":" + position;
        return currentViewPagerFragmentTag;
    }
}