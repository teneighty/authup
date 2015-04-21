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

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * The main activity that displays usernames and codes
 */
public class TokenEditActivity extends TestableActivity {

  private static final String TAG = "TokenEditActivity";

  private AccountDb mAccountDb;
  private OtpSource mOtpProvider;
  private String mUser;
  private EditText mProviderTypeEdit;
  private EditText mNameEdit;
  private TextView mType;
  private TextView mDoneButton;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final ActionBar actionBar = getActionBar();
    actionBar.setHomeButtonEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(true);

    mAccountDb = DependencyInjector.getAccountDb();

    setTitle(R.string.app_name);
    setContentView(R.layout.act_token_edit);

    Intent intent = getIntent();
    mUser = intent.getStringExtra("user");
    if (null == mUser) {
      finish();
      return;
    }

    mNameEdit = (EditText) findViewById(R.id.token_name);
    mNameEdit.setText(mUser);

    String providerType = mAccountDb.getProviderType(mUser);
    mProviderTypeEdit = (EditText) findViewById(R.id.provider_type);
    mProviderTypeEdit.setText(providerType == null ? "" : providerType);

    OtpType otpType = mAccountDb.getType(mUser);
    mType = (TextView) findViewById(R.id.token_type);
    mType.setText(otpType == OtpType.TOTP ? "TOPT" : "HOPT");

    mDoneButton = (Button) findViewById(R.id.done_button);
    mDoneButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        String newName = mNameEdit.getText().toString();
        String newProvType = mProviderTypeEdit.getText().toString();
        mAccountDb.update(newName,
            mAccountDb.getSecret(mUser), mUser, mAccountDb.getType(mUser),
            mAccountDb.getCounter(mUser), newProvType);
        TokenEditActivity.this.finish();
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.token_edit, menu);
    return true;
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        this.finish();
        return true;
      case R.id.token_remove:
        View promptContentView =
            getLayoutInflater().inflate(R.layout.remove_account_prompt, null, false);
        WebView webView = (WebView) promptContentView.findViewById(R.id.web_view);
        webView.setBackgroundColor(Color.TRANSPARENT);
        Utilities.setWebViewHtml(
            webView,
            "<html><body style=\"background-color: transparent;\" text=\"white\">"
                + getString(R.string.remove_account_dialog_message)
                + "</body></html>");

        new AlertDialog.Builder(this)
          .setTitle(getString(R.string.remove_account_dialog_title, mUser))
          .setView(promptContentView)
          .setIcon(android.R.drawable.ic_dialog_alert)
          .setPositiveButton(R.string.remove_account_dialog_button_remove,
              new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int whichButton) {
                  mAccountDb.delete(mUser);
                  TokenEditActivity.this.finish();
                }
              }
          )
          .setNegativeButton(R.string.cancel, null)
          .show();
        return true;
      default:
        return super.onContextItemSelected(item);
    }
  }
}
