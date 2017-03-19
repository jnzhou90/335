/*
 * Copyright (c) 2017 Reto Inderbitzin (mail@indr.ch)
 *
 * For the full copyright and license information, please view
 * the LICENSE file that was distributed with this source code.
 */

package ch.indr.threethreefive.services;

import android.content.Context;

public interface UiModeManager {
  int UI_MODE_NONE = 0;

  int UI_MODE_BUTTONS = 1;

  int UI_MODE_LIST = 2;

  int getCurrentUiMode();

  void launchButtonsUi(Context context);

  void launchListUi(Context context);
}
