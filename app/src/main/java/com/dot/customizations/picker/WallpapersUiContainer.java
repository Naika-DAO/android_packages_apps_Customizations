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

import androidx.annotation.Nullable;

/**
 * Interface for a class which presents (in UI) a collection of wallpapers.
 */
public interface WallpapersUiContainer {
    /**
     * Notifies the container that wallpapers are ready to display.
     */
    void onWallpapersReady();

    /**
     * Returns the {@link CategorySelectorFragment} used by this container to display wallpaper
     * categories, or {@code null} if none is available.
     */
    @Nullable
    CategorySelectorFragment getCategorySelectorFragment();

    /**
     * Notifies the container that categories have been fetched.
     */
    void doneFetchingCategories();
}
