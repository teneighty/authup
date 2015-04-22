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
import com.google.android.apps.authenticator.utils.ImageUtilities;
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
import android.content.res.AssetManager;
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
import android.view.ActionMode;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.webkit.WebView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class ProviderListActivity extends TestableActivity {

  public static final String PROVIDER_TYPE = "provider_type";

  private static final String TAG = "ProviderListActivity";

  private ListView mProviderList;
  private ProviderAdapter mAdapter;

  private String[] mProviders = {
    "airbitz",
    "bitcoin",
    "digitalocean",
    "dropbox",
    "facebook",
    "github",
    "gmail",
    "google",
    "other"
  };

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setTitle(R.string.app_name);
    setContentView(R.layout.act_provider_list);

    final ActionBar actionBar = getActionBar();
    actionBar.setHomeButtonEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(true);

    mAdapter = new ProviderAdapter(this, mProviders);
    mProviderList = (ListView) findViewById(R.id.provider_list);
    mProviderList.setAdapter(mAdapter);
    mProviderList.setOnItemClickListener(new OnItemClickListener(){
      @Override
      public void onItemClick(AdapterView<?> parent, View row,
                              int position, long unusedId) {
        String provider = (String) parent.getItemAtPosition(position);
        Intent intent = new Intent();
        intent.putExtra(PROVIDER_TYPE, provider);
        setResult(RESULT_OK, intent);
        finish();
      }
    });
  }

  private class ProviderAdapter extends ArrayAdapter<String>  {

    public ProviderAdapter(Context context, String[] providers) {
      super(context, R.layout.item_provider, providers);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
     LayoutInflater inflater = getLayoutInflater();
     String provider = getItem(position);

     View row;
     if (convertView != null) {
       row = convertView;
     } else {
       row = inflater.inflate(R.layout.item_provider, null);
     }
     TextView userView = (TextView) row.findViewById(R.id.provider);
     userView.setText(provider);

     ImageView icon = (ImageView) row.findViewById(R.id.icon);
     Bitmap bitmap = ImageUtilities.getMenuBitmap(ProviderListActivity.this, provider);
     if (null != bitmap) {
        icon.setImageBitmap(bitmap);
     }
     return row;
    }
  }
}
