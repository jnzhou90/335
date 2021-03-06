/*
 * Copyright (c) 2017 Reto Inderbitzin (mail@indr.ch)
 *
 * For the full copyright and license information, please view
 * the LICENSE file that was distributed with this source code.
 */

package ch.indr.threethreefive.data.db.music.model;

import android.net.Uri;
import android.support.annotation.Nullable;

import ch.indr.threethreefive.libs.utils.StringUtils;

import static ch.indr.threethreefive.data.db.music.MusicStoreUtils.sanitize;

public class Album {

  private final String id;
  private final String name;
  private final String artist;
  private final String artistId;
  private final int numberOfTracks;
  private final Uri artworkUri;

  public Album(String id, String name, String artist, String artistId, int numberOfTracks,
               final @Nullable String artworkUri) {
    this.id = id;
    this.name = sanitize(name);
    this.artist = sanitize(artist);
    this.artistId = artistId;
    this.numberOfTracks = numberOfTracks;
    this.artworkUri = StringUtils.isEmpty(artworkUri) ? null : Uri.parse(artworkUri);
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getArtist() {
    return artist;
  }

  public String getArtistId() {
    return artistId;
  }

  public int getNumberOfTracks() {
    return numberOfTracks;
  }

  public @Nullable Uri getArtworkUri() {
    return artworkUri;
  }
}
