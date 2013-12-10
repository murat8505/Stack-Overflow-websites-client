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

package com.prasanna.android.views;

import android.content.Context;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.TextView;

public class HtmlTextView extends TextView {
  public HtmlTextView(Context context) {
    super(context);
  }

  public HtmlTextView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public HtmlTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public void setText(CharSequence text, BufferType type) {
    super.setText(Html.fromHtml(text.toString()), type);
  }

}
