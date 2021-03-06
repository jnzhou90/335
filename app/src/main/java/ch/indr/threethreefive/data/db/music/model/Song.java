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

public class Song {

  private final String id;
  private final String name;
  private final String artist;
  private final String artistId;
  private final String album;
  private final String albumId;
  private final String data;
  private final long duration;
  private final Uri albumArtworkUri;

  public Song(String id, String name, String artist, String artistId, String album, String albumId,
              String data, long duration, final @Nullable String albumArtworkUri) {
    this.id = id;
    this.name = sanitize(name);
    this.artist = sanitize(artist);
    this.artistId = artistId;
    this.album = sanitize(album);
    this.albumId = albumId;
    this.data = data;
    this.duration = duration;
    this.albumArtworkUri = StringUtils.isEmpty(albumArtworkUri) ? null : Uri.parse(albumArtworkUri);
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

  public String getAlbum() {
    return album;
  }

  public String getArtistId() {
    return artistId;
  }

  public String getAlbumId() {
    return albumId;
  }

  public String getData() {
    return data;
  }

  public long getDuration() {
    return duration;
  }

  public @Nullable Uri getAlbumArtworkUri() {
    return albumArtworkUri;
  }
}
