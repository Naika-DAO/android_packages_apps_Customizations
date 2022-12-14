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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.dot.customizations.R;
import com.dot.customizations.module.WallpaperPersister.Destination;

/**
 * Dialog fragment which communicates a message that setting the wallpaper failed with an option to
 * try again.
 */
public class SetWallpaperErrorDialogFragment extends DialogFragment {

    private static final String ARG_MESSAGE = "message";
    private static final String ARG_WALLPAPER_DESTINATION = "destination";

    public static SetWallpaperErrorDialogFragment newInstance(int messageId,
                                                              @Destination int wallpaperDestination) {
        SetWallpaperErrorDialogFragment dialogFrag = new SetWallpaperErrorDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MESSAGE, messageId);
        args.putInt(ARG_WALLPAPER_DESTINATION, wallpaperDestination);
        dialogFrag.setArguments(args);
        return dialogFrag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        int message = getArguments().getInt(ARG_MESSAGE);
        @Destination final int wallpaperDestination = getArguments().getInt(ARG_WALLPAPER_DESTINATION);

        return new AlertDialog.Builder(getActivity(), R.style.LightDialogTheme)
                .setMessage(message)
                .setPositiveButton(R.string.try_again,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // The component hosting this DialogFragment could be either a Fragment or an
                                // Activity, so check if a target Fragment was explicitly set--if not then the
                                // appropriate Listener would be the containing Activity.
                                Fragment fragment = getTargetFragment();
                                Activity activity = getActivity();

                                Listener callback = (Listener) (fragment == null ? activity : fragment);

                                if (callback != null) {
                                    callback.onClickTryAgain(wallpaperDestination);
                                }
                            }
                        }
                )
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    /**
     * Interface which clients of this DialogFragment should implement in order to handle user actions
     * on the dialog's buttons.
     */
    public interface Listener {
        void onClickTryAgain(@Destination int wallpaperDestination);
    }
}
