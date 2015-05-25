/*
 * Copyright 2010 Google Inc. All Rights Reserved.
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

package io.authup.android.apps.authenticator.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.InputStream;

public class ImageUtilities {

  static final String TAG = "ImageUtilities";

  private static Bitmap getBitmapFromAssets(Context context, String fileName) throws java.io.IOException {
    AssetManager assetManager = context.getAssets();
    InputStream is = assetManager.open(fileName);
    Bitmap bitmap = BitmapFactory.decodeStream(is);
    return bitmap;
  }

  private static Bitmap getImageType(Context context, String providerType, String type) {
    Bitmap bitmap = null;
    if (null == providerType) {
      return null;
    }
    try {
      bitmap = getBitmapFromAssets(context, providerType + "/" + type);
    } catch (java.io.IOException e) {
      Log.e(TAG, "", e);
    }
    return bitmap;
  }

  public static Bitmap getMenuBitmap(Context context, String providerType) {
    return getImageType(context, providerType, "menu_item_v2.png");
  }

  public static Bitmap getLogo(Context context, String providerType) {
    return getImageType(context, providerType, "logo.png");
  }

}
