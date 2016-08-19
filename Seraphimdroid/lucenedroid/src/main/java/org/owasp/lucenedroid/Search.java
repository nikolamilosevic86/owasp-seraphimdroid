package org.owasp.lucenedroid;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.lukhnos.portmobile.file.Path;
import org.lukhnos.portmobile.file.Paths;

import java.io.IOException;
import java.util.ArrayList;

public class Search implements AutoCloseable {
  final Analyzer analyzer;
  final IndexReader indexReader;

  public Search(String indexRoot) throws IOException {
    Path indexRootPath = Paths.get(indexRoot);
    analyzer = Indexer.getAnalyzer();
    Directory mainIndexDir = FSDirectory.open(Indexer.getMainIndexPath(indexRootPath));
    indexReader = DirectoryReader.open(mainIndexDir);
  }

  public SearchResult search(String queryStr, int maxCount) throws ParseException, IOException {
    return search(queryStr, null, maxCount);
  }

  public SearchResult search(String queryStr, SortBy sortBy, int maxCount)
      throws ParseException, IOException {
    String[] fields = { Indexer.TITLE_FIELD_NAME, Indexer.TEXT_FIELD_NAME, Indexer.CATEGORY_FIELD_NAME, Indexer.TAGS_FIELD_NAME };
    QueryParser parser = new MultiFieldQueryParser(fields, analyzer);
    Query query = parser.parse(queryStr);

    Sort sort = null;
    if (sortBy != null) {
      sort = sortBy.sort;
    }

    return searchAfter(null, query, sort, maxCount);
  }

  public SearchResult searchAfter(SearchResult result, int maxCount) throws IOException {
    if (!result.hasMore()) {
      throw new AssertionError("No more search results to be fetched after this");
    }

    return searchAfter(result.lastScoreDoc, result.query, result.sort, maxCount);
  }

  @Override
  public void close() throws Exception {
    indexReader.close();
  }

  SearchResult searchAfter(ScoreDoc lastScoreDoc, Query query, Sort sort, int maxCount)
      throws IOException {
    if (maxCount < 1) {
      throw new AssertionError("maxCount must be at least 1, but instead: " + maxCount);
    }

    IndexSearcher searcher = new IndexSearcher(indexReader);

    TopDocs topDocs;
    int actualMaxCount = maxCount + 1;
    if (lastScoreDoc == null) {
      if (sort == null) {
        topDocs = searcher.search(query, actualMaxCount);
      } else {
        topDocs = searcher.search(query, actualMaxCount, sort);
      }
    } else {
      if (sort == null) {
        topDocs = searcher.searchAfter(lastScoreDoc, query, actualMaxCount);
      } else {
        topDocs = searcher.searchAfter(lastScoreDoc, query, actualMaxCount, sort);
      }
    }

    ScoreDoc nextSearchAfterDoc = null;
    int topDocsLen;
    if (topDocs.scoreDocs.length > maxCount) {
      nextSearchAfterDoc = topDocs.scoreDocs[maxCount - 1];
      topDocsLen = maxCount;
    } else {
      topDocsLen = topDocs.scoreDocs.length;
    }

//    HighlightingHelper highlightingHelper = new HighlightingHelper(query, analyzer);

    ArrayList<Article> docs = new ArrayList<>();
    for (int i = 0; i < topDocsLen; i++) {
      org.apache.lucene.document.Document luceneDoc = indexReader.document(topDocs.scoreDocs[i].doc);
      Article doc = Indexer.fromLuceneDocument(luceneDoc);
      docs.add(doc);
    }

    return new SearchResult(topDocs.totalHits, docs, nextSearchAfterDoc, query, sort);
  }

  public enum SortBy {
    RELEVANCE(Sort.RELEVANCE),
    DOCUMENT_ORDER(Sort.INDEXORDER),
    TITLE(new Sort(new SortField(Indexer.TITLE_FIELD_NAME, SortField.Type.STRING))),
    TEXT(new Sort(new SortField(Indexer.TEXT_FIELD_NAME, SortField.Type.STRING, true))),
    CATEGORY(new Sort(new SortField(Indexer.CATEGORY_FIELD_NAME, SortField.Type.STRING, true))),
    TAGS(new Sort(new SortField(Indexer.TAGS_FIELD_NAME, SortField.Type.STRING, true)));

    final Sort sort;
    SortBy(Sort sort) {
      this.sort = sort;
    }
  }
}
