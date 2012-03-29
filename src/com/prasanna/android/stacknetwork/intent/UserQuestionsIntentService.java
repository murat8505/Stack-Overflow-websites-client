package com.prasanna.android.stacknetwork.intent;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.Intent;

import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.service.UserService;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class UserQuestionsIntentService extends IntentService
{
    private UserService userService = UserService.getInstance();

    public UserQuestionsIntentService()
    {
        this("UserQuestionsService");
    }

    public UserQuestionsIntentService(String name)
    {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        long userId = intent.getLongExtra(StringConstants.USER_ID, -1);
        int page = intent.getIntExtra("page", 1);

        if (userId == -1)
        {
            getQuestionsForUnauthorizedUser(page);
        }
        else
        {
            getQuestionsByUserAndBroadcastIntent(userId, page);
        }
    }

    private void getQuestionsForUnauthorizedUser(int page)
    {
        ArrayList<Question> questions = userService.getAllQuestions(page);
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentActionEnum.QuestionIntentAction.ALL_QUESTIONS.name());
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(IntentActionEnum.QuestionIntentAction.ALL_QUESTIONS.getExtra(), questions);
        sendBroadcast(broadcastIntent);
    }

    private void getQuestionsByUserAndBroadcastIntent(long userId, int page)
    {
        ArrayList<Question> questions = userService.getQuestionsByUser(userId, page);

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(IntentActionEnum.UserIntentAction.QUESTIONS_BY_USER.name());
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(IntentActionEnum.UserIntentAction.QUESTIONS_BY_USER.getExtra(), questions);
        sendBroadcast(broadcastIntent);
    }
}
