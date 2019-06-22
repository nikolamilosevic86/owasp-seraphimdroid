package org.owasp.lucenedroid;

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
//  final HighlightingHelper highlightingHelper;

  SearchResult(int totalHits, ArrayList<Article> documents, ScoreDoc lastScoreDoc, Query query, Sort sort) {
    this.totalHits = totalHits;
    this.documents = documents;
    this.lastScoreDoc = lastScoreDoc;
    this.query = query;
    this.sort = sort;
//    this.highlightingHelper = highlightingHelper;
  }

  public boolean hasMore() {
    return lastScoreDoc != null;
  }

  public String getHighlightedTitle(Article doc) {
//    highlightingHelper.setFragmentLength(HighlightingHelper.DEFAULT_FRAGMENT_LENGTH);
//    return highlightingHelper.highlightOrOriginal(Indexer.TITLE_FIELD_NAME, doc.title);
    return  doc.title;
  }

  public String getHighlightedText(Article doc) {
//    highlightingHelper.setFragmentLength(HighlightingHelper.DEFAULT_FRAGMENT_LENGTH);
//    return highlightingHelper.highlightOrOriginal(Indexer.TEXT_FIELD_NAME, doc.text);
    return doc.text;
  }

}
