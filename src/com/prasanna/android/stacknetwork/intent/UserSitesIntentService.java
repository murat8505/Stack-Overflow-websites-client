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
import java.util.LinkedHashMap;

import android.content.Intent;
import android.util.Log;

import com.prasanna.android.http.HttpErrorException;
import com.prasanna.android.stacknetwork.model.Account;
import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.service.UserService;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.StringConstants;

public class UserSitesIntentService extends AbstractIntentService
{
    private UserService userService = UserService.getInstance();

    public UserSitesIntentService()
    {
	this(UserSitesIntentService.class.getName());
    }

    public UserSitesIntentService(String name)
    {
	super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
	try
	{
	    handleIntent();
	}
	catch (HttpErrorException e)
	{
	    broadcastHttpErrorIntent(e.getError());
	}
    }

    private void handleIntent()
    {
	HashMap<String, Account> linkAccountsMap = null;
	LinkedHashMap<String, Site> linkSitesMap = userService.getAllSitesInNetwork();

	if (AppUtils.inAuthenticatedRealm())
	{
	    linkAccountsMap = userService.getAccounts(1);
	}

	if (linkAccountsMap != null && linkSitesMap != null)
	{
	    for (String siteUrl : linkAccountsMap.keySet())
	    {
		if (linkSitesMap.containsKey(siteUrl) == true)
		{
		    Site site = linkSitesMap.get(siteUrl);
		    Log.d("Usertype for " + siteUrl, linkAccountsMap.get(siteUrl).userType.name());
		    site.userType = linkAccountsMap.get(siteUrl).userType;
		    linkSitesMap.put(siteUrl, site);
		}
	    }
	}

	if (linkSitesMap != null)
	{
	    Intent broadcastIntent = new Intent();
	    broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	    broadcastIntent.setAction(StringConstants.SITES);
	    broadcastIntent.putExtra(StringConstants.SITES, new ArrayList<Site>(linkSitesMap.values()));
	    sendBroadcast(broadcastIntent);
	}
    }
}
