/*
    Copyright 2012 Prasanna Thirumalai
    
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

import android.content.Context;
import android.content.Intent;

import com.prasanna.android.stacknetwork.StackNetworkListActivity;
import com.prasanna.android.stacknetwork.UserProfileActivity;
import com.prasanna.android.stacknetwork.model.User;

public class IntentUtils
{
    public static Intent createUserProfileIntent(Context context, long userId)
    {
	Intent userProfileIntent = new Intent(context, UserProfileActivity.class);
	User user = new User();
	user.id = userId;
	userProfileIntent.putExtra(StringConstants.USER, user);
	return userProfileIntent;
    }

    public static Intent createUserProfileIntent(Context context, String accessToken)
    {
	Intent userProfileIntent = new Intent(context, UserProfileActivity.class);
	User user = new User();
	user.accessToken = accessToken;
	userProfileIntent.putExtra(StringConstants.USER, user);
	return userProfileIntent;
    }

    public static Intent createSiteListIntent(Context context)
    {
	Intent listStackNetworkIntent = new Intent(context, StackNetworkListActivity.class);
	return listStackNetworkIntent.putExtra("allSites", true);
    }

    public static Intent createEmailIntent(String subject, String body)
    {
	String type = "plain/text";
	Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
	emailIntent.setType(type);
	emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
	emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
	return emailIntent;
    }
}
