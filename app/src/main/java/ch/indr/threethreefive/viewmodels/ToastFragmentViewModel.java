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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import ch.indr.threethreefive.libs.Environment;
import ch.indr.threethreefive.libs.FragmentViewModel;
import ch.indr.threethreefive.libs.utils.ObjectUtils;
import ch.indr.threethreefive.services.ToastManager;
import ch.indr.threethreefive.ui.fragments.ToastFragment;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

import static ch.indr.threethreefive.services.ToastManager.Toast;

public class ToastFragmentViewModel extends FragmentViewModel<ToastFragment> {

  private static final int TOAST_LENGTH = 3000;
  private static final int ANIMATION_LENGTH = 500;

  private final ToastManager toastManager;

  private Toast isShowing = null;
  private boolean autoHideToast = true;

  private final Queue<Toast> toasts = new ConcurrentLinkedQueue<>();
  private final PublishSubject<Object> pollQueue = PublishSubject.create();
  private final PublishSubject<Toast> autoHide = PublishSubject.create();

  // OUTPUTS
  private PublishSubject<Toast> hideToast = PublishSubject.create();
  private PublishSubject<Toast> showToast = PublishSubject.create();

  public ToastFragmentViewModel(@NonNull Environment environment) {
    super(environment);

    this.toastManager = environment.toastManager();
  }

  @Override protected void onCreate(@NonNull Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);

    // When a toast is shown, we save its instance
    showToast
        .observeOn(AndroidSchedulers.mainThread())
        .compose(bindToLifecycle())
        .subscribe(toast -> {
          autoHideToast = true;
          isShowing = toast;
        });

    // Hide current toast when a new toast is incoming
    pollQueue.filter(__ -> ObjectUtils.isNotNull(isShowing))
        .observeOn(AndroidSchedulers.mainThread())
        .compose(bindToLifecycle())
        .map(__ -> isShowing)
        .subscribe(hideToast);

    // Show toast immediately if no toast is already showing
    pollQueue.filter(__ -> ObjectUtils.isNull(isShowing))
        .observeOn(AndroidSchedulers.mainThread())
        .compose(bindToLifecycle())
        .map(__ -> toasts.poll())
        .filter(ObjectUtils::isNotNull)
        .subscribe(showToast);

    // Auto hide toast after given length
    showToast.mergeWith(autoHide)
        .observeOn(AndroidSchedulers.mainThread())
        .debounce(TOAST_LENGTH, TimeUnit.MILLISECONDS)
        .filter(__ -> autoHideToast)
        .filter(toast -> toast.equals(isShowing))
        .compose(bindToLifecycle())
        .subscribe(hideToast);

    // 4. After hiding the current toast, we trigger pollQueue
    hideToast.delay(ANIMATION_LENGTH, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext(__ -> isShowing = null)
        .compose(bindToLifecycle())
        .subscribe(pollQueue);
  }

  @Override protected void onResume(@NonNull ToastFragment view) {
    super.onResume(view);

    toastManager.addToastListener(toastListener);
  }

  @Override protected void onPause() {
    super.onPause();

    toastManager.removeToastListener(toastListener);
  }

  public Observable<Toast> hideToast() {
    return hideToast;
  }

  public Observable<Toast> showToast() {
    return showToast;
  }

  public void toastClicked() {
    hideToast.onNext(isShowing);
  }

  public void toastTouched() {
    autoHide.onNext(isShowing);
    // autoHideToast = false;
  }

  private ToastManager.ToastListener toastListener = new ToastManager.ToastListener() {
    @Override public void toast(Toast toast) {
      toasts.add(toast);
      pollQueue.onNext(null);
    }
  };
}
