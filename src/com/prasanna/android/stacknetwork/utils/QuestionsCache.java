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

package com.prasanna.android.stacknetwork.utils;

import java.lang.ref.SoftReference;
import java.util.ArrayList;

import com.prasanna.android.cache.LRU;
import com.prasanna.android.stacknetwork.model.Answer;
import com.prasanna.android.stacknetwork.model.Question;

public class QuestionsCache
{
    private QuestionsCache()
    {
    }

    private static int CACHE_SIZE = 15;
    private static final LRU<Long, SoftReference<Question>> lru = new LRU<Long, SoftReference<Question>>(CACHE_SIZE);

    public static void add(Question question)
    {
	if (question != null && question.id > 0)
	{

	    lru.put(question.id, new SoftReference<Question>(question));
	}
    }

    public static Question get(Long id)
    {
	Question question = null;

	if (id != null && id > 0 && lru.containsKey(id))
	{
	    question = lru.get(id).get();
	    if (question == null)
		lru.remove(id);
	}

	return question;
    }

    public static void updateAnswersForQuestion(long questionId, ArrayList<Answer> answers)
    {
	if (answers != null && lru.containsKey(questionId))
	{
	    Question question = lru.get(questionId).get();
	    if (question != null)
	    {
		if (question.answers == null)
		{
		    question.answers = new ArrayList<Answer>();
		}

		question.answers.addAll(answers);
		lru.put(questionId, new SoftReference<Question>(question));
	    }
	}
    }
}
