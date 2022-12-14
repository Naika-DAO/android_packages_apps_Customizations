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
package com.dot.customizations.model;

import android.content.Context;
import android.content.Intent;

/**
 * Factory for getting an intent to show the in-app (inline) picker activity for a given list of
 * wallpapers, if appropriate for that category.
 */
public interface PickerIntentFactory {
    /**
     * Returns an intent to show the picker activity for the given category of wallpapers.
     */
    Intent newIntent(Context ctx, String collectionId);
}
