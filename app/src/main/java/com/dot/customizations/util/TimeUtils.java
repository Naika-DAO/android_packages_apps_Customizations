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
package com.dot.customizations.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.format.DateFormat;

import androidx.annotation.Nullable;

import java.util.Calendar;

/**
 * Utility class for clock time preview.
 */
public final class TimeUtils {

    private static final CharSequence CLOCK_FORMAT_12HOUR = "h:mm";
    private static final CharSequence CLOCK_FORMAT_24HOUR = "H:mm";
    private static final CharSequence CLOCK_DOUBLE_LINE_FORMAT_12_HOUR = "hh\nmm";
    private static final CharSequence CLOCK_DOUBLE_LINE_FORMAT_24_HOUR = "HH\nmm";

    /**
     * Returns the default clock formatted time. For example: 4:35 or 16:35.
     *
     * <p> For 12-hour format, there's no AM/PM field displayed.
     */
    public static CharSequence getFormattedTime(Context context, Calendar calendar) {
        return DateFormat.format(
                DateFormat.is24HourFormat(context)
                        ? CLOCK_FORMAT_24HOUR
                        : CLOCK_FORMAT_12HOUR,
                calendar);
    }

    /**
     * Returns the double line clock formatted time.
     *
     * <p> For 12-hour format, there's no AM/PM field displayed.
     */
    public static CharSequence getDoubleLineFormattedTime(Context context,
                                                          Calendar calendar) {
        return DateFormat.format(
                DateFormat.is24HourFormat(context)
                        ? CLOCK_DOUBLE_LINE_FORMAT_24_HOUR
                        : CLOCK_DOUBLE_LINE_FORMAT_12_HOUR,
                calendar);
    }

    /**
     * BroadcastReceiver that can notify a listener when the system time (minutes) changes.
     * Use {@link #registerNewReceiver(Context, TimeListener)} to create a new instance that will be
     * automatically registered using the given Context.
     */
    public static class TimeTicker extends BroadcastReceiver {

        private TimeListener mListener;

        private TimeTicker(@Nullable TimeListener listener) {
            mListener = listener;
        }

        /**
         * Registers a broadcast receiver for time tick.
         */
        public static TimeTicker registerNewReceiver(Context context,
                                                     @Nullable TimeListener listener) {
            TimeTicker receiver = new TimeTicker(listener);
            // Register broadcast receiver for time tick
            final IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
            context.registerReceiver(receiver, filter);
            return receiver;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mListener != null) {
                mListener.onCurrentTimeChanged();
            }
        }

        /**
         * Listener for the system time's change.
         */
        public interface TimeListener {
            /**
             * Called when the system time (minutes) changes.
             */
            void onCurrentTimeChanged();
        }
    }
}
