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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Encoder;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;

import java.io.IOException;
import java.util.regex.Pattern;

class HighlightingHelper {
  // For removing any leading spaces or punctuations except "<" since <B> is used for highlights.
  static final Pattern cleanUpPattern = Pattern.compile(
      "^[\\s\\p{Punct}&&[^<]]+");

  // For concatenating paragraphes separated by \n.
  static final Pattern replaceLFPattern = Pattern.compile("\\n");

  public static final int DEFAULT_FRAGMENT_LENGTH = 120;

  final QueryScorer scorer;
  final Highlighter highlighter;
  final Analyzer analyzer;
  int fragmentLength;

  HighlightingHelper(Query query, Analyzer analyzer) {
    this.analyzer = analyzer;

    Formatter formatter = new SimpleHTMLFormatter();
    Encoder encoder = new MinimalHTMLEncoder();
    scorer = new QueryScorer(query);
    highlighter = new Highlighter(formatter, encoder, scorer);

    fragmentLength = DEFAULT_FRAGMENT_LENGTH;
    Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, fragmentLength);
    highlighter.setTextFragmenter(fragmenter);
  }

  int getFragmentLength() {
    return fragmentLength;
  }

  void setFragmentLength(int length) {
    if (length < 1) {
      throw new AssertionError("length must be at least 1");
    }

    // Create a new fragmenter if the length is different.
    if (fragmentLength != length) {
      Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, length);
      highlighter.setTextFragmenter(fragmenter);
      fragmentLength = length;
    }
  }

  String highlightOrOriginal(String fieldName, String text) {
    if (text == null) {
      return null;
    }

    String highlighted = highlight(fieldName, text);
    if (highlighted != null) {
      return highlighted;
    }

    // The text has no highlights. We count the codepoints for the fallback substring.
    int cpCount = text.codePointCount(0, text.length());
    if (cpCount < fragmentLength) {
      return text;
    }

    int index = text.offsetByCodePoints(0, fragmentLength - 1);
    return text.substring(0, index) + "…";
  }

  String highlight(String fieldName, String text) {
    if (text == null) {
      return null;
    }

    try {
      String highlighted = highlighter.getBestFragment(analyzer, fieldName, text);
      if (highlighted == null) {
        return null;
      }

      highlighted = cleanUpPattern.matcher(highlighted).replaceAll("");
      highlighted = replaceLFPattern.matcher(highlighted).replaceAll(" ");
      if (highlighted.isEmpty()) {
        highlighted = null;
      }

      return highlighted;
    } catch (InvalidTokenOffsetsException|IOException e) {
      return null;
    }
  }
}
