package com.prasanna.android.stacknetwork.fragment;

import java.util.ArrayList;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.QuestionIntentAction;
import com.prasanna.android.stacknetwork.utils.QuestionRowLayoutBuilder;

public abstract class QuestionsFragment extends Fragment implements ScrollableFragment
{
    protected boolean serviceRunning = false;

    protected LinearLayout questionsLinearLayout;

    protected ArrayList<Question> questions = new ArrayList<Question>();

    protected ProgressDialog loadingQuestionsDialog;

    protected BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            processIntentBroadcast(context, intent);
        }
    };

    private LinearLayout loadingProgressView;

    private int lastDisplayQuestionIndex = 0;

    private Intent intentForService;

    public abstract QuestionIntentAction getReceiverExtraName();

    public abstract void startIntentService();

    public abstract String getLogTag();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if (container == null)
        {
            Log.d(getLogTag(), "onCreateView return null");
            return null;
        }

        lastDisplayQuestionIndex = 0;

        questionsLinearLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.fragment_questions,
                null);

        return questionsLinearLayout;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        stopServiceAndUnregisterReceiver();
    }

    @Override
    public void onStop()
    {
        super.onStop();

        stopServiceAndUnregisterReceiver();
    }

    private void stopServiceAndUnregisterReceiver()
    {
        if (intentForService != null)
        {
            getActivity().stopService(intentForService);
        }

        try
        {
            getActivity().unregisterReceiver(receiver);
        }
        catch (IllegalArgumentException e)
        {
            Log.d(getLogTag(), e.getMessage());
        }
    }

    @Override
    public void onScrollToBottom()
    {
        if (serviceRunning == false)
        {
            loadingProgressView = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.loading_progress,
                    null);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 15, 0, 15);
            questionsLinearLayout.addView(loadingProgressView, layoutParams);
            startIntentService();
        }
    }

    protected Intent getIntentForService(Class<?> clazz, String action)
    {
        intentForService = new Intent(getActivity().getApplicationContext(), clazz);
        intentForService.setAction(action);
        return intentForService;
    }

    @SuppressWarnings("unchecked")
    private void processIntentBroadcast(Context context, Intent intent)
    {
        if (loadingQuestionsDialog != null)
        {
            loadingQuestionsDialog.dismiss();
            loadingQuestionsDialog = null;
        }

        if (loadingProgressView != null)
        {
            loadingProgressView.setVisibility(View.GONE);
            loadingProgressView = null;
        }

        questions.addAll((ArrayList<Question>) intent.getSerializableExtra(getReceiverExtraName().getExtra()));

        displayQuestions();
    }

    protected void displayQuestions()
    {
        Log.d(getLogTag(), "questions size: " + questions.size() + ", lastDisplayQuestionIndex: "
                + lastDisplayQuestionIndex);

        for (; lastDisplayQuestionIndex < questions.size(); lastDisplayQuestionIndex++)
        {
            LinearLayout questionLayout = QuestionRowLayoutBuilder.getInstance().build(
                    getActivity().getLayoutInflater(), getActivity(), questions.get(lastDisplayQuestionIndex));
            questionsLinearLayout.addView(questionLayout, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
        }

        serviceRunning = false;
    }
}
