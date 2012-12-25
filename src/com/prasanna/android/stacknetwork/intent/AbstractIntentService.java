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

import java.io.Serializable;

import com.prasanna.android.stacknetwork.model.StackXError;
import com.prasanna.android.stacknetwork.utils.IntentActionEnum;
import com.prasanna.android.stacknetwork.utils.StringConstants;

import android.app.IntentService;
import android.content.Intent;

public abstract class AbstractIntentService extends IntentService
{

    public AbstractIntentService(String name)
    {
	super(name);
    }

    protected void broadcastHttpErrorIntent(StackXError error)
    {
	Intent broadcastIntent = new Intent();
	broadcastIntent.setAction(IntentActionEnum.ErrorIntentAction.HTTP_ERROR.name());
	broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	broadcastIntent.putExtra(StringConstants.ERROR, error);
	sendBroadcast(broadcastIntent);
    }

    protected void broadcastIntent(String action, String extraName, Serializable extra)
    {
	Intent broadcastIntent = new Intent();
	broadcastIntent.setAction(action);
	broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
	broadcastIntent.putExtra(extraName, extra);
	sendBroadcast(broadcastIntent);
    }
}