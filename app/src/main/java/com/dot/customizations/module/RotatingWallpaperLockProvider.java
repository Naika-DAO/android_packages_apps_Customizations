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
package com.dot.customizations.module;

/**
 * Provides a singleton POJO used to lock file read and write operations and metadata transactions
 * on the daily rotating wallpaper.
 */
public class RotatingWallpaperLockProvider {

    private static Object sInstance;

    public static synchronized Object getInstance() {
        if (sInstance == null) {
            sInstance = new Object();
        }
        return sInstance;
    }
}
