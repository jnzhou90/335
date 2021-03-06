/*
 * Copyright (c) 2017 Reto Inderbitzin (mail@indr.ch)
 *
 * For the full copyright and license information, please view
 * the LICENSE file that was distributed with this source code.
 */

package ch.indr.threethreefive.data.network.radioBrowser;

import android.support.annotation.NonNull;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.UrlEncodedContent;
import com.octo.android.robospice.persistence.DurationInMillis;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ch.indr.threethreefive.data.network.radioBrowser.model.Language;

public class LanguagesRequest extends RadioBrowserInfoRequest<Language[]> {

  public LanguagesRequest() {
    super(Language[].class);
  }

  @Override public long getCacheExpiryDuration() {
    return DurationInMillis.ONE_DAY;
  }

  @NonNull @Override protected HttpRequest buildHttpRequest() throws IOException {
    final Map<String, String> content = new HashMap<>();
    content.put("hidebroken", "true");
    content.put("order", "value");
    content.put("reverse", "false");

    return getHttpRequestFactory().buildPostRequest(
        makeUrlV1("/languages"), new UrlEncodedContent(content));
  }
}
