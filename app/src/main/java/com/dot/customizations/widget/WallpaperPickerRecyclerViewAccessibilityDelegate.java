/*
 * Copyright (C) 2020 The Android Open Source Project
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
package com.dot.customizations.widget;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerViewAccessibilityDelegate;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

/**
 * An AccessibilityDelegate class used to fix some RecyclerView-related Talkback issues.
 * <p>
 * This is to fix a bug which TalkBack can't visit all wallpaper category/wallpaper items.
 */
public class WallpaperPickerRecyclerViewAccessibilityDelegate
        extends RecyclerViewAccessibilityDelegate {

    private final RecyclerView mGridRecyclerView;
    private final BottomSheetHost mBottomSheetHost;
    private final int mColumns;
    public WallpaperPickerRecyclerViewAccessibilityDelegate(
            RecyclerView recyclerView, BottomSheetHost bottomSheetHost, int columns) {
        super(recyclerView);
        mGridRecyclerView = recyclerView;
        mBottomSheetHost = bottomSheetHost;
        mColumns = columns;
    }

    @Override
    public boolean onRequestSendAccessibilityEvent(
            ViewGroup host, View child, AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {
            int itemPos = mGridRecyclerView.getChildLayoutPosition(child);

            // Expand the bottom sheet when TB travel to second column.
            if (mBottomSheetHost != null && !mBottomSheetHost.isExpanded()
                    && itemPos >= mColumns) {
                mBottomSheetHost.expandBottomSheet();
            }
        }
        return super.onRequestSendAccessibilityEvent(host, child, event);
    }

    @Override
    public boolean performAccessibilityAction(View host, int action, Bundle args) {
        // Expand the bottom sheet when Switch Access scrolls down grid category.
        if (action == AccessibilityNodeInfoCompat.ACTION_SCROLL_FORWARD) {
            if (mBottomSheetHost != null && !mBottomSheetHost.isExpanded()) {
                mBottomSheetHost.expandBottomSheet();
            }
        }
        return super.performAccessibilityAction(host, action, args);
    }

    /**
     * Interface to be implemented by an Fragment hosting a
     * {@link WallpaperPickerRecyclerViewAccessibilityDelegate}
     */
    public interface BottomSheetHost {
        /**
         * Expands the bottom sheet if it's not expanded.
         */
        void expandBottomSheet();

        /**
         * Gets bottom sheet current state.
         */
        int getBottomSheetState();

        /**
         * Returns {@code true} if the bottom sheet is expanded.
         */
        default boolean isExpanded() {
            return getBottomSheetState() == BottomSheetBehavior.STATE_EXPANDED;
        }
    }
}
