/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.apps.authenticator.testability;

import com.google.android.apps.authenticator2.R;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;

/**
 * Base class for {@link Activity} instances to make them more testable.
 *
 * @author klyubin@google.com (Alex Klyubin)
 */
public class TestableActivity extends Activity {

  @Override
  public void startActivity(Intent intent) {
    StartActivityListener listener = DependencyInjector.getStartActivityListener();
    if ((listener != null) && (listener.onStartActivityInvoked(this, intent))) {
      return;
    }

    super.startActivity(intent);
    super.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_exit);
  }

  @Override
  public void startActivityForResult(Intent intent, int requestCode) {
    StartActivityListener listener = DependencyInjector.getStartActivityListener();
    if ((listener != null) && (listener.onStartActivityInvoked(this, intent))) {
      return;
    }

    super.startActivityForResult(intent, requestCode);
    super.overridePendingTransition(R.anim.slide_in_right, R.anim.fade_out_exit);
  }

  @Override
  public void finish() {
    super.finish();
    super.overridePendingTransition(R.anim.fade_in_enter, R.anim.slide_out_right);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }
}
