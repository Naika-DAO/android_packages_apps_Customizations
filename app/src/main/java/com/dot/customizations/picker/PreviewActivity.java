/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dot.customizations.picker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.dot.customizations.R;
import com.dot.customizations.model.InlinePreviewIntentFactory;
import com.dot.customizations.model.WallpaperInfo;
import com.dot.customizations.module.InjectorProvider;
import com.dot.customizations.picker.AppbarFragment.AppbarFragmentHost;
import com.dot.customizations.util.ActivityUtils;

/**
 * Activity that displays a preview of a specific wallpaper and provides the ability to set the
 * wallpaper as the user's current wallpaper.
 */
public class PreviewActivity extends BasePreviewActivity implements AppbarFragmentHost {

    /**
     * Returns a new Intent with the provided WallpaperInfo instance put as an extra.
     */
    public static Intent newIntent(Context packageContext, WallpaperInfo wallpaperInfo) {
        Intent intent = new Intent(packageContext, PreviewActivity.class);
        intent.putExtra(EXTRA_WALLPAPER_INFO, wallpaperInfo);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        enableFullScreen();

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            Intent intent = getIntent();
            WallpaperInfo wallpaper = intent.getParcelableExtra(EXTRA_WALLPAPER_INFO);
            boolean viewAsHome = intent.getBooleanExtra(EXTRA_VIEW_AS_HOME, true);
            boolean testingModeEnabled = intent.getBooleanExtra(EXTRA_TESTING_MODE_ENABLED, false);
            fragment = InjectorProvider.getInjector().getPreviewFragment(
                    /* context */ this,
                    wallpaper,
                    PreviewFragment.MODE_CROP_AND_SET_WALLPAPER,
                    viewAsHome,
                    testingModeEnabled);
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    public void onUpArrowPressed() {
        onBackPressed();
    }

    @Override
    public boolean isUpArrowSupported() {
        return !ActivityUtils.isSUWMode(getBaseContext());
    }

    /**
     * Implementation that provides an intent to start a PreviewActivity.
     */
    public static class PreviewActivityIntentFactory implements InlinePreviewIntentFactory {
        @Override
        public Intent newIntent(Context context, WallpaperInfo wallpaper) {
            return PreviewActivity.newIntent(context, wallpaper);
        }
    }
}
