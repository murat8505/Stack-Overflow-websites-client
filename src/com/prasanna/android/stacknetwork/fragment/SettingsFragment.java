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
package com.prasanna.android.stacknetwork.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;

import com.prasanna.android.stacknetwork.LogoutActivity;
import com.prasanna.android.stacknetwork.OAuthActivity;
import com.prasanna.android.stacknetwork.R;
import com.prasanna.android.stacknetwork.utils.AlarmUtils;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.CacheUtils;
import com.prasanna.android.stacknetwork.utils.DialogBuilder;

public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener
{
    public static final String KEY_PREF_INBOX = "pref_inbox";
    public static final String KEY_PREF_INBOX_REFRESH_INTERVAL = "pref_inboxRefreshInterval";
    public static final String KEY_PREF_INBOX_NOTIFICATION = "pref_newNotification";
    public static final String KEY_PREF_NOTIF_VIBRATE = "pref_vibrate";
    public static final String KEY_PREF_NOTIF_RINGTONE = "pref_notificationTone";
    public static final String KEY_PREF_CACHE_MAX_SIZE = "pref_cacheMaxSize";
    public static final String KEY_PREF_ACCOUNT_ACTION = "pref_accountAction";

    private static final String DEFAULT_RINGTONE = "content://settings/system/Silent";

    private ListPreference refreshIntervalPref;
    private ListPreference accountActionPref;
    private RingtonePreference notifRingTonePref;
    private EditTextPreference cacheMaxSizePreference;
    private PreferenceCategory inboxPrefCategory;

    public static int getInboxRefreshInterval(Context context)
    {
	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	return Integer.parseInt(sharedPreferences.getString(KEY_PREF_INBOX_REFRESH_INTERVAL, "-1"));
    }

    public static boolean isNotificationEnabled(Context context)
    {
	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	return sharedPreferences.getBoolean(KEY_PREF_INBOX_NOTIFICATION, false);
    }

    public static boolean isVibrateEnabled(Context context)
    {
	if (!isNotificationEnabled(context))
	    return false;

	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	return sharedPreferences.getBoolean(KEY_PREF_NOTIF_VIBRATE, false);
    }

    public static Uri getRingtone(Context context)
    {
	if (!isNotificationEnabled(context))
	    return null;

	SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
	return Uri.parse(sharedPreferences.getString(KEY_PREF_NOTIF_RINGTONE, ""));
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);

	addPreferencesFromResource(R.xml.preferences);

	setupAccountPreference();

	setupInboxPreference();

	setupCacheMaxSizePreference();
    }

    private void setupAccountPreference()
    {
	accountActionPref = (ListPreference) findPreference(KEY_PREF_ACCOUNT_ACTION);
	accountActionPref.setEntryValues(new String[0]);
	accountActionPref.setEntries(new String[0]);

	if (AppUtils.inAuthenticatedRealm())
	{
	    setupLogoutPreference();
	}
	else
	{
	    setupLoginPreference();
	}
    }

    private void setupLogoutPreference()
    {
	accountActionPref.setTitle(getString(R.string.logout));
	accountActionPref.setOnPreferenceClickListener(new OnPreferenceClickListener()
	{
	    private DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
	    {
		@Override
		public void onClick(DialogInterface dialog, int which)
		{
		    switch (which)
		    {
			case DialogInterface.BUTTON_POSITIVE:
			    Intent logoutIntent = new Intent(getActivity(), LogoutActivity.class);
			    logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			    logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			    startActivity(logoutIntent);
			    break;

			case DialogInterface.BUTTON_NEGATIVE:
			    dialog.dismiss();
			    break;
		    }
		}
	    };

	    @Override
	    public boolean onPreferenceClick(Preference preference)
	    {
		accountActionPref.getDialog().dismiss();

		AlertDialog yesNoDialog = DialogBuilder.yesNoDialog(getActivity(), R.string.logoutMsg,
		                dialogClickListener);
		yesNoDialog.show();

		return true;
	    }
	});
    }

    private void setupLoginPreference()
    {
	accountActionPref.setTitle(getString(R.string.login));
	accountActionPref.setOnPreferenceClickListener(new OnPreferenceClickListener()
	{
	    @Override
	    public boolean onPreferenceClick(Preference preference)
	    {
		accountActionPref.getDialog().dismiss();

		Intent oAuthIntent = new Intent(getActivity(), OAuthActivity.class);
		CacheUtils.clear(getActivity());
		startActivity(oAuthIntent);
		return true;
	    }
	});
    }

    private void setupInboxPreference()
    {
	inboxPrefCategory = (PreferenceCategory) findPreference(KEY_PREF_INBOX);
	inboxPrefCategory.setEnabled(AppUtils.inAuthenticatedRealm());
	refreshIntervalPref = (ListPreference) findPreference(KEY_PREF_INBOX_REFRESH_INTERVAL);
	refreshIntervalPref.setSummary(refreshIntervalPref.getEntry());

	setupRingtonePreference();
    }

    private void setupCacheMaxSizePreference()
    {
	String currentCacheSize = CacheUtils.getHumanReadableCacheSize(getActivity().getCacheDir());
	
	cacheMaxSizePreference = (EditTextPreference) findPreference(KEY_PREF_CACHE_MAX_SIZE);
	cacheMaxSizePreference.setSummary(cacheMaxSizePreference.getText() + getString(R.string.MB) + ". Used: "
	                + currentCacheSize);
    }

    private void setupRingtonePreference()
    {
	notifRingTonePref = (RingtonePreference) findPreference(KEY_PREF_NOTIF_RINGTONE);
	notifRingTonePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
	{
	    @Override
	    public boolean onPreferenceChange(Preference preference, Object newValue)
	    {
		setRingtoneSummary(Uri.parse((String) newValue));
		return true;
	    }
	});

	Uri ringtoneUri = Uri.parse(notifRingTonePref.getSharedPreferences().getString(KEY_PREF_NOTIF_RINGTONE,
	                DEFAULT_RINGTONE));
	setRingtoneSummary(ringtoneUri);
    }

    @Override
    public void onResume()
    {
	super.onResume();
	getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause()
    {
	super.onPause();
	getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
	if (key.equals(KEY_PREF_INBOX_REFRESH_INTERVAL))
	{
	    refreshIntervalPref.setSummary(refreshIntervalPref.getEntry());
	    AlarmUtils.rescheduleInboxRefreshAlarm(getActivity());
	}
	else if (key.equals(KEY_PREF_CACHE_MAX_SIZE))
	{
	    String cacheSize = sharedPreferences.getString(key, "");
	    findPreference(KEY_PREF_CACHE_MAX_SIZE).setSummary(cacheSize);
	}
    }

    private void setRingtoneSummary(Uri uri)
    {
	Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), uri);
	notifRingTonePref.setSummary(ringtone.getTitle(getActivity()));
    }
}
