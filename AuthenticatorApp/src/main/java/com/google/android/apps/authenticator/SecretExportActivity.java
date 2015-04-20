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
import com.google.android.apps.authenticator.testability.DependencyInjector;
import com.google.android.apps.authenticator.testability.TestableActivity;
import com.google.android.apps.authenticator2.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * The main activity that displays usernames and codes
 */
public class SecretExportActivity extends TestableActivity {

  private static final String TAG = "SecretExportActivity";

  private AccountDb mAccountDb;
  private OtpSource mOtpProvider;
  private String mUser;
  private ImageView mImageView;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final ActionBar actionBar = getActionBar();
    actionBar.setHomeButtonEnabled(true);

    mAccountDb = DependencyInjector.getAccountDb();

    setTitle(R.string.app_name);
    setContentView(R.layout.act_secret_export);

    Intent intent = getIntent();
    mUser = intent.getStringExtra("user");
    if (null == mUser) {
      finish();
      return;
    }

    OtpType type = mAccountDb.getType(mUser);

    final int size = getResources().getDimensionPixelSize(R.dimen.bitmap_qr_size);
    // TODO: include issuer=....
    String uri = "otpauth://" + (type == OtpType.TOTP ? "topt" : "hotp") + "/" + mUser + "?secret=" + mAccountDb.getSecret(mUser);
    Bitmap bitmap = Qr.bitmap(uri, size);

    mImageView = (ImageView) findViewById(R.id.qrcode);
    if (bitmap != null) {
      mImageView.setImageBitmap(bitmap);
    }
  }
}
