/*
 * Copyright (C) 2021 The Android Open Source Project
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
package com.dot.customizations.model;

import com.dot.customizations.picker.MyPhotosStarter;

/**
 * The interface for a class that can request the permission.
 */
public interface PermissionRequester {
    /**
     * Requests the {@link android.Manifest.permission#READ_EXTERNAL_STORAGE} permission.
     *
     * @param listener the listener to be notified of permissions grant status changes
     */
    void requestExternalStoragePermission(MyPhotosStarter.PermissionChangedListener listener);
}
