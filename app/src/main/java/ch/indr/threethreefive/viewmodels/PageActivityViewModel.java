/*
 * Copyright (c) 2016 Reto Inderbitzin (mail@indr.ch)
 *
 * For the full copyright and license information, please view
 * the LICENSE file that was distributed with this source code.
 */

package ch.indr.threethreefive.viewmodels;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;

import java.util.List;
import java.util.Stack;

import ch.indr.threethreefive.BuildConfig;
import ch.indr.threethreefive.libs.ActivityLifecycleType;
import ch.indr.threethreefive.libs.ActivityViewModel;
import ch.indr.threethreefive.libs.Description;
import ch.indr.threethreefive.libs.Environment;
import ch.indr.threethreefive.libs.PageCommand;
import ch.indr.threethreefive.libs.PageItem;
import ch.indr.threethreefive.libs.PageLink;
import ch.indr.threethreefive.libs.PageManager;
import ch.indr.threethreefive.libs.pages.Page;
import ch.indr.threethreefive.libs.pages.PageTransition;
import ch.indr.threethreefive.libs.utils.ObjectUtils;
import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public abstract class PageActivityViewModel<ViewType extends ActivityLifecycleType> extends ActivityViewModel<ViewType> {

  protected final Stack<Page> pageStack = new Stack<>();

  // OUTPUTS
  protected final PublishSubject<Object> goBack = PublishSubject.create();
  protected final BehaviorSubject<Boolean> canGoUp = BehaviorSubject.create(false);
  protected final BehaviorSubject<String> pageTitle = BehaviorSubject.create();
  protected final BehaviorSubject<List<PageItem>> pageItems = BehaviorSubject.create((List<PageItem>) null);
  protected final PublishSubject<PageLink> showPage = PublishSubject.create();

  protected final BehaviorSubject<Page> page = BehaviorSubject.create();

  public PageActivityViewModel(@NonNull Environment environment) {
    super(environment);
  }

  @Override protected void onCreate(@NonNull Context context, @Nullable Bundle savedInstanceState) {
    super.onCreate(context, savedInstanceState);

    intent().map(PageTransition::fromIntent)
        .filter(ObjectUtils::isNotNull)
        .startWith(new PageTransition(PageLink.HomePage.getUri(), null))
        .map(transition -> Pair.create(transition, PageManager.fetch(context, transition)))
        .compose(bindToLifecycle())
        .subscribe(this::transitionTo);

    page.switchMap(Page::pageItems)
        .compose(bindToLifecycle())
        .subscribe(pageItems);

    page.switchMap(Page::description)
        .map(Description::getTitle)
        .compose(bindToLifecycle())
        .subscribe(pageTitle);

    page.switchMap(Page::parentPageLink)
        .map(ObjectUtils::isNotNull)
        .compose(bindToLifecycle())
        .subscribe(canGoUp);

    page.switchMap(Page::transitionTo)
        .map(transition -> Pair.create(transition, PageManager.fetch(context, transition)))
        .compose(bindToLifecycle())
        .subscribe(this::transitionTo);
  }

  private void reportPageView(final @Nullable Page page) {
    if (page == null) return;
    try {
      final Uri pageUri = page.getPageUri();
      final String contentId = pageUri.toString().replace("//ch.indr.threethreefive", "");
      Timber.d("reportPageView content id %s, %s", contentId, this.toString());

      if (BuildConfig.ANSWERS) {
        Answers.getInstance().logContentView(new ContentViewEvent()
            .putContentId(contentId)
            .putContentName(page.getTitle())
            .putContentType(page.getClass().getName().replace("ch.indr.threethreefive.", "")));
      }
    } catch (Exception ex) {
      Timber.e(ex, "Error logging content view event");
    }
  }

  @Override protected void onPause() {
    super.onPause();

    Page page = this.page.getValue();
    if (page != null) {
      PageManager.pause(page);
    }
  }

  @Override protected void onResume(@NonNull ViewType view) {
    super.onResume(view);

    Page page = this.page.getValue();
    if (page != null) {
      PageManager.resume(page);
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();

    PageManager.destroy(pageStack);
  }

  private void transitionTo(final @NonNull Pair<PageTransition, Page> transition) {
    final PageTransition pageTransition = transition.first;
    final Page newPage = transition.second;
    final Page oldPage = this.page.getValue();

    beforeTransition(pageTransition, newPage);
    transitionTo(oldPage, newPage);
    afterTransition(pageTransition, newPage, oldPage);
  }

  private void afterTransition(PageTransition pageTransition, Page newPage, Page oldPage) {
    Timber.d("onAfterTransition %s", this.toString());
    onAfterTransition();

    if (newPage.getIsRootPage()) {
      Timber.d("New page is a root page, clearing stack");
      PageManager.destroy(pageStack);
    } else if (pageTransition.getReplace()) {
      Timber.d("Removing page from stack in order to replace %s, %s", oldPage.toString(), this.toString());
      pageStack.remove(oldPage);
      PageManager.destroy(oldPage);
    }

    Timber.d("Adding page to stack %s, %s", newPage.toString(), this.toString());
    pageStack.add(newPage);
  }

  private void beforeTransition(PageTransition pageTransition, Page newPage) {
    Timber.d("transitionTo new %s, %s", newPage.toString(), this.toString());
    reportPageView(newPage);

    Timber.d("onBeforeTransition %s", this.toString());
    onBeforeTransition(pageTransition);
  }

  private void transitionTo(@Nullable Page oldPage, @NonNull Page newPage) {
    Timber.d("transitionTo old %s, new %s, %s", oldPage == null ? "null" : oldPage.toString(), newPage.toString(), this.toString());

    PageManager.resume(newPage);
    this.page.onNext(newPage);
    if (oldPage != null) {
      PageManager.pause(oldPage);
    }
  }

  protected void onBeforeTransition(final @NonNull PageTransition pageTransition) {
  }

  protected void onAfterTransition() {
  }

  protected void executePageItem(@NonNull PageItem pageItem) {
    if (PageLink.class.isAssignableFrom(pageItem.getClass())) {
      showPage.onNext((PageLink) pageItem);

    } else if (PageCommand.class.isAssignableFrom(pageItem.getClass())) {
      ((PageCommand) pageItem).execute(environment());
    }
  }

  // INPUTS
  public void back() {
    if (pageStack.size() < 2) {
      // Issue default back behavior
      goBack.onNext(null);
      return;
    }

    // Pop current page, old page
    Page oldPage = pageStack.pop();
    Timber.d("Popped page from stack %s, %s", oldPage.toString(), this.toString());

    // Peek previous page, new page
    Page newPage = pageStack.peek();

    // Make transition, this issues onPause and onResume
    transitionTo(oldPage, newPage);

    // Destroy old page
    PageManager.destroy(oldPage);
  }

  public void up() {
    back();
  }

  // OUTPUTS
  public Observable<Object> goBack() {
    return goBack;
  }
}
