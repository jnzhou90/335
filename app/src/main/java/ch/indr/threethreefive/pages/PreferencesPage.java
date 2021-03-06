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

import ch.indr.threethreefive.R;
import ch.indr.threethreefive.commands.OpenWebsite;
import ch.indr.threethreefive.libs.Build;
import ch.indr.threethreefive.libs.Environment;
import ch.indr.threethreefive.libs.PageCommand;
import ch.indr.threethreefive.libs.PageItemsBuilder;
import ch.indr.threethreefive.libs.pages.Page;
import ch.indr.threethreefive.services.AccessibilityServices;
import ch.indr.threethreefive.services.Speaker;
import ch.indr.threethreefive.services.UiModeManager;

public class PreferencesPage extends Page {

  protected @Inject Build build;
  protected @Inject Speaker speaker;
  protected @Inject UiModeManager uiModeManager;

  public PreferencesPage(Environment environment) {
    super(environment);
  }

  @Override public void onCreate(@NonNull Context context, @NonNull Uri uri, Bundle bundle) {
    super.onCreate(context, uri, bundle);

    component().inject(this);

    setDescription(getString(R.string.mainmenu_preferences_title));

    final PageItemsBuilder builder = pageItemsBuilder();

    addReplayInterfaceInstructionsItem(builder);
    addSwitchUiItem(builder);

    if (uiModeManager.getCurrentUiMode() == UiModeManager.UI_MODE_LIST) {
      builder.add(new OpenWebsite(context, "http://www.indr.ch/335"));
    }

    if (build.isDebug()) {
      builder.add(new ResetAppLaunchCounter());
      builder.add(new ResetButtonUiLaunchCounter());
      addAccessibilityServiceStatusInfos(builder);
    }

    setPageItems(builder);
  }

  private void addAccessibilityServiceStatusInfos(PageItemsBuilder builder) {
    final AccessibilityServices accessibilityServices = AccessibilityServices.newInstance(context());

    builder.addText("Accessibility.isEnabled: " + accessibilityServices.isEnabled());
    builder.addText("Accessibility.isSpokenFeedbackEnabled: " + accessibilityServices.isSpokenFeedbackEnabled());
    builder.addText("Accessibility.isTouchExplorationEnabled: " + accessibilityServices.isTouchExplorationEnabled());
  }

  private void addReplayInterfaceInstructionsItem(PageItemsBuilder builder) {
    if (uiModeManager.getCurrentUiMode() == UiModeManager.UI_MODE_BUTTONS) {
      builder.addAction("Replay Interface Instructions", this::replayInterfaceInstructions);
    }
  }

  private void replayInterfaceInstructions(Environment environment) {
    speaker.instructions().replayUsage();
  }

  private void addSwitchUiItem(PageItemsBuilder builder) {
    final int uiMode = uiModeManager.getCurrentUiMode();

    switch (uiMode) {
      case UiModeManager.UI_MODE_BUTTONS:
        builder.addAction("Switch to List Interface", this::startUiModeList);
        break;
      case UiModeManager.UI_MODE_LIST:
        builder.addAction("Switch to Button Interface", this::startUiModeButtons);
        break;
    }
  }

  private void startUiModeList(Environment environment) {
    uiModeManager.launchListUi(context());
  }

  private void startUiModeButtons(Environment environment) {
    uiModeManager.launchButtonsUi(context());
  }

  private class ResetAppLaunchCounter extends PageCommand {

    ResetAppLaunchCounter() {
      super("Reset App Launch Counter");
    }

    @Override public void execute(@NonNull Environment environment) {
      environment.preferences().appLaunchCounter().delete();
      environment.toastManager().toast("App launch counter reset");
      environment.speaker().command().preferenceAppLaunchCounterReset();
    }
  }

  private class ResetButtonUiLaunchCounter extends PageCommand {

    ResetButtonUiLaunchCounter() {
      super("Reset Button UI Launch Counter");
    }

    @Override public void execute(@NonNull Environment environment) {
      environment.preferences().uiModeButtonsLaunchCounter().delete();
      environment.toastManager().toast("Button UI launch counter reset");
      environment.speaker().command().preferenceButtonUiLaunchCounterReset();
    }
  }
}
