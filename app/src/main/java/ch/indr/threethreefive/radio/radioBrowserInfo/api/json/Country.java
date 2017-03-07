/*
 * Copyright (c) 2016 Reto Inderbitzin (mail@indr.ch)
 *
 * For the full copyright and license information, please view
 * the LICENSE file that was distributed with this source code.
 */

package ch.indr.threethreefive.radio.radioBrowserInfo.api.json;

import com.google.api.client.util.Key;

public class Country {

  @Key private String name;

  @Key private String value;

  @Key private String stationcount;

  public Country() {
  }

  public Country(String value) {
    this.name = value;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public int getStationCount() {
    return stationcount == null ? 0 : Integer.parseInt(stationcount);
  }
}

