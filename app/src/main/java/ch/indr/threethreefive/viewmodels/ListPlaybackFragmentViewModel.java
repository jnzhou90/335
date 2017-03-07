/*
 * Copyright (c) 2017 Reto Inderbitzin (mail@indr.ch)
 *
 * For the full copyright and license information, please view
 * the LICENSE file that was distributed with this source code.
 */

package ch.indr.threethreefive.viewmodels;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Pair;
import android.view.View;

import ch.indr.threethreefive.libs.Environment;
import ch.indr.threethreefive.libs.utils.MediaDescriptionUtils;
import ch.indr.threethreefive.libs.utils.PlaybackStateUtils;
import ch.indr.threethreefive.ui.fragments.ListPlaybackFragment;
import rx.Observable;
import rx.subjects.BehaviorSubject;

public class ListPlaybackFragmentViewModel extends BasePlaybackFragmentViewModel<ListPlaybackFragment> {

  // OUTPUTS
  private final BehaviorSubject<Integer> visible = BehaviorSubject.create(View.GONE);
  private final BehaviorSubject<PlaybackStateUtils.PlayPauseAction> playPauseAction = BehaviorSubject.create();
  private final BehaviorSubject<String> statusText = BehaviorSubject.create();
  private final BehaviorSubject<Long> durationInMs = BehaviorSubject.create();
  private final BehaviorSubject<Boolean> autoUpdateSeekBar = BehaviorSubject.create();
  private final BehaviorSubject<PlaybackStateCompat> playbackStateCompat = BehaviorSubject.create();

  public ListPlaybackFragmentViewModel(@NonNull Environment environment) {
    super(environment);
  }

  @Override protected void onCreate(@NonNull Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);

    playbackState()
        .map(state -> state == PlaybackStateCompat.STATE_NONE ? View.GONE : View.VISIBLE)
        .distinctUntilChanged()
        .compose(bindToLifecycle())
        .subscribe(visible);

    playbackState()
        .map(PlaybackStateUtils::playPauseAction)
        .distinctUntilChanged()
        .compose(bindToLifecycle())
        .subscribe(playPauseAction);

    Observable<String> trackFullTitle = playbackClient.mediaMetadata()
        .map(metadata -> metadata == null ? null : metadata.getDescription())
        .distinctUntilChanged()
        .map(MediaDescriptionUtils::fullTitle);

    Observable.combineLatest(playbackState(), trackFullTitle, Pair::create)
        .map(this::makeStatusText)
        .distinctUntilChanged()
        .compose(bindToLifecycle())
        .subscribe(statusText);

    playbackClient.mediaMetadata()
        .map(metadata -> {
          if (metadata != null) {
            return metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
          }
          return 0L;
        })
        .compose(bindToLifecycle())
        .subscribe(durationInMs);

    playbackState()
        .map(state -> state == PlaybackStateCompat.STATE_PLAYING)
        .compose(bindToLifecycle())
        .subscribe(autoUpdateSeekBar);

    playbackClient.playbackStateCompat()
        .compose(bindToLifecycle())
        .subscribe(playbackStateCompat);
  }

  private String makeStatusText(Pair<Integer, String> statusAndTitle) {
    String title = statusAndTitle.second;
    switch (statusAndTitle.first) {
      case PlaybackStateCompat.STATE_CONNECTING:
        return "Loading: " + title;
      case PlaybackStateCompat.STATE_ERROR:
        return "Error: " + title;
    }
    return title;
  }

  // INPUTS
  public void seekTo(int position) {
    final MediaControllerCompat.TransportControls transportControls = playbackClient.transportControls();
    if (transportControls != null) {
      transportControls.seekTo(position);
    }
  }

  // OUTPUTS
  public Observable<Integer> visible() {
    return visible;
  }

  public Observable<PlaybackStateUtils.PlayPauseAction> playPauseAction() {
    return playPauseAction;
  }

  public Observable<String> statusText() {
    return statusText;
  }

  public Observable<Long> durationInMs() {
    return durationInMs;
  }

  public Observable<Boolean> autoUpdateSeekBar() {
    return autoUpdateSeekBar;
  }

  public Observable<PlaybackStateCompat> playbackStateCompat() {
    return playbackStateCompat;
  }
}
