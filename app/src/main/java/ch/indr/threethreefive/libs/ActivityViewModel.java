/*
 * Copyright (c) 2016 Reto Inderbitzin (mail@indr.ch)
 *
 * For the full copyright and license information, please view
 * the LICENSE file that was distributed with this source code.
 */

package ch.indr.threethreefive.libs;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import com.trello.rxlifecycle.ActivityEvent;

import java.util.ArrayList;
import java.util.List;

import ch.indr.threethreefive.ui.data.ActivityResult;
import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import timber.log.Timber;

import static android.content.ContentValues.TAG;

public class ActivityViewModel<ViewType extends ActivityLifecycleType> {

  private final PublishSubject<ViewType> viewChange = PublishSubject.create();
  private final Observable<ViewType> view = viewChange.filter(v -> v != null);
  private final List<Subscription> subscriptions = new ArrayList<>();

  private final PublishSubject<ActivityResult> activityResult = PublishSubject.create();

  private final PublishSubject<Intent> intent = PublishSubject.create();
  private final Environment environment;
  // protected final Koala koala;

  public ActivityViewModel(final @NonNull Environment environment) {
    // koala = environment.koala();
    this.environment = environment;
  }

  protected Environment environment() {
    return environment;
  }

  /**
   * Takes activity result data from the activity.
   */
  public void activityResult(final @NonNull ActivityResult activityResult) {
    this.activityResult.onNext(activityResult);
  }

  /**
   * Takes intent data from the view.
   */
  public void intent(final @NonNull Intent intent) {
    this.intent.onNext(intent);
  }

  @CallSuper
  protected void onCreate(final @NonNull Context context, final @Nullable Bundle savedInstanceState) {
    Timber.d("onCreate %s", this.toString());
    dropView();
  }

  @CallSuper
  protected void onResume(final @NonNull ViewType view) {
    Timber.d("onResume %s", this.toString());
    onTakeView(view);
  }

  @CallSuper
  protected void onPause() {
    Timber.d("onPause %s", this.toString());
    dropView();
  }

  @CallSuper
  protected void onDestroy() {
    Timber.d("onDestroy %s", this.toString());
    for (final Subscription subscription : subscriptions) {
      subscription.unsubscribe();
    }
    viewChange.onCompleted();
  }

  private void onTakeView(final @NonNull ViewType view) {
    Timber.d("onTakeView %s %s", this.toString(), view.toString());
    viewChange.onNext(view);
  }

  private void dropView() {
    Timber.d("dropView %s", this.toString());
    viewChange.onNext(null);
  }

  protected @NonNull Observable<ActivityResult> activityResult() {
    return activityResult;
  }

  protected @NonNull Observable<Intent> intent() {
    return intent;
  }

  /**
   * By composing this transformer with an observable you guarantee that every observable in your view model
   * will be properly completed when the view model completes.
   *
   * It is required that *every* observable in a view model do `.compose(bindToLifecycle())` before calling
   * `subscribe`.
   */
  public @NonNull <T> Observable.Transformer<T, T> bindToLifecycle() {
    return source -> source.takeUntil(
      view.switchMap(v -> v.lifecycle().map(e -> Pair.create(v, e)))
        .filter(ve -> isFinished(ve.first, ve.second))
    );
  }

  /**
   * Determines from a view and lifecycle event if the view's life is over.
   */
  private boolean isFinished(final @NonNull ViewType view, final @NonNull ActivityEvent event) {

    if (view instanceof BaseActivity) {
      return event == ActivityEvent.DESTROY && ((BaseActivity) view).isFinishing();
    }

    return event == ActivityEvent.DESTROY;
  }
}
