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

import org.apache.lucene.search.highlight.Encoder;

/**
 * A simplified version of Lucene's SimpleHTMLEncoder.
 *
 * This implementation only escapes three XML entity characters.
 */
public class MinimalHTMLEncoder implements Encoder {
  public final static String htmlEncode(String plainText) {
    if (plainText == null || plainText.length() == 0) {
      return "";
    }

    StringBuilder result = new StringBuilder(plainText.length());
    for (int index = 0; index < plainText.length(); index++) {
      char ch = plainText.charAt(index);

      switch (ch) {
        case '&':
          result.append("&amp;");
          break;
        case '<':
          result.append("&lt;");
          break;
        case '>':
          result.append("&gt;");
          break;
        default:
          result.append(ch);
      }
    }

    return result.toString();
  }

  @Override
  public String encodeText(String originalText) {
    return htmlEncode(originalText);
  }
}
