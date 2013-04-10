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

package com.prasanna.android.stacknetwork.utils;

import android.app.Activity;
import android.content.Intent;

import com.prasanna.android.stacknetwork.QuestionsActivity;
import com.prasanna.android.stacknetwork.UserProfileActivity;

public class ActivityStartHelper
{

    public static void startRelatedQuestionActivity(Activity currentActivity, long questionId)
    {
        Intent questionsIntent = new Intent(currentActivity, QuestionsActivity.class);
        questionsIntent.setAction(StringConstants.RELATED);
        questionsIntent.putExtra(StringConstants.QUESTION_ID, questionId);
        currentActivity.startActivity(questionsIntent);
    }

    public static void startSimilarQuestionActivity(Activity currentActivity, String title)
    {
        Intent questionsIntent = new Intent(currentActivity, QuestionsActivity.class);
        questionsIntent.setAction(StringConstants.SIMILAR);
        questionsIntent.putExtra(StringConstants.TITLE, title);
        currentActivity.startActivity(questionsIntent);
    }

    public static void startEmailActivity(Activity currentActivity, String subject, String body)
    {
        Intent emailIntent = IntentUtils.createEmailIntent(subject, body);
        currentActivity.startActivity(Intent.createChooser(emailIntent, ""));
    }

    public static void startUserProfileActivity(Activity currentActivity, long userId)
    {
        Intent userProfileIntent = new Intent(currentActivity, UserProfileActivity.class);
        userProfileIntent.putExtra(StringConstants.USER_ID, userId);
        currentActivity.startActivity(userProfileIntent);
    }
}
