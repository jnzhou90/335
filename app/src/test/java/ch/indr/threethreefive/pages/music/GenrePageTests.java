/*
 * Copyright (c) 2017 Reto Inderbitzin (mail@indr.ch)
 *
 * For the full copyright and license information, please view
 * the LICENSE file that was distributed with this source code.
 */

package ch.indr.threethreefive.pages.music;

import android.os.Bundle;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import ch.indr.threethreefive.R;
import ch.indr.threethreefive.TtfRobolectricTestCase;
import ch.indr.threethreefive.data.db.music.MusicStore;
import ch.indr.threethreefive.data.db.music.model.Genre;
import ch.indr.threethreefive.libs.PageItem;
import ch.indr.threethreefive.libs.PageUris;

import static org.mockito.Mockito.when;

public class GenrePageTests extends TtfRobolectricTestCase {

  private static final long GENRE_ID = 1;

  private MusicStore musicStore;

  @Override public void setUp() throws Exception {
    super.setUp();

    this.musicStore = appModule().provideMusicStore(application());
  }

  @Test
  public void onCreate_whenNoSongsFound_addsNoSongsFound() {
    when(musicStore.getGenreById(GENRE_ID)).thenReturn(new Genre(GENRE_ID, "Genre"));
    when(musicStore.querySongs(null, null)).thenReturn(new ArrayList<>());

    final GenrePage page = createPage();

    final List<PageItem> pageItems = page.getPageItems();
    assertEquals(1, pageItems.size());
    assertEquals(getString(R.string.no_songs_found), pageItems.get(0).getTitle());
  }

  private GenrePage createPage() {
    final GenrePage page = new GenrePage(environment());
    page.onCreate(context(), PageUris.musicGenre(String.valueOf(GENRE_ID)), new Bundle());
    return page;
  }
}