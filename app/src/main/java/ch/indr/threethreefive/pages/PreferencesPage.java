/*
 * Copyright (c) 2017 Reto Inderbitzin (mail@indr.ch)
 *
 * For the full copyright and license information, please view
 * the LICENSE file that was distributed with this source code.
 */

package ch.indr.threethreefive.pages;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import ch.indr.threethreefive.libs.Build;
import ch.indr.threethreefive.libs.Environment;
import ch.indr.threethreefive.libs.PageCommand;
import ch.indr.threethreefive.libs.PageItemsBuilder;
import ch.indr.threethreefive.navigation.Page;
import ch.indr.threethreefive.services.SpeakerType;
import ch.indr.threethreefive.services.UiModeManagerType;

public class PreferencesPage extends Page {

  protected @Inject Build build;
  protected @Inject SpeakerType speaker;
  protected @Inject UiModeManagerType uiModeManager;

  public PreferencesPage(Environment environment) {
    super(environment);
  }

  @Override public void onCreate(@NonNull Context context, Uri uri, Bundle bundle) {
    super.onCreate(context, uri, bundle);

    component().inject(this);

    setTitle("Preferences");

    final PageItemsBuilder builder = pageItemsBuilder();

    addReplayInterfaceInstructionsItem(builder);
    addSwitchUiItem(builder);

    if (build.isDebug()) {
      builder.addItem(new ResetAppLaunchCounter());
      builder.addItem(new ResetButtonUiLaunchCounter());
    }

    setPageItems(builder);
  }

  private void addReplayInterfaceInstructionsItem(PageItemsBuilder builder) {
    if (uiModeManager.getCurrentUiMode() == UiModeManagerType.UI_MODE_BUTTONS) {
      builder.addItem("Replay Interface Instructions", this::replayInterfaceInstructions);
    }
  }

  private void replayInterfaceInstructions(Environment environment) {
    speaker.instructions().replay();
  }

  private void addSwitchUiItem(PageItemsBuilder builder) {
    final int uiMode = uiModeManager.getCurrentUiMode();

    switch (uiMode) {
      case UiModeManagerType.UI_MODE_BUTTONS:
        builder.addItem("Switch to List Interface", this::startUiModeList);
        break;
      case UiModeManagerType.UI_MODE_LIST:
        builder.addItem("Switch to Button Interface", this::startUiModeButtons);
        break;
    }
  }

  private void startUiModeList(Environment environment) {
    uiModeManager.launchListUi(getContext());
  }

  private void startUiModeButtons(Environment environment) {
    uiModeManager.launchButtonsUi(getContext());
  }

  private class ResetAppLaunchCounter extends PageCommand {

    ResetAppLaunchCounter() {
      super();
      name.onNext("Reset App Launch Counter");
    }

    @Override public void execute(@NonNull Environment environment) {
      environment.preferences().appLaunchCounter().delete();
      environment.toastManager().toast("App launch counter reset");
      environment.speaker().command().preferenceAppLaunchCounterReset();
    }
  }

  private class ResetButtonUiLaunchCounter extends PageCommand {

    ResetButtonUiLaunchCounter() {
      super();
      name.onNext("Reset Button UI Launch Counter");
    }

    @Override public void execute(@NonNull Environment environment) {
      environment.preferences().uiModeButtonsLaunchCounter().delete();
      environment.toastManager().toast("Button UI launch counter reset");
      environment.speaker().command().preferenceButtonUiLaunchCounterReset();
    }
  }
}
