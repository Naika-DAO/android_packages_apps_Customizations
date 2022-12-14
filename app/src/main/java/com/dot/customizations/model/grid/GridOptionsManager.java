/*
 * Copyright (C) 2019 The Android Open Source Project
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
package com.dot.customizations.model.grid;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.dot.customizations.R;
import com.dot.customizations.model.CustomizationManager;
import com.dot.customizations.module.CustomizationInjector;
import com.dot.customizations.module.InjectorProvider;
import com.dot.customizations.module.ThemesUserEventLogger;
import com.dot.customizations.util.PreviewUtils;

import java.util.List;

/**
 * {@link CustomizationManager} for interfacing with the launcher to handle {@link GridOption}s.
 */
public class GridOptionsManager implements CustomizationManager<GridOption> {

    private static GridOptionsManager sGridOptionsManager;

    private final LauncherGridOptionsProvider mProvider;
    private final ThemesUserEventLogger mEventLogger;

    @VisibleForTesting
    GridOptionsManager(LauncherGridOptionsProvider provider, ThemesUserEventLogger logger) {
        mProvider = provider;
        mEventLogger = logger;
    }

    /**
     * Returns the {@link GridOptionsManager} instance.
     */
    public static GridOptionsManager getInstance(Context context) {
        if (sGridOptionsManager == null) {
            Context appContext = context.getApplicationContext();
            CustomizationInjector injector = (CustomizationInjector) InjectorProvider.getInjector();
            ThemesUserEventLogger eventLogger = (ThemesUserEventLogger) injector.getUserEventLogger(
                    appContext);
            sGridOptionsManager = new GridOptionsManager(
                    new LauncherGridOptionsProvider(appContext,
                            appContext.getString(R.string.grid_control_metadata_name)),
                    eventLogger);
        }
        return sGridOptionsManager;
    }

    @Override
    public boolean isAvailable() {
        return mProvider.areGridsAvailable();
    }

    @Override
    public void apply(GridOption option, Callback callback) {
        int updated = mProvider.applyGrid(option.name);
        if (updated == 1) {
            mEventLogger.logGridApplied(option);
            callback.onSuccess();
        } else {
            callback.onError(null);
        }
    }

    @Override
    public void fetchOptions(OptionsFetchedListener<GridOption> callback, boolean reload) {
        new FetchTask(mProvider, callback, reload).execute();
    }

    /**
     * Call through content provider API to render preview
     */
    public void renderPreview(Bundle bundle, String gridName,
                              PreviewUtils.WorkspacePreviewCallback callback) {
        mProvider.renderPreview(gridName, bundle, callback);
    }

    private static class FetchTask extends AsyncTask<Void, Void, List<GridOption>> {
        private final LauncherGridOptionsProvider mProvider;
        @Nullable
        private final OptionsFetchedListener<GridOption> mCallback;
        private final boolean mReload;

        private FetchTask(@NonNull LauncherGridOptionsProvider provider,
                          @Nullable OptionsFetchedListener<GridOption> callback, boolean reload) {
            mCallback = callback;
            mProvider = provider;
            mReload = reload;
        }

        @Override
        protected List<GridOption> doInBackground(Void[] params) {
            return mProvider.fetch(mReload);
        }

        @Override
        protected void onPostExecute(List<GridOption> gridOptions) {
            if (mCallback != null) {
                if (gridOptions != null && !gridOptions.isEmpty()) {
                    mCallback.onOptionsLoaded(gridOptions);
                } else {
                    mCallback.onError(null);
                }
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (mCallback != null) {
                mCallback.onError(null);
            }
        }
    }
}
