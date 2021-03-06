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

package com.prasanna.android.stacknetwork.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.prasanna.android.stacknetwork.model.User.UserType;
import com.prasanna.android.stacknetwork.utils.JSONObjectWrapper;
import com.prasanna.android.stacknetwork.utils.JsonFields;

public class Site implements Serializable {
  private static final long serialVersionUID = 7057957647981597410L;

  public long dbId;

  public String name;

  public String apiSiteParameter;

  public String audience;

  public String link;

  public int headerResId;

  public String logoUrl;

  public String iconUrl;

  public String faviconUrl;

  public UserType userType = UserType.DOES_NOT_EXIST;

  public long userId = -1;

  public static Site parses(JSONObjectWrapper siteJsonObject) {
    if (siteJsonObject == null)
      return null;

    Site site = new Site();
    site.apiSiteParameter = siteJsonObject.getString(JsonFields.Site.API_SITE_PARAMETER);
    site.logoUrl = siteJsonObject.getString(JsonFields.Site.LOGO_URL);
    site.name = siteJsonObject.getString(JsonFields.Site.NAME);
    site.audience = siteJsonObject.getString(JsonFields.Site.AUDIENCE);
    site.link = siteJsonObject.getString(JsonFields.Site.SITE_URL);
    site.faviconUrl = siteJsonObject.getString(JsonFields.Site.FAVICON_URL);
    site.iconUrl = siteJsonObject.getString(JsonFields.Site.ICON_URL);
    return site;
  }

  public JSONObject toJson() throws JSONException {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put(JsonFields.Site.API_SITE_PARAMETER, apiSiteParameter);
    jsonObject.put(JsonFields.Site.NAME, name);
    return jsonObject;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((apiSiteParameter == null) ? 0 : apiSiteParameter.hashCode());
    result = prime * result + ((link == null) ? 0 : link.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Site other = (Site) obj;
    if (apiSiteParameter == null) {
      if (other.apiSiteParameter != null)
        return false;
    } else if (!apiSiteParameter.equals(other.apiSiteParameter))
      return false;
    if (link == null) {
      if (other.link != null)
        return false;
    } else if (!link.equals(other.link))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

}
