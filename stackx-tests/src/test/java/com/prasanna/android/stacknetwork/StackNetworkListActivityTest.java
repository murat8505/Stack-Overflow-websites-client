package com.prasanna.android.stacknetwork;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowHandler;
import org.robolectric.shadows.ShadowToast;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.prasanna.android.stacknetwork.model.Site;
import com.prasanna.android.stacknetwork.model.User.UserType;
import com.prasanna.android.stacknetwork.service.UserIntentService;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.StringConstants;

@RunWith(RobolectricTestRunner.class)
public class StackNetworkListActivityTest extends AbstractBaseListActivityTest<Site> {
  private StackNetworkListActivity stackNetworkListActivity;

  public void createStackNetWorkListActivity() {
    stackNetworkListActivity = createActivity(StackNetworkListActivity.class);
    super.setContext(stackNetworkListActivity);
  }

  @Test
  public void siteListDisplayedForNonAuthUser() throws JSONException {
    ArrayList<Site> siteList = getSiteArrayListForNonAuthUser();
    createStackNetWorkListActivity();
    ListView listView = (ListView) stackNetworkListActivity.findViewById(android.R.id.list);

    ShadowActivity shadowListActivity = Robolectric.shadowOf(stackNetworkListActivity);
    assertGetUserSitesIntentServiceStarted(shadowListActivity, false);

    Bundle bundle = stubIntentServiceResponseBundle(siteList);
    stackNetworkListActivity.onReceiveResult(UserIntentService.GET_USER_SITES, bundle);
    ArrayList<View> siteListViews = assertListViewAndGetListItemViews(listView, siteList);
    assertListItemClick(siteList, siteListViews, 0);
    assertOnDefaultSiteClick(siteList, siteListViews, 0);
  }

  private Bundle stubIntentServiceResponseBundle(ArrayList<Site> siteList) {
    Bundle bundle = new Bundle();
    bundle.putSerializable(StringConstants.SITES, siteList);
    return bundle;
  }

  @Test
  public void siteListDisplayedForAuthUserWithWrite() {
    ArrayList<Site> siteList = getSiteArrayListForAuthUserWithWrite();
    AppUtils.setAccessToken(stackNetworkListActivity, "validAccessToken");
    createStackNetWorkListActivity();
    ListView listView = (ListView) stackNetworkListActivity.findViewById(android.R.id.list);

    ShadowActivity shadowListActivity = Robolectric.shadowOf(stackNetworkListActivity);
    assertGetUserSitesIntentServiceStarted(shadowListActivity, true);
    Bundle bundle = stubIntentServiceResponseBundle(siteList);
    stackNetworkListActivity.onReceiveResult(UserIntentService.GET_USER_SITES, bundle);
    ArrayList<View> siteListViews = assertListViewAndGetListItemViews(listView, siteList);
    assertListItemClick(siteList, siteListViews, 1);
  }

  private void assertListItemClick(ArrayList<Site> siteList, ArrayList<View> siteListViews, int position) {
    assertTrue(siteListViews.get(position).performClick());
    assertNextActivity(stackNetworkListActivity, QuestionsActivity.class);
    assertSame(siteList.get(position), OperatingSite.getSite());
  }

  private void assertOnDefaultSiteClick(ArrayList<Site> siteList, ArrayList<View> siteListViews, int position) {
    assertTrue(siteListViews.get(position).findViewById(R.id.isDefaultSite).performClick());
    Site defaultSite = AppUtils.getDefaultSite(stackNetworkListActivity);
    assertNotNull(defaultSite);
    assertEquals(siteList.get(position).apiSiteParameter, defaultSite.apiSiteParameter);

    ShadowHandler.idleMainLooper();
    assertEquals(ShadowToast.getTextOfLatestToast(), siteList.get(0).name + " set as default site.");
  }

  @Override
  protected View assertListItem(ListAdapter listAdpater, int position, Site site) {
    View view = listAdpater.getView(position, null, null);
    assertNotNull(view);
    TextView siteNameView = (TextView) view.findViewById(R.id.siteName);
    assertNotNull(siteNameView);
    assertEquals(site.name, siteNameView.getText().toString());

    assertRegisteredUserHint(site, view);

    ImageView isDefaultSiteImageView = (ImageView) view.findViewById(R.id.isDefaultSite);
    assertNotNull(isDefaultSiteImageView);
    return view;
  }

  private void assertRegisteredUserHint(Site site, View view) {
    if (site.userType == null || !UserType.REGISTERED.equals(site.userType))
      assertTrue(view.findViewById(R.id.siteUserTypeRegistered).getVisibility() == View.GONE);
    else if (UserType.REGISTERED.equals(site.userType))
      assertTrue(view.findViewById(R.id.siteUserTypeRegistered).getVisibility() == View.VISIBLE);
  }

  private void assertGetUserSitesIntentServiceStarted(ShadowActivity shadowListActivity, boolean me) {
    Intent nextStartedServiceIntent = assertNextStartedIntentService(shadowListActivity, UserIntentService.class);
    assertEquals(UserIntentService.GET_USER_SITES, nextStartedServiceIntent.getIntExtra(StringConstants.ACTION, -1));
    assertEquals(me, nextStartedServiceIntent.getBooleanExtra(StringConstants.ME, !me));
    assertNotNull(nextStartedServiceIntent.getParcelableExtra(StringConstants.RESULT_RECEIVER));
  }

  private ArrayList<Site> getSiteArrayListForNonAuthUser() {
    ArrayList<Site> sites = new ArrayList<Site>();
    sites.add(getSite("Stack Overflow", "stackoverflow", false, false));
    sites.add(getSite("Super User", "superuser", false, false));
    return sites;
  }

  private ArrayList<Site> getSiteArrayListForAuthUserWithWrite() {
    ArrayList<Site> sites = new ArrayList<Site>();
    sites.add(getSite("Stack Overflow", "stackoverflow", true, true));
    sites.add(getSite("Super User", "superuser", true, false));
    return sites;
  }

}
