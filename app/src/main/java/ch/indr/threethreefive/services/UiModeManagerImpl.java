/*
 * Copyright (c) 2017 Reto Inderbitzin (mail@indr.ch)
 *
 * For the full copyright and license information, please view
 * the LICENSE file that was distributed with this source code.
 */

package ch.indr.threethreefive.services;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import ch.indr.threethreefive.BuildConfig;
import ch.indr.threethreefive.libs.Preferences;
import ch.indr.threethreefive.ui.activities.ButtonGuideActivity;
import ch.indr.threethreefive.ui.activities.ListGuideActivity;

import static android.content.Context.UI_MODE_SERVICE;

public class UiModeManagerImpl implements UiModeManager {

  private final Context context;
  private final Preferences preferences;

  public UiModeManagerImpl(final @NonNull Context context, final @NonNull Preferences preferences) {
    this.context = context;
    this.preferences = preferences;
  }

  public int getCurrentUiMode() {
    return preferences.uiMode().get();
  }

  @Override public void launchButtonsUi(@NonNull Context context) {
    setCurrentUiMode(UI_MODE_BUTTONS);
    startActivity(context, ButtonGuideActivity.class);

    if (BuildConfig.ANSWERS) {
      Answers.getInstance().logCustom(new CustomEvent("Buttons Interface Launched"));
    }
  }

  @Override public void launchListUi(@NonNull Context context) {
    setCurrentUiMode(UI_MODE_LIST);
    startActivity(context, ListGuideActivity.class);

    if (BuildConfig.ANSWERS) {
      Answers.getInstance().logCustom(new CustomEvent("List Interface Launched"));
    }
  }

  private android.app.UiModeManager getUiModeManager() {
    return (android.app.UiModeManager) context.getSystemService(UI_MODE_SERVICE);
  }

  private void setCurrentUiMode(int uiMode) {
    preferences.uiMode().set(uiMode);
    switch (uiMode) {
      case UiModeManager.UI_MODE_BUTTONS:
        preferences.uiModeButtonsLaunchCounter().increment();
        break;
      case UiModeManager.UI_MODE_LIST:
        preferences.uiModeListLaunchCounter().increment();
        break;
    }
  }

  private void startActivity(Context packageContext, Class<?> cls) {
    Intent intent = new Intent(packageContext, cls);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    context.startActivity(intent);
  }
}
