/*
 * Copyright (c) 2017 Reto Inderbitzin (mail@indr.ch)
 *
 * For the full copyright and license information, please view
 * the LICENSE file that was distributed with this source code.
 */

package ch.indr.threethreefive.services;

import android.content.res.Resources;
import android.support.annotation.NonNull;

import ch.indr.threethreefive.R;

public class InstructionsSpeakerImpl implements InstructionsSpeaker {
  private final Speaker speaker;
  private final Resources resources;

  public InstructionsSpeakerImpl(@NonNull Speaker speaker, @NonNull Resources resources) {
    this.speaker = speaker;
    this.resources = resources;
  }

  @Override public void playAccessibilityServicesDetected() {
    speak(R.string.speech_accessibility_services_detected, true);
    speak(R.string.speech_interface_instructions_end_with_home_screen);
  }

  @Override public void playUsage() {
    speak(R.string.speech_interface_instructions, true);
    speak(R.string.speech_interface_instructions_end_with_home_screen);
  }

  @Override public void replayUsage() {
    speak(R.string.speech_interface_instructions, true);
    speak(R.string.speech_interface_instructions_end_with_preference_screen);
  }

  private void speak(int resourceId) {
    speak(resourceId, false);
  }

  private void speak(int resourceId, boolean urgent) {
    final String text = resources.getString(resourceId);
    final String[] parts = text.split("\n");

    if (urgent) {
      speaker.sayUrgent(parts[0], Speaker.SPEECH_RATE_SLOW);
    } else {
      speaker.sayQueued(parts[0], Speaker.SPEECH_RATE_SLOW);
    }
    for (int i = 1; i < parts.length; i++) {
      String part = parts[i];
      speaker.sayQueued(" — ", Speaker.SPEECH_RATE_SLOW);
      speaker.sayQueued(" — ", Speaker.SPEECH_RATE_SLOW);
      speaker.sayQueued(part, Speaker.SPEECH_RATE_SLOW);
    }
  }
}
