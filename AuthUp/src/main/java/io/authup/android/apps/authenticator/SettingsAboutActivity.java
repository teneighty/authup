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

package io.authup.android.apps.authenticator;

import io.authup.android.apps.authenticator.testability.TestablePreferenceActivity;
import io.authup.android.apps.authenticator.R;

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

/**
 * Activity that displays the "About" preferences.
 *
 * @author klyubin@google.com (Alex Klyubin)
 */
public class SettingsAboutActivity extends TestablePreferenceActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    addPreferencesFromResource(R.xml.preferences_about);

    String packageVersion = "";
    try {
      packageVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
    } catch (NameNotFoundException e) {}
    findPreference("version").setSummary(packageVersion);
  }
}
