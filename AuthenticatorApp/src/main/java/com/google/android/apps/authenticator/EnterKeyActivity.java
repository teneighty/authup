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
import com.google.android.apps.authenticator.Base32String.DecodingException;
import com.google.android.apps.authenticator.testability.DependencyInjector;
import com.google.android.apps.authenticator.testability.TestableActivity;
import com.google.android.apps.authenticator.wizard.WizardPageActivity;
import com.google.android.apps.authenticator2.R;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.io.Serializable;

/**
 * The activity that lets the user manually add an account by entering its name, key, and type
 * (TOTP/HOTP).
 *
 * @author sweis@google.com (Steve Weis)
 */
public class EnterKeyActivity extends TestableActivity implements TextWatcher {
  private static final int MIN_KEY_BYTES = 10;
  private EditText mKeyEntryField;
  private EditText mAccountName;
  private EditText mProviderTypeEdit;
  private Spinner mType;

  private Button mDone;
  private AccountDb mAccountDb;
  private String mUser;

  /**
   * Called when the activity is first created
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.enter_key);

    final ActionBar actionBar = getActionBar();
    actionBar.setHomeButtonEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(true);

    mAccountDb = DependencyInjector.getAccountDb();

    // Find all the views on the page
    mKeyEntryField = (EditText) findViewById(R.id.key_value);
    mAccountName = (EditText) findViewById(R.id.account_name);
    mType = (Spinner) findViewById(R.id.type_choice);
    mProviderTypeEdit = (EditText) findViewById(R.id.provider_type);
    mDone = (Button) findViewById(R.id.done_button);
    mDone.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        // TODO(cemp): This depends on the OtpType enumeration to correspond
        // to array indices for the dropdown with different OTP modes.
        OtpType mode = mType.getSelectedItemPosition() == OtpType.TOTP.value ?
                      OtpType.TOTP :
                      OtpType.HOTP;
        if (validateKeyAndUpdateStatus(true)) {
          mAccountDb.update(
              mAccountName.getText().toString(),
              getEnteredKey(),
              mUser,
              mode,
              AccountDb.DEFAULT_HOTP_COUNTER,
              mProviderTypeEdit.getText().toString());
          finish();
        }
      }
    });

    ArrayAdapter<CharSequence> types = ArrayAdapter.createFromResource(this,
        R.array.type, android.R.layout.simple_spinner_item);
    types.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mType.setAdapter(types);

    // Set listeners
    mKeyEntryField.addTextChangedListener(this);

    Intent intent = getIntent();
    mUser = intent.getStringExtra("user");
    if (null != mUser) {
      mAccountName.setText(mUser);
      mKeyEntryField.setText(mAccountDb.getSecret(mUser));
      // mType.setSelectedItemPosition();
      String providerType = mAccountDb.getProviderType(mUser);
      mProviderTypeEdit.setText(providerType == null ? "" : providerType);
    }
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

  /*
   * Return key entered by user, replacing visually similar characters 1 and 0.
   */
  private String getEnteredKey() {
    String enteredKey = mKeyEntryField.getText().toString();
    return enteredKey.replace('1', 'I').replace('0', 'O');
  }

  /*
   * Verify that the input field contains a valid base32 string,
   * and meets minimum key requirements.
   */
  private boolean validateKeyAndUpdateStatus(boolean submitting) {
    String userEnteredKey = getEnteredKey();
    try {
      byte[] decoded = Base32String.decode(userEnteredKey);
      if (decoded.length < MIN_KEY_BYTES) {
        // If the user is trying to submit a key that's too short, then
        // display a message saying it's too short.
        mKeyEntryField.setError(submitting ? getString(R.string.enter_key_too_short) : null);
        return false;
      } else {
        mKeyEntryField.setError(null);
        return true;
      }
    } catch (DecodingException e) {
      mKeyEntryField.setError(getString(R.string.enter_key_illegal_char));
      return false;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void afterTextChanged(Editable userEnteredValue) {
    validateKeyAndUpdateStatus(false);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    // Do nothing
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    // Do nothing
  }
}
