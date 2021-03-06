/*
    Copyright (C) 2014 Prasanna Thirumalai
    
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

package com.prasanna.android.stacknetwork.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.prasanna.android.http.SecureHttpHelper;
import com.prasanna.android.http.SecureHttpHelper.HttpResponseBodyParser;
import com.prasanna.android.http.SecureHttpHelper.HttpResponseParseException;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.IdentifiableItem;
import com.prasanna.android.stacknetwork.model.Question;
import com.prasanna.android.stacknetwork.model.StackXPage;
import com.prasanna.android.stacknetwork.model.User;
import com.prasanna.android.stacknetwork.model.User.UserType;
import com.prasanna.android.stacknetwork.utils.AppUtils;
import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.JsonFields;
import com.prasanna.android.stacknetwork.utils.OperatingSite;
import com.prasanna.android.stacknetwork.utils.StackUri;
import com.prasanna.android.stacknetwork.utils.StackUri.QueryParamDefaultValues;
import com.prasanna.android.utils.LogWrapper;

public abstract class AbstractBaseServiceHelper {
  protected abstract String getLogTag();

  public static final JSONParser JSON_PARSER = new JSONParser();

  public static class JSONParser implements HttpResponseBodyParser<JSONObjectWrapper> {

    @Override
    public JSONObjectWrapper parse(String responseBody) throws HttpResponseParseException {
      try {
        return new JSONObjectWrapper(new JSONObject(responseBody));
      } catch (JSONException e) {
        throw new HttpResponseParseException(e);
      }
    }

  }

  protected SecureHttpHelper getHttpHelper() {
    return SecureHttpHelper.getInstance();
  }

  protected void getPageInfo(JSONObjectWrapper jsonObjectWrapper, StackXPage<? extends IdentifiableItem> page) {
    if (jsonObjectWrapper != null && page != null) {
      page.quota_remaining = jsonObjectWrapper.getInt(JsonFields.QUOTA_REMAINING);
      page.quota_max = jsonObjectWrapper.getInt(JsonFields.QUOTA_MAX);
      page.hasMore = jsonObjectWrapper.getBoolean(JsonFields.HAS_MORE);
    }
  }

  protected StackXPage<User> getSerializedUserObject(JSONObjectWrapper jsonObject) {
    StackXPage<User> page = new StackXPage<User>();

    User user = null;

    if (jsonObject != null) {
      JSONArray jsonArray = jsonObject.getJSONArray(JsonFields.ITEMS);
      JSONObjectWrapper userJsonObject = JSONObjectWrapper.wrap(getIndexFromArray(jsonArray, 0, JSONObject.class));

      page.items = new ArrayList<User>();

      getPageInfo(jsonObject, page);

      if (userJsonObject != null) {
        user = new User();
        user.id = userJsonObject.getLong(JsonFields.User.USER_ID);
        user.type = UserType.toEnum(userJsonObject.getString(JsonFields.User.USER_TYPE));
        user.accountId = userJsonObject.getLong(JsonFields.User.ACCOUNT_ID);
        user.displayName = userJsonObject.getString(JsonFields.User.DISPLAY_NAME);
        user.reputation = userJsonObject.getInt(JsonFields.User.REPUTATION);
        user.profileImageLink = userJsonObject.getString(JsonFields.User.PROFILE_IMAGE);
        user.questionCount = userJsonObject.getInt(JsonFields.User.QUESTION_COUNT);
        user.answerCount = userJsonObject.getInt(JsonFields.User.ANSWER_COUNT);
        user.upvoteCount = userJsonObject.getInt(JsonFields.User.UP_VOTE_COUNT);
        user.downvoteCount = userJsonObject.getInt(JsonFields.User.DOWN_VOTE_COUNT);
        user.profileViews = userJsonObject.getInt(JsonFields.User.VIEW_COUNT);
        user.badgeCounts = getBadgeCounts(userJsonObject.getJSONObject(JsonFields.User.BADGE_COUNTS));
        user.lastAccessTime = userJsonObject.getLong(JsonFields.User.LAST_ACCESS_DATE);
        user.acceptRate = userJsonObject.getInt(JsonFields.User.ACCEPT_RATE);
        user.creationDate = userJsonObject.getLong(JsonFields.User.CREATION_DATE);
        page.items.add(user);
      }
    }

    return page;
  }

  protected int[] getBadgeCounts(JSONObjectWrapper badgeCountJsonObject) {
    int[] badgeCounts = { 0, 0, 0 };

    if (badgeCountJsonObject != null) {
      badgeCounts[0] = badgeCountJsonObject.getInt(JsonFields.BadgeCounts.GOLD);
      badgeCounts[1] = badgeCountJsonObject.getInt(JsonFields.BadgeCounts.SILVER);
      badgeCounts[2] = badgeCountJsonObject.getInt(JsonFields.BadgeCounts.BRONZE);
    }

    return badgeCounts;
  }

  protected StackXPage<Question> getQuestionModel(JSONObjectWrapper questionsJsonResponse) {
    StackXPage<Question> page = new StackXPage<Question>();
    if (questionsJsonResponse != null) {
      page.items = new ArrayList<Question>();
      getPageInfo(questionsJsonResponse, page);

      JSONArray jsonArray = questionsJsonResponse.getJSONArray(JsonFields.ITEMS);
      if (jsonArray != null) {
        for (int i = 0; i < jsonArray.length(); i++) {
          try {
            JSONObjectWrapper jsonObject = JSONObjectWrapper.wrap(jsonArray.getJSONObject(i));
            page.items.add(getSerializedQuestionObject(jsonObject));
          } catch (JSONException e) {
            LogWrapper.d(getLogTag(), e.getMessage());
          }
        }
      }
    }

    return page;
  }

  protected Question getSerializedQuestionObject(JSONObjectWrapper jsonObject) throws JSONException {
    Question question = new Question();

    question.title = jsonObject.getString(JsonFields.Question.TITLE);
    question.id = jsonObject.getLong(JsonFields.Question.QUESTION_ID);
    question.answered = jsonObject.getBoolean(JsonFields.Question.IS_ANSWERED);
    question.score = jsonObject.getInt(JsonFields.Question.SCORE);
    question.answerCount = jsonObject.getInt(JsonFields.Question.ANSWER_COUNT);
    question.viewCount = jsonObject.getInt(JsonFields.Question.VIEW_COUNT);
    question.tags = getTags(jsonObject);
    question.upvoted = jsonObject.getBoolean(JsonFields.Question.UPVOTED);
    question.downvoted = jsonObject.getBoolean(JsonFields.Question.DOWNVOTED);
    question.favorited = jsonObject.getBoolean(JsonFields.Question.FAVORITED);
    question.bountyAmount = jsonObject.getInt(JsonFields.Question.BOUNTY_AMOUNT);
    question.creationDate = jsonObject.getLong(JsonFields.Question.CREATION_DATE);
    question.link = jsonObject.getString(JsonFields.Question.LINK);

    if (jsonObject.has(JsonFields.Question.ACCEPTED_ANSWER_ID)) question.hasAcceptedAnswer = true;

    question.owner = getSerializableUserSnippetObject(jsonObject.getJSONObject(JsonFields.Question.OWNER));
    return question;
  }

  protected User getSerializableUserSnippetObject(JSONObjectWrapper userJsonObject) {
    User user = null;
    if (userJsonObject != null) {
      user = new User();
      user.id = userJsonObject.getLong(JsonFields.User.USER_ID);
      user.type = UserType.toEnum(userJsonObject.getString(JsonFields.User.USER_TYPE));
      user.displayName = userJsonObject.getString(JsonFields.User.DISPLAY_NAME);
      user.reputation = userJsonObject.getInt(JsonFields.User.REPUTATION);
      user.profileImageLink = userJsonObject.getString(JsonFields.User.PROFILE_IMAGE);
      user.acceptRate = userJsonObject.getInt(JsonFields.User.ACCEPT_RATE);
    }

    return user;
  }

  protected Answer getSerializedAnswerObject(JSONObjectWrapper jsonObject) throws JSONException {
    Answer answer = new Answer();
    answer.id = jsonObject.getLong(JsonFields.Answer.ANSWER_ID);
    answer.questionId = jsonObject.getLong(JsonFields.Answer.QUESTION_ID);
    answer.link = jsonObject.getString(JsonFields.Answer.LINK);
    answer.body = jsonObject.getString(JsonFields.Answer.BODY);
    answer.title = jsonObject.getString(JsonFields.Answer.TITLE);
    answer.score = jsonObject.getInt(JsonFields.Answer.SCORE);
    answer.creationDate = jsonObject.getLong(JsonFields.Answer.CREATION_DATE);
    answer.accepted = jsonObject.getBoolean(JsonFields.Answer.IS_ACCEPTED);
    answer.upvoted = jsonObject.getBoolean(JsonFields.Answer.UPVOTED);
    answer.downvoted = jsonObject.getBoolean(JsonFields.Answer.DOWNVOTED);
    answer.owner = getSerializableUserSnippetObject(jsonObject.getJSONObject(JsonFields.Answer.OWNER));
    return answer;
  }

  protected String[] getTags(JSONObjectWrapper jsonObject) throws JSONException {
    String[] tags = null;

    JSONArray tagsJsonArray = jsonObject.getJSONArray(JsonFields.Question.TAGS);
    if (tagsJsonArray != null) {
      tags = new String[tagsJsonArray.length()];

      for (int i = 0; i < tags.length; i++) {
        tags[i] = tagsJsonArray.getString(i);
      }
    }
    return tags;
  }

  @SuppressWarnings("unchecked")
  protected <T> T getIndexFromArray(JSONArray jsonArray, int index, Class<T> type) {
    T wrapperObject = null;

    if (jsonArray != null && jsonArray.length() > index) {
      try {
        wrapperObject = (T) jsonArray.get(index);
      } catch (JSONException e) {
        Log.w(getLogTag(), e.getMessage());
      }
    }

    return wrapperObject;
  }

  protected List<BasicNameValuePair> getBasicNameValuePartListForPost() {
    List<BasicNameValuePair> parameters = new ArrayList<BasicNameValuePair>();
    parameters.add(new BasicNameValuePair(StackUri.QueryParams.ACCESS_TOKEN, AppUtils.loadAccessToken(null)));
    parameters.add(new BasicNameValuePair(StackUri.QueryParams.KEY, StackUri.QueryParamDefaultValues.KEY));
    parameters.add(new BasicNameValuePair(StackUri.QueryParams.CLIENT_ID, QueryParamDefaultValues.CLIENT_ID));
    parameters.add(new BasicNameValuePair(StackUri.QueryParams.SITE, OperatingSite.getSite().apiSiteParameter));
    return parameters;
  }

  protected JSONObjectWrapper executeHttpGetRequest(String restEndPoint, Map<String, String> queryParams) {
    return getHttpHelper().executeHttpGet(StackUri.STACKX_API_HOST, restEndPoint, queryParams,
        SecureHttpHelper.HTTP_GZIP_RESPONSE_INTERCEPTOR, JSON_PARSER);
  }

  protected JSONObjectWrapper executeHttpPostRequest(String restEndPoint, Map<String, String> requestHeaders,
      Map<String, String> queryParams, HttpEntity httpEntity) {
    return getHttpHelper().executeHttpPost(StackUri.STACKX_API_HOST, restEndPoint, requestHeaders, queryParams,
        httpEntity, SecureHttpHelper.HTTP_GZIP_RESPONSE_INTERCEPTOR, JSON_PARSER);

  }

  protected Map<String, String> getDefaultQueryParams(String apiSiteParameter) {
    Map<String, String> queryParams = AppUtils.getDefaultQueryParams();
    if (apiSiteParameter != null) {
      queryParams.put(StackUri.QueryParams.SITE, apiSiteParameter);
    }
    queryParams.put(StackUri.QueryParams.FILTER, StackUri.QueryParamDefaultValues.ITEM_DETAIL_FILTER);
    return queryParams;
  }

  protected void sleep(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      LogWrapper.e(getLogTag(), e.getMessage());
    }
  }

}
