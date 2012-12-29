/*
    Copyright (C) 2012 Prasanna Thirumalai
    
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

package com.prasanna.android.stacknetwork.intent;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.util.Log;

import com.prasanna.android.http.HttpErrorException;
import com.prasanna.android.stacknetwork.model.InboxItem;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.service.UserService;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum.UserIntentAction;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class UserIntentService extends AbstractIntentService
{
    private static final String TAG = UserIntentService.class.getSimpleName();
    public static final int GET_USER_PROFILE = 1;
    public static final int GET_USER_QUESTIONS = 2;
    public static final int GET_USER_ANSWERS = 3;
    public static final int GET_USER_INBOX = 4;
    public static final int GET_USER_UNREAD_INBOX = 5;

    private UserService userService = UserService.getInstance();

    public UserIntentService()
    {
	this(TAG);
    }

    public UserIntentService(String name)
    {
	super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
	int action = intent.getIntExtra(StringConstants.ACTION, -1);
	int page = intent.getIntExtra(StringConstants.PAGE, 1);
	boolean me = intent.getBooleanExtra(StringConstants.ME, false);
	long userId = intent.getLongExtra(StringConstants.USER_ID, -1);

	try
	{
	    switch (action)
	    {
		case GET_USER_PROFILE:
		    Log.d(TAG, "getUserDetail");
		    getUserDetail(me, userId, page);
		    break;
		case GET_USER_QUESTIONS:
		    Log.d(TAG, "getQuestions");
		    getQuestions(me, userId, page);
		    break;
		case GET_USER_ANSWERS:
		    Log.d(TAG, "getAnswers");
		    getAnswers(me, userId, page);
		    break;
		case GET_USER_INBOX:
		    broadcastSerializableExtra(UserIntentAction.INBOX.name(), UserIntentAction.INBOX.getExtra(),
			            userService.getInbox(page));
		    break;
		case GET_USER_UNREAD_INBOX:
		    getUnreadInboxItems(intent);
		    break;
		default:
		    Log.e(TAG, "Unknown action: " + action);
		    break;
	    }
	}
	catch (HttpErrorException e)
	{
	    broadcastHttpErrorIntent(e.getError());
	}
    }

    private void getUserDetail(boolean me, long userId, int page)
    {
	try
	{

	    if (me)
	    {
		broadcastSerializableExtra(UserIntentAction.USER_DETAIL.name(),
		                UserIntentAction.USER_DETAIL.getExtra(), userService.getMe());
	    }
	    else
	    {
		broadcastSerializableExtra(UserIntentAction.USER_DETAIL.name(),
		                UserIntentAction.USER_DETAIL.getExtra(), userService.getUserById(userId));
	    }
	}
	catch (HttpErrorException e)
	{
	    broadcastHttpErrorIntent(e.getError());
	}

	fetchUserAccountsAndBroadcast(me, userId);
    }

    private void fetchUserAccountsAndBroadcast(boolean me, long userId)
    {
	if (me)
	{
	    broadcastSerializableExtra(UserIntentAction.USER_ACCOUNTS.name(),
		            UserIntentAction.USER_ACCOUNTS.getExtra(), userService.getAccounts(1));
	}
	else
	{
	    broadcastSerializableExtra(UserIntentAction.USER_ACCOUNTS.name(),
		            UserIntentAction.USER_ACCOUNTS.getExtra(), userService.getAccounts(userId, 1));
	}
    }

    private void getQuestions(boolean me, long userId, int page)
    {
	if (me)
	{
	    broadcastSerializableExtra(UserIntentAction.QUESTIONS_BY_USER.name(),
		            UserIntentAction.QUESTIONS_BY_USER.getExtra(), userService.getMyQuestions(page));
	}
	else
	{
	    if (userId > 0)
	    {
		broadcastSerializableExtra(UserIntentAction.QUESTIONS_BY_USER.name(),
		                UserIntentAction.QUESTIONS_BY_USER.getExtra(),
		                userService.getQuestionsByUser(userId, page));
	    }
	}
    }

    private void getAnswers(boolean me, long userId, int page)
    {
	if (me)
	{
	    broadcastSerializableExtra(UserIntentAction.ANSWERS_BY_USER.name(),
		            UserIntentAction.ANSWERS_BY_USER.getExtra(), userService.getMyAnswers(page));
	}
	else
	{
	    if (userId > 0)
	    {
		broadcastSerializableExtra(UserIntentAction.ANSWERS_BY_USER.name(),
		                UserIntentAction.ANSWERS_BY_USER.getExtra(), userService.getAnswersByUser(userId, page));
	    }
	}
    }

    @SuppressWarnings("unchecked")
    private void getUnreadInboxItems(Intent intent)
    {
	int totalNewMsgs = 0;
	int page = intent.getIntExtra(StringConstants.PAGE, 1);
	HashMap<String, Integer> newMsgCount = new HashMap<String, Integer>();
	HashMap<String, Site> sites = (HashMap<String, Site>) intent.getSerializableExtra(UserIntentAction.SITES
	                .getExtra());

	for (Site site : sites.values())
	{
	    ArrayList<InboxItem> unreadInboxItems = userService.getUnreadItemsInInbox(page, site);
	    if (unreadInboxItems != null && !unreadInboxItems.isEmpty())
	    {
		newMsgCount.put(site.name, unreadInboxItems.size());
		totalNewMsgs += unreadInboxItems.size();
	    }
	}

	broadcastUnreadItemsCount(totalNewMsgs, newMsgCount);
    }

    private void broadcastUnreadItemsCount(int totalNewMsgs, HashMap<String, Integer> newMsgCount)
    {
	Intent broadcastIntent = new Intent();
	broadcastIntent.setAction(UserIntentAction.NEW_MSG.name());
	broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	broadcastIntent.putExtra(UserIntentAction.NEW_MSG.getExtra(), newMsgCount);
	broadcastIntent.putExtra(UserIntentAction.TOTAL_NEW_MSGS.getExtra(), totalNewMsgs);
	sendBroadcast(broadcastIntent);
    }
}
