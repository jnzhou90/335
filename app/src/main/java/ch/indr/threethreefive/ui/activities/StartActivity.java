/*
 * Copyright (c) 2016 Reto Inderbitzin (mail@indr.ch)
 *
 * For the full copyright and license information, please view
 * the LICENSE file that was distributed with this source code.
 */

package ch.indr.threethreefive.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ch.indr.threethreefive.R;
import ch.indr.threethreefive.ThreeThreeFiveApp;
import ch.indr.threethreefive.libs.BaseActivity;
import ch.indr.threethreefive.libs.qualifiers.RequiresActivityViewModel;
import ch.indr.threethreefive.services.UiModeManager;
import ch.indr.threethreefive.viewmodels.StartViewModel;
import timber.log.Timber;

import static ch.indr.threethreefive.libs.rx.transformers.Transfomers.observeForUI;

@RequiresActivityViewModel(StartViewModel.class)
public class StartActivity extends BaseActivity<StartViewModel> {

  protected @Inject UiModeManager uiModeManager;

  protected @Bind(R.id.buttonContinue) Button buttonContinue;
  protected @Bind(R.id.textViewStatus) TextView textViewStatus;

  @Override protected void onCreate(Bundle savedInstanceState) {
    // Make sure this is before calling super.onCreate
    setTheme(R.style.AppTheme_NoActionBar);
    super.onCreate(savedInstanceState);

    ((ThreeThreeFiveApp) getApplication()).component().inject(this);
    setContentView(R.layout.activity_start);
    ButterKnife.bind(this);

    textViewStatus.setText(R.string.start_loading);
    buttonContinue.setVisibility(View.GONE);

    viewModel.showTextToSpeechError()
        .compose(bindToLifecycle())
        .compose(observeForUI())
        .subscribe(this::showTextToSpeechError);

    viewModel.showWelcome()
        .compose(bindToLifecycle())
        .compose(observeForUI())
        .subscribe(this::showWelcome);

    viewModel.startUiSelection()
        .compose(bindToLifecycle())
        .compose(observeForUI())
        .subscribe(this::showUiModeSelection);
  }

  private void showTextToSpeechError(final int status) {
    Timber.d("showTextToSpeechError %d, %s", status, this.toString());

    textViewStatus.setText(R.string.start_tts_error);
    buttonContinue.setVisibility(View.VISIBLE);
  }

  private void showWelcome(Object __) {
    Timber.d("showWelcome %s", this.toString());

    textViewStatus.setText(R.string.start_welcome_to);
  }

  private void showUiModeSelection(Object __) {
    Timber.d("showUiModeSelection %s", this.toString());

    Intent intent = new Intent(this, UiSelectionActivity.class);
    startActivity(intent);
    finish();
  }

  private void startListUiMode() {
    Timber.d("startListUiMode %s", this.toString());

    uiModeManager.launchListUi(this);
    finish();
  }

  @OnClick(R.id.buttonContinue)
  public void buttonContinueOnClick() {
    Timber.d("buttonContinueOnClick %s", this.toString());

    startListUiMode();
  }
}
