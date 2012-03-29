package com.prasanna.android.stacknetwork;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.prasanna.android.stacknetwork.intent.UserDetailsIntentService;
import com.prasanna.android.stacknetwork.intent.UserQuestionsIntentService;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.DateTimeUtils;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.LayoutBuilder;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.StringConstants;
import com.prasanna.android.task.FetchImageAsyncTask;
import com.prasanna.android.task.ImageFetchAsyncTaskCompleteNotifierImpl;
import com.prasanna.android.views.ScrollViewWithNotifier;

public class UserProfileActivity extends Activity
{
    private static final String TAG = UserProfileActivity.class.getSimpleName();

    private Intent userProfileIntent;

    private Intent questionsByUserIntent;

    private User user;

    private ProgressDialog fetchProfileProgress;

    private ProgressDialog fetchUserQuestionsProgress;

    private RelativeLayout profileHomeLayout;

    private LinearLayout questionsLayout;

    private ScrollViewWithNotifier questionsScroll;

    private ArrayList<Question> questionsByUser = new ArrayList<Question>();

    private LinearLayout loadingProgressView;

    private LinearLayout questionsDisplayList;

    private int page = 0;

    private int questionDisplayCount = 0;

    private class TabListener implements ActionBar.TabListener
    {
        private Fragment mFragment;

        public TabListener(Fragment fragment)
        {
            mFragment = fragment;
        }

        public void onTabSelected(Tab tab, FragmentTransaction ft)
        {
            ft.add(R.id.fragmentContainer, mFragment, null);
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft)
        {
            ft.remove(mFragment);
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft)
        {
            Toast.makeText(UserProfileActivity.this, "Reselected!", Toast.LENGTH_SHORT).show();
        }

    }

    private class ProfileFragment extends Fragment
    {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            profileHomeLayout = (RelativeLayout) inflater.inflate(R.layout.user_proile_layout, container, false);

