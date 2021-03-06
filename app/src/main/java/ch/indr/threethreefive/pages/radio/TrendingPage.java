/*
 * Copyright (c) 2017 Reto Inderbitzin (mail@indr.ch)
 *
 * For the full copyright and license information, please view
 * the LICENSE file that was distributed with this source code.
 */

package ch.indr.threethreefive.pages.radio;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Comparator;
import java.util.List;

import ch.indr.threethreefive.R;
import ch.indr.threethreefive.data.network.radioBrowser.model.Station;
import ch.indr.threethreefive.libs.Environment;
import ch.indr.threethreefive.libs.PageItemsBuilder;
import ch.indr.threethreefive.libs.PageUris;

public class TrendingPage extends StationListBasePage {

  public TrendingPage(Environment environment) {
    super(environment);
  }

  @Override public void onCreate(@NonNull Context context, @NonNull Uri uri, Bundle bundle) {
    super.onCreate(context, uri, bundle);
    component().inject(this);

    setDescription(getString(R.string.radio_trending_title));
  }

  @Override public void onStart() {
    super.onStart();

    apiClient.getTrendingStations(MAX_NUMBER_OF_MORE_STATIONS, this);
  }

  @Override protected void populateLists(@NonNull List<Station> allStations) {
    expander.add(allStations, getString(R.string.show_all_stations));
  }

  @Override protected void addPageItems(PageItemsBuilder builder, List<Station> stations) {
    if (stations.size() == 0) {
      builder.addText(getString(R.string.no_stations_found));
      return;
    }

    for (Station station : stations) {
      builder.addLink(PageUris.radioStation(station.getId()),
          station.getName(),
          station.makeSubtitle("CLG"),
          station.makeContentDescription("CLG"),
          station.getLogoUri(),
          R.drawable.ic_radio_grey600_36dp);
    }
  }

  @Nullable @Override protected Comparator<Station> getStationsComparator() {
    // API returns stations already sorted by click trend and/or click trend.
    return null;
  }
}
