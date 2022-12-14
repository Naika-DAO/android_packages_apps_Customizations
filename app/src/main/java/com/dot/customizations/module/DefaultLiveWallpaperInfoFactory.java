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
package com.dot.customizations.module;

import androidx.annotation.Nullable;

import com.dot.customizations.model.LiveWallpaperInfo;
import com.dot.customizations.model.WallpaperInfo;

/**
 * Default implementation of {@link LiveWallpaperInfoFactory} that just creates
 * {@link LiveWallpaperInfo}.
 */
public class DefaultLiveWallpaperInfoFactory implements LiveWallpaperInfoFactory {

    @Override
    public WallpaperInfo getLiveWallpaperInfo(android.app.WallpaperInfo info) {
        return new LiveWallpaperInfo(info);
    }

    @Override
    public WallpaperInfo getLiveWallpaperInfo(android.app.WallpaperInfo info,
                                              boolean shouldShowTitle, @Nullable String collectionId) {
        return new LiveWallpaperInfo(info, shouldShowTitle, collectionId);
    }
}
