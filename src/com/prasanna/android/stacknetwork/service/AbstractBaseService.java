package com.prasanna.android.stacknetwork.service;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.JsonFields;

public abstract class AbstractBaseService
{
    protected abstract String getLogTag();

    protected User getSerializedUserObject(JSONObject userJsonObject)
    {
        User user = null;
        try
        {

            user = new User();
            user.id = userJsonObject.getLong(JsonFields.User.USER_ID);
            user.accountId = userJsonObject.getLong(JsonFields.User.ACCOUNT_ID);
            user.displayName = userJsonObject.getString(JsonFields.User.DISPLAY_NAME);
            user.reputation = userJsonObject.getInt(JsonFields.User.REPUTATION);
            user.profileImageLink = userJsonObject.getString(JsonFields.User.PROFILE_IMAGE);
            user.questionCount = userJsonObject.getInt(JsonFields.User.QUESTION_COUNT);
            user.answerCount = userJsonObject.getInt(JsonFields.User.ANSWER_COUNT);
            user.upvoteCount = userJsonObject.getInt(JsonFields.User.UP_VOTE_COUNT);
            user.downvoteCount = userJsonObject.getInt(JsonFields.User.DOWN_VOTE_COUNT);
            user.profileViews = userJsonObject.getInt(JsonFields.User.VIEW_COUNT);
            user.badgeCounts = getBadgeCounts(userJsonObject);
            user.lastAccessTime = userJsonObject.getLong(JsonFields.User.LAST_ACCESS_DATE);
            user.acceptRate = userJsonObject.getInt(JsonFields.User.ACCEPT_RATE);
        }
        catch (JSONException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return user;
    }

    protected int[] getBadgeCounts(JSONObject userJsonObject) throws JSONException
    {
        int[] badgeCounts = new int[3];
        badgeCounts[0] = 0;
        badgeCounts[1] = 0;
        badgeCounts[2] = 0;
        JSONObject badgeCountJsonObject = userJsonObject.getJSONObject(JsonFields.User.BADGE_COUNTS);
        badgeCounts[0] = badgeCountJsonObject.getInt(JsonFields.BadgeCounts.GOLD);
        badgeCounts[1] = badgeCountJsonObject.getInt(JsonFields.BadgeCounts.SILVER);
        badgeCounts[2] = badgeCountJsonObject.getInt(JsonFields.BadgeCounts.BRONZE);

        return badgeCounts;
    }

    protected ArrayList<Question> getQuestionModel(JSONObjectWrapper questionsJsonResponse)
    {
        ArrayList<Question> questions = new ArrayList<Question>();
        JSONArray jsonArray = questionsJsonResponse.getJSONArray(JsonFields.ITEMS);
        if (jsonArray != null)
        {
            try
            {
                for (int i = 0; i < jsonArray.length(); i++)
                {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    questions.add(getSerializedQuestionObject(jsonObject));
                }
            }
            catch (JSONException e)
            {
                Log.d(getLogTag(), e.getMessage());
            }
        }
        return questions;
    }

    protected Question getSerializedQuestionObject(JSONObject jsonObject) throws JSONException
    {
        Question question = new Question();

        question.title = jsonObject.getString(JsonFields.Question.TITLE);
        question.id = jsonObject.getLong(JsonFields.Question.QUESTION_ID);
        question.answered = jsonObject.getBoolean(JsonFields.Question.IS_ANSWERED);
        question.score = jsonObject.getInt(JsonFields.Question.SCORE);
        question.answerCount = jsonObject.getInt(JsonFields.Question.ANSWER_COUNT);
        question.viewCount = jsonObject.getInt(JsonFields.Question.VIEW_COUNT);
        question.tags = getTags(jsonObject);
        question.owner = getOwner(jsonObject);
        question.creationDate = jsonObject.getLong(JsonFields.Question.CREATION_DATE);

        if (jsonObject.has(JsonFields.Question.ACCEPTED_ANSWER_ID))
        {
            question.hasAcceptedAnswer = true;
        }
        return question;
    }

    protected User getOwner(JSONObject jsonObject)
    {
        User user = null;
        JSONObject owner = null;
        try
        {
            owner = jsonObject.getJSONObject(JsonFields.Question.OWNER);
            if (owner != null)
            {
                user = new User();
                user.id = owner.getLong(JsonFields.User.USER_ID);
                user.displayName = owner.getString(JsonFields.User.DISPLAY_NAME);
                user.reputation = owner.getInt(JsonFields.User.REPUTATION);
                user.profileImageLink = owner.getString(JsonFields.User.PROFILE_IMAGE);
                user.acceptRate = owner.getInt(JsonFields.User.ACCEPT_RATE);
            }
        }
        catch (JSONException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return user;
    }

    protected String[] getTags(JSONObject jsonObject) throws JSONException
    {
        String[] tags = null;

        JSONArray tagsJsonArray = jsonObject.getJSONArray(JsonFields.Question.TAGS);
        if (tagsJsonArray != null)
        {
            tags = new String[tagsJsonArray.length()];

            for (int i = 0; i < tags.length; i++)
            {
                tags[i] = tagsJsonArray.getString(i);
            }
        }
        return tags;
    }
}
