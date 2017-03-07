/*
 * Copyright (c) 2016 Reto Inderbitzin (mail@indr.ch)
 *
 * For the full copyright and license information, please view
 * the LICENSE file that was distributed with this source code.
 */

package ch.indr.threethreefive.radio.pages;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import ch.indr.threethreefive.libs.Environment;
import ch.indr.threethreefive.libs.PageItemsBuilder;
import ch.indr.threethreefive.libs.PageUris;
import ch.indr.threethreefive.navigation.SpiceBasePage;
import ch.indr.threethreefive.radio.radioBrowserInfo.api.StationsRequest;
import ch.indr.threethreefive.radio.radioBrowserInfo.api.json.Station;

public class CountryPage extends SpiceBasePage implements RequestListener<Station[]> {

  private String country;

  public CountryPage(Environment environment) {
    super(environment);
  }

  @Override public void onCreate(@NonNull Context context, Uri uri, Bundle bundle) {
    super.onCreate(context, uri, bundle);

    country = bundle.getString("id");
    setTitle(country != null ? country : "Country");
  }

  @Override public void onStart() {
    super.onStart();

    executeRequest(StationsRequest.byCountry(country), this);
  }

  @Override public void onRequestFailure(SpiceException spiceException) {
    handle(spiceException);
  }

  @Override public void onRequestSuccess(Station[] response) {
    final PageItemsBuilder builder = pageItemsBuilder();
    builder.addToggleFavorite(getCurrentPageLink());

    for (Station station : response) {
      builder.addLink(PageUris.makeStationUri(station.getId()), station.getName());
    }

    setPageItems(builder);
  }
}
