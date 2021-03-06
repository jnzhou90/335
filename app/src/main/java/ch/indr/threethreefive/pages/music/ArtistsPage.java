/*
 * Copyright (c) 2017 Reto Inderbitzin (mail@indr.ch)
 *
 * For the full copyright and license information, please view
 * the LICENSE file that was distributed with this source code.
 */

package ch.indr.threethreefive.pages.music;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.List;

import ch.indr.threethreefive.R;
import ch.indr.threethreefive.data.db.music.MusicStore;
import ch.indr.threethreefive.data.db.music.model.Artist;
import ch.indr.threethreefive.libs.Environment;
import ch.indr.threethreefive.libs.PageItemsBuilder;
import ch.indr.threethreefive.libs.PageUris;
import ch.indr.threethreefive.libs.pages.Page;

public class ArtistsPage extends Page {

  public ArtistsPage(Environment environment) {
    super(environment);
  }

  @Override public void onCreate(@NonNull Context context, @NonNull Uri uri, Bundle bundle) {
    super.onCreate(context, uri, bundle);

    setDescription(getString(R.string.artists));

    final MusicStore musicStore = new MusicStore(context());

    final List<Artist> artists = musicStore.queryArtists(null);
    if (artists.size() == 0) {
      handle(getString(R.string.no_artists_found));
      return;
    }

    final PageItemsBuilder builder = pageItemsBuilder();
    for (Artist artist : artists) {
      builder.addLink(PageUris.musicArtist(artist.getId()),
          artist.getName(),
          makeSubtitle(artist),
          makeContentDescription(artist));
    }

    setPageItems(builder);
  }

  private String makeContentDescription(Artist artist) {
    return artist.getName() + ", " + makeSubtitle(artist);
  }

  private String makeSubtitle(Artist artist) {
    return resources().getQuantityString(R.plurals.music_albums, artist.getNumberOfAlbums(), artist.getNumberOfAlbums()) +
        ", " + resources().getQuantityString(R.plurals.music_tracks, artist.getNumberOfTracks(), artist.getNumberOfTracks());
  }
}
