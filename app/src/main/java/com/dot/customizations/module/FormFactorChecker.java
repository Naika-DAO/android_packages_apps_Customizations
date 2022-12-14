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

import androidx.annotation.IntDef;

/**
 * Checks the form factor of the device.
 */
public interface FormFactorChecker {
    int FORM_FACTOR_DESKTOP = 0;
    int FORM_FACTOR_MOBILE = 1;

    @FormFactor
    int getFormFactor();

    /**
     * The possible form factors.
     */
    @IntDef({
            FORM_FACTOR_DESKTOP,
            FORM_FACTOR_MOBILE})
    @interface FormFactor {
    }
}
