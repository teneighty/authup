/*
 * Copyright 2009 Google Inc. All Rights Reserved.
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

package com.google.android.apps.authenticator;

import com.google.android.apps.authenticator.AccountDb.OtpType;
import com.google.android.apps.authenticator.dataimport.ImportController;
import com.google.android.apps.authenticator.howitworks.IntroEnterPasswordActivity;
import com.google.android.apps.authenticator.testability.DependencyInjector;
import com.google.android.apps.authenticator.testability.TestableActivity;
import com.google.android.apps.authenticator2.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.ClipboardManager;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.webkit.WebView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * The main activity that displays usernames and codes
 */
public class TokenViewActivity extends TestableActivity {

  private static final String TAG = "TokenViewActivity";

  private double mTotpCountdownPhase;
  private AccountDb mAccountDb;
  private OtpSource mOtpProvider;
  private TextView mToken;
  private TextView mTimeLeft;
  private ImageView mImage;
  private CountdownIndicator mCountdownIndicator;
  private String mUser;
  private OtpType mType;
  private String mCode;

  private static final long TOTP_COUNTDOWN_REFRESH_PERIOD = 100;

  /**
   * Minimum amount of time (milliseconds) that has to elapse from the moment a HOTP code is
   * generated for an account until the moment the next code can be generated for the account.
   * This is to prevent the user from generating too many HOTP codes in a short period of time.
   */
  private static final long HOTP_MIN_TIME_INTERVAL_BETWEEN_CODES = 5000;

  /**
   * The maximum amount of time (milliseconds) for which a HOTP code is displayed after it's been
   * generated.
   */
  private static final long HOTP_DISPLAY_TIMEOUT = 2 * 60 * 1000;

  /** Counter used for generating TOTP verification codes. */
  private TotpCounter mTotpCounter;

  /** Clock used for generating TOTP verification codes. */
  private TotpClock mTotpClock;

  /**
   * Task that periodically notifies this activity about the amount of time remaining until
   * the TOTP codes refresh. The task also notifies this activity when TOTP codes refresh.
   */
  private TotpCountdownTask mTotpCountdownTask;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final ActionBar actionBar = getActionBar();
    actionBar.setHomeButtonEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(true);

    mAccountDb = DependencyInjector.getAccountDb();
    mOtpProvider = DependencyInjector.getOtpProvider();
    mTotpCounter = mOtpProvider.getTotpCounter();
    mTotpClock = mOtpProvider.getTotpClock();

    setContentView(R.layout.act_token);

    Intent intent = getIntent();
    mUser = intent.getStringExtra("user");
    if (null == mUser) {
      // TODO: error error error
      finish();
      return;
    }
    setTitle(mUser);
    mType = mAccountDb.getType(mUser);

    mToken = (TextView) findViewById(R.id.token);
    mTimeLeft = (TextView) findViewById(R.id.time_left);
    mCountdownIndicator = (CountdownIndicator) findViewById(R.id.countdown_icon);

    mImage = (ImageView) findViewById(R.id.image);
    mImage.setVisibility(View.GONE);
    setLogo();

    refreshVerificationCode();
  }

  private void setLogo() {
    String providerType = mAccountDb.getProviderType(mUser);
    Bitmap bitmap = null;
    if (providerType != null) {
      try {
        bitmap = AuthenticatorActivity.getBitmapFromAssets(this, providerType + "/logo.png");
      } catch (java.io.IOException e) {
         Log.e(TAG, "", e);
      }
    }
    if (null != bitmap) {
      mImage.setImageBitmap(bitmap);
      mImage.setVisibility(View.VISIBLE);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.token, menu);
    return true;
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        this.finish();
        return true;
      case R.id.token_copy:
        copyToClipboard();
        return true;
    }
    return super.onMenuItemSelected(featureId, item);
  }

  private void copyToClipboard() {
    ClipboardManager clipboard =
        (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    clipboard.setText(mCode);

    Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_LONG).show();
  }

  @Override
  protected void onStart() {
    super.onStart();

    updateCodesAndStartTotpCountdownTask();
  }

  @Override
  protected void onStop() {
    stopTotpCountdownTask();

    super.onStop();
  }

  private void updateCodesAndStartTotpCountdownTask() {
    stopTotpCountdownTask();

    mTotpCountdownTask =
        new TotpCountdownTask(mTotpCounter, mTotpClock, TOTP_COUNTDOWN_REFRESH_PERIOD);
    mTotpCountdownTask.setListener(new TotpCountdownTask.Listener() {
      @Override
      public void onTotpCountdown(long millisRemaining) {
        if (isFinishing()) {
          // No need to reach to this even because the Activity is finishing anyway
          return;
        }
        setTotpCountdownPhaseFromTimeTillNextValue(millisRemaining);
      }

      @Override
      public void onTotpCounterValueChanged() {
        if (isFinishing()) {
          // No need to reach to this even because the Activity is finishing anyway
          return;
        }
        refreshVerificationCode();
      }
    });

    mTotpCountdownTask.startAndNotifyListener();
  }

  private void stopTotpCountdownTask() {
    if (mTotpCountdownTask != null) {
      mTotpCountdownTask.stop();
      mTotpCountdownTask = null;
    }
  }

  private void setTotpCountdownPhase(double phase) {
    mTotpCountdownPhase = phase;
    updateCountdownIndicators();
  }

  private void setTotpCountdownPhaseFromTimeTillNextValue(long millisRemaining) {
    setTotpCountdownPhase(
        ((double) millisRemaining) / Utilities.secondsToMillis(mTotpCounter.getTimeStep()));
  }

  private void refreshVerificationCode() {
    try {
      mCode = mOtpProvider.getNextCode(mUser);
      mToken.setText(mCode);
    } catch (OtpSourceException ignored) {
        /* Shhhh */
    }
    setTotpCountdownPhase(1.0);
  }

  private void updateCountdownIndicators() {
    if (mCountdownIndicator != null) {
      mCountdownIndicator.setPhase(mTotpCountdownPhase);
      //TODO: refactor please
      mTimeLeft.setText(String.format("%d s", (int) Math.floor(mTotpCountdownPhase * 30.0)));
    }
  }
}