            if (user == null)
            {
                fetchProfileProgress = ProgressDialog.show(UserProfileActivity.this, "", "Fetching profile");
            }
            else
            {
                displayUserDetail(user, profileHomeLayout);
            }
            return profileHomeLayout;
        }
    }

    private class QuestionsFragment extends Fragment
    {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            fetchUserQuestionsProgress = ProgressDialog.show(UserProfileActivity.this, "", "Loading questions");

            questionsDisplayList = (LinearLayout) questionsLayout.findViewById(R.id.questionsDisplay);
            questionsScroll = (ScrollViewWithNotifier) questionsLayout.findViewById(R.id.questionsScroll);
            questionsScroll.setOnScrollListener(new ScrollViewWithNotifier.OnScrollListener()
            {
                @Override
                public void onScrollToBottom(View view)
                {
                    if (loadingProgressView == null)
                    {
                        loadingProgressView = (LinearLayout) getLayoutInflater().inflate(R.layout.loading_progress,
                                null);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                        layoutParams.setMargins(0, 15, 0, 15);
                        questionsDisplayList.addView(loadingProgressView, layoutParams);
                    }

                    startUserQuestionsService();
                }
            });

            displayQuestions();

            return questionsLayout;
        }

    }

    private void displayQuestions()
    {
        if (fetchUserQuestionsProgress != null)
        {
            fetchUserQuestionsProgress.dismiss();
            fetchUserQuestionsProgress = null;
        }

        if (loadingProgressView != null)
        {
            loadingProgressView.setVisibility(View.GONE);
            loadingProgressView = null;
        }

        if (questionsByUser != null && questionsLayout != null && questionsDisplayList != null)
        {
            for (; questionDisplayCount < questionsByUser.size(); questionDisplayCount++)
            {
                LinearLayout questionLayout = LayoutBuilder.getInstance().buildQuestionSnippet(
                        questionsLayout.getContext(), questionsByUser.get(questionDisplayCount));
                questionsDisplayList.addView(questionLayout, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                        LayoutParams.WRAP_CONTENT));
            }
        }
    }

    private BroadcastReceiver userProfileReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            user = (User) intent.getSerializableExtra(IntentActionEnum.UserIntentAction.USER_DETAIL.getExtra());

            if (profileHomeLayout != null)
            {
                fetchProfileProgress.dismiss();
                displayUserDetail(user, profileHomeLayout);
            }
        }
    };

    private BroadcastReceiver questionsByUserReceiver = new BroadcastReceiver()
    {
        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent)
        {
            questionsByUser.addAll((ArrayList<Question>) intent
                    .getSerializableExtra(IntentActionEnum.UserIntentAction.QUESTIONS_BY_USER.getExtra()));

            displayQuestions();

            Log.d(TAG, "Number of questions by user: " + questionsByUser.size());
        }
    };

    @Override
    public void onCreate(android.os.Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        questionsLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.questions_layout, null);
        setContentView(R.layout.fragment_container);
        registerForUserProfileReceiver();
        registerForQuestionsByUserReceiver();
        startUserProfileService();
        startUserQuestionsService();
        setupActionBarTabs();
    }

    private void setupActionBarTabs()
    {
        ActionBar actionBar = getActionBar();
        getActionBar().setTitle(OperatingSite.getSite().getName());
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        Tab profileTab = actionBar.newTab();
        profileTab.setText("Profile").setTabListener(new TabListener(new ProfileFragment()));
        actionBar.addTab(profileTab);

        Tab questionsTab = actionBar.newTab();
        questionsTab.setText("Questions").setTabListener(new TabListener(new QuestionsFragment()));
        actionBar.addTab(questionsTab);

        Tab answersTab = actionBar.newTab();
        answersTab.setText("Answers").setTabListener(new TabListener(new ProfileFragment()));
        actionBar.addTab(answersTab);

        Tab tagsTab = actionBar.newTab();
        tagsTab.setText("Tags").setTabListener(new TabListener(new ProfileFragment()));
        actionBar.addTab(tagsTab);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        stopServiceAndUnregsiterReceiver();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        stopServiceAndUnregsiterReceiver();
    }

    private void stopServiceAndUnregsiterReceiver()
    {
        if (userProfileIntent != null)
        {
            stopService(userProfileIntent);
        }

        try
        {
            unregisterReceiver(userProfileReceiver);
        }
        catch (IllegalArgumentException e)
        {
            Log.d(TAG, e.getMessage());
        }
    }

    private void startUserProfileService()
    {
        long userId = (long) getIntent().getLongExtra(StringConstants.USER_ID, -1);
        userProfileIntent = new Intent(this, UserDetailsIntentService.class);
        userProfileIntent.setAction(IntentActionEnum.UserIntentAction.USER_DETAIL.name());
        userProfileIntent.putExtra(StringConstants.USER_ID, userId);
        startService(userProfileIntent);
    }

    private void startUserQuestionsService()
    {
        long userId = (long) getIntent().getLongExtra(StringConstants.USER_ID, -1);
        questionsByUserIntent = new Intent(this, UserQuestionsIntentService.class);
        questionsByUserIntent.setAction(IntentActionEnum.UserIntentAction.QUESTIONS_BY_USER.name());
        questionsByUserIntent.putExtra(StringConstants.USER_ID, userId);
        questionsByUserIntent.putExtra(StringConstants.PAGE, ++page);
        startService(questionsByUserIntent);

    }

    private void registerForUserProfileReceiver()
    {
        IntentFilter filter = new IntentFilter(IntentActionEnum.UserIntentAction.USER_DETAIL.name());
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(userProfileReceiver, filter);
    }

    private void registerForQuestionsByUserReceiver()
    {
        IntentFilter filter = new IntentFilter(IntentActionEnum.UserIntentAction.QUESTIONS_BY_USER.name());
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(questionsByUserReceiver, filter);
    }

    private void displayUserDetail(User user, RelativeLayout relativeLayout)
    {
        if (user != null)
        {
            updateProfileInfo(user, relativeLayout);

            TextView textView = (TextView) relativeLayout.findViewById(R.id.questionCount);
            textView.append(" " + String.valueOf(user.getQuestionCount()));

            textView = (TextView) relativeLayout.findViewById(R.id.answerCount);
            textView.append(" " + String.valueOf(user.getAnswerCount()));

            textView = (TextView) relativeLayout.findViewById(R.id.upvoteCount);
            textView.append(" " + String.valueOf(user.getUpvoteCount()));

            textView = (TextView) relativeLayout.findViewById(R.id.downvoteCount);
            textView.append(" " + String.valueOf(user.getDownvoteCount()));
        }
    }

    private void updateProfileInfo(User user, RelativeLayout relativeLayout)
    {
        ImageView userProfileImage = (ImageView) relativeLayout.findViewById(R.id.profileUserImage);
        FetchImageAsyncTask fetchImageAsyncTask = new FetchImageAsyncTask(new ImageFetchAsyncTaskCompleteNotifierImpl(
                userProfileImage));
        fetchImageAsyncTask.execute(user.getProfileImageLink());

        TextView textView = (TextView) relativeLayout.findViewById(R.id.profileDisplayName);
        textView.setText(user.getDisplayName());

        textView = (TextView) relativeLayout.findViewById(R.id.profileUserReputation);
        textView.setText(AppUtils.formatUserReputation(user.getReputation()));

        if (user.getBadgeCounts() != null && user.getBadgeCounts().length == 3)
        {
            textView = (TextView) relativeLayout.findViewById(R.id.profileUserGoldNum);
            textView.setText(String.valueOf(user.getBadgeCounts()[0]));

            textView = (TextView) relativeLayout.findViewById(R.id.profileUserSilverNum);
            textView.setText(String.valueOf(user.getBadgeCounts()[1]));

            textView = (TextView) relativeLayout.findViewById(R.id.profileUserBronzeNum);
            textView.setText(String.valueOf(user.getBadgeCounts()[2]));
        }

        textView = (TextView) relativeLayout.findViewById(R.id.profileViews);
        textView.append(" " + user.getProfileViews());

        textView = (TextView) relativeLayout.findViewById(R.id.profileUserLastSeen);
        textView.append(" " + DateTimeUtils.getElapsedDurationSince(user.getLastAccessTime()));
    }
}
