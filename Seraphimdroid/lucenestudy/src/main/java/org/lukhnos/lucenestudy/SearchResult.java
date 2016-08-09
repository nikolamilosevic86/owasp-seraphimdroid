package org.lukhnos.lucenestudy;

/**
 * Copyright (c) 2015 Lukhnos Liu
 *
 * Licensed under the MIT License.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;

import java.util.ArrayList;

public class SearchResult {
  public final int totalHits;
  public final ArrayList<Article> documents;
  final ScoreDoc lastScoreDoc;
  final Query query;
  final Sort sort;
  final HighlightingHelper highlightingHelper;

  SearchResult(int totalHits, ArrayList<Article> documents, ScoreDoc lastScoreDoc, Query query, Sort sort,
               HighlightingHelper highlightingHelper) {
    this.totalHits = totalHits;
    this.documents = documents;
    this.lastScoreDoc = lastScoreDoc;
    this.query = query;
    this.sort = sort;
    this.highlightingHelper = highlightingHelper;
  }

  public boolean hasMore() {
    return lastScoreDoc != null;
  }

  public String getHighlightedTitle(Article doc) {
    highlightingHelper.setFragmentLength(HighlightingHelper.DEFAULT_FRAGMENT_LENGTH);
    return highlightingHelper.highlightOrOriginal(Indexer.TITLE_FIELD_NAME, doc.title);
  }

  public String getHighlightedText(Article doc) {
    highlightingHelper.setFragmentLength(HighlightingHelper.DEFAULT_FRAGMENT_LENGTH);
    return highlightingHelper.highlightOrOriginal(Indexer.TEXT_FIELD_NAME, doc.text);
  }

}
