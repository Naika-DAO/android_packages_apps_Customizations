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

import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperColors;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.service.wallpaper.WallpaperService;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;
import com.dot.customizations.R;
import com.dot.customizations.model.Category;
import com.dot.customizations.model.LiveWallpaperInfo;
import com.dot.customizations.model.WallpaperCategory;
import com.dot.customizations.model.WallpaperInfo;
import com.dot.customizations.module.CurrentWallpaperInfoFactory;
import com.dot.customizations.module.CurrentWallpaperInfoFactory.WallpaperInfoCallback;
import com.dot.customizations.module.InjectorProvider;
import com.dot.customizations.module.UserEventLogger;
import com.dot.customizations.module.WallpaperPersister;
import com.dot.customizations.module.WallpaperPreferences;
import com.dot.customizations.module.WallpaperPreferences.PresentationMode;
import com.dot.customizations.picker.CategorySelectorFragment.CategorySelectorFragmentHost;
import com.dot.customizations.picker.MyPhotosStarter.MyPhotosStarterProvider;
import com.dot.customizations.picker.MyPhotosStarter.PermissionChangedListener;
import com.dot.customizations.picker.individual.IndividualPickerFragment;
import com.dot.customizations.picker.individual.IndividualPickerFragment.ThumbnailUpdater;
import com.dot.customizations.picker.individual.IndividualPickerFragment.WallpaperDestinationCallback;
import com.dot.customizations.util.DeepLinkUtils;
import com.dot.customizations.util.ResourceUtils;
import com.dot.customizations.util.SizeCalculator;
import com.dot.customizations.util.WallpaperConnection;
import com.dot.customizations.util.WallpaperConnection.WallpaperConnectionListener;
import com.dot.customizations.util.WallpaperSurfaceCallback;
import com.dot.customizations.widget.LockScreenPreviewer;
import com.dot.customizations.widget.PreviewPager;
import com.dot.customizations.widget.WallpaperColorsLoader;
import com.dot.customizations.widget.WallpaperPickerRecyclerViewAccessibilityDelegate;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Displays the Main UI for picking a category of wallpapers to choose from.
 */
public class CategoryFragment extends AppbarFragment
        implements CategorySelectorFragmentHost, ThumbnailUpdater, WallpaperDestinationCallback,
        WallpaperPickerRecyclerViewAccessibilityDelegate.BottomSheetHost,
        IndividualPickerFragment.IndividualPickerFragmentHost {

    private static final String TAG = "CategoryFragment";
    private static final int SETTINGS_APP_INFO_REQUEST_CODE = 1;
    private static final String PERMISSION_READ_WALLPAPER_INTERNAL =
            "android.permission.READ_WALLPAPER_INTERNAL";
    private final Rect mPreviewLocalRect = new Rect();
    private final Rect mPreviewGlobalRect = new Rect();
    private final int[] mLivePreviewLocation = new int[2];
    private SurfaceView mWorkspaceSurface;
    private WorkspaceSurfaceHolderCallback mWorkspaceSurfaceCallback;
    private SurfaceView mWallpaperSurface;
    private WallpaperSurfaceCallback mWallpaperSurfaceCallback;
    private PreviewPager mPreviewPager;
    private List<View> mWallPaperPreviews;
    private WallpaperConnection mWallpaperConnection;
    private CategorySelectorFragment mCategorySelectorFragment;
    private IndividualPickerFragment mIndividualPickerFragment;
    private boolean mShowSelectedWallpaper;
    private BottomSheetBehavior<View> mBottomSheetBehavior;
    // The index of Destination#DEST_HOME_SCREEN or Destination#DEST_LOCK_SCREEN
    private int mWallpaperIndex;
    // The wallpaper information which is currently shown on the home preview.
    private WallpaperInfo mHomePreviewWallpaperInfo;
    // The wallpaper information which is currently shown on the lock preview.
    private WallpaperInfo mLockPreviewWallpaperInfo;
    private LockScreenPreviewer mLockScreenPreviewer;
    private View mRootContainer;
    public CategoryFragment() {
        mCategorySelectorFragment = new CategorySelectorFragment();
    }

    public static CategoryFragment newInstance(CharSequence title) {
        CategoryFragment fragment = new CategoryFragment();
        fragment.setArguments(AppbarFragment.createArguments(title));
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_category_picker, container, /* attachToRoot= */ false);

        mWallPaperPreviews = new ArrayList<>();
        CardView homePreviewCard = (CardView) inflater.inflate(
                R.layout.wallpaper_preview_card, null);
        mWorkspaceSurface = homePreviewCard.findViewById(R.id.workspace_surface);
        mWorkspaceSurfaceCallback = new WorkspaceSurfaceHolderCallback(
                mWorkspaceSurface, getContext());
        mWallpaperSurface = homePreviewCard.findViewById(R.id.wallpaper_surface);
        mWallpaperSurfaceCallback = new WallpaperSurfaceCallback(getContext(), homePreviewCard,
                mWallpaperSurface);
        mWallPaperPreviews.add(homePreviewCard);

        CardView lockscreenPreviewCard = (CardView) inflater.inflate(
                R.layout.wallpaper_preview_card, null);
        lockscreenPreviewCard.findViewById(R.id.workspace_surface).setVisibility(View.GONE);
        lockscreenPreviewCard.findViewById(R.id.wallpaper_surface).setVisibility(View.GONE);
        ViewGroup lockPreviewContainer = lockscreenPreviewCard.findViewById(
                R.id.lock_screen_preview_container);
        lockPreviewContainer.setVisibility(View.VISIBLE);
        mLockScreenPreviewer = new LockScreenPreviewer(getLifecycle(), getContext(),
                lockPreviewContainer);
        mWallPaperPreviews.add(lockscreenPreviewCard);

        mPreviewPager = view.findViewById(R.id.wallpaper_preview_pager);
        if (mPreviewPager.isRtl()) {
            Collections.reverse(mWallPaperPreviews);
        }
        mPreviewPager.setAdapter(new PreviewPagerAdapter(mWallPaperPreviews));
        mPreviewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
                // For live wallpaper, show its thumbnail when scrolling.
                if (mWallpaperConnection != null && mWallpaperConnection.isEngineReady()
                        && mHomePreviewWallpaperInfo instanceof LiveWallpaperInfo) {
                    if (positionOffset == 0.0f || positionOffset == 1.0f
                            || positionOffsetPixels == 0) {
                        // The page is not moved. Show live wallpaper.
                        mWallpaperSurface.setZOrderMediaOverlay(false);
                    } else {
                        // The page is moving. Show live wallpaper's thumbnail.
                        mWallpaperSurface.setZOrderMediaOverlay(true);
                    }
                }
            }

            @Override
            public void onPageSelected(int i) {
                mWallpaperIndex = mPreviewPager.isRtl()
                        ? (mWallPaperPreviews.size() - 1) - i
                        : i;
                if (mIndividualPickerFragment != null && mIndividualPickerFragment.isVisible()) {
                    mIndividualPickerFragment.highlightAppliedWallpaper(mWallpaperIndex);
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });
        setupCurrentWallpaperPreview(view);

        ViewGroup fragmentContainer = view.findViewById(R.id.category_fragment_container);
        mBottomSheetBehavior = BottomSheetBehavior.from(fragmentContainer);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // Update preview pager's accessibility param since it will be blocked by the
                // bottom sheet when expanded.
                mPreviewPager.setImportantForAccessibility(newState == STATE_EXPANDED
                        ? View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
                        : View.IMPORTANT_FOR_ACCESSIBILITY_YES);
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
        mRootContainer = view.findViewById(R.id.root_container);
        fragmentContainer.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View containerView, int left, int top, int right,
                                       int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int minimumHeight = mRootContainer.getHeight() - mPreviewPager.getMeasuredHeight();
                mBottomSheetBehavior.setPeekHeight(minimumHeight);
                containerView.setMinimumHeight(minimumHeight);
                homePreviewCard.setRadius(SizeCalculator.getPreviewCornerRadius(
                        getActivity(), homePreviewCard.getMeasuredWidth()));
                if (lockscreenPreviewCard != null) {
                    lockscreenPreviewCard
                            .setRadius(SizeCalculator.getPreviewCornerRadius(
                                    getActivity(), lockPreviewContainer.getMeasuredWidth()));
                }
            }
        });
        fragmentContainer.setOnApplyWindowInsetsListener((v, windowInsets) -> {
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(),
                    windowInsets.getSystemWindowInsetBottom());
            return windowInsets;
        });

        setUpToolbar(view);

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.category_fragment_container, mCategorySelectorFragment)
                .commitNow();

        // Deep link case
        Intent intent = getActivity().getIntent();
        String deepLinkCollectionId = DeepLinkUtils.getCollectionId(intent);
        if (!TextUtils.isEmpty(deepLinkCollectionId)) {
            mIndividualPickerFragment = InjectorProvider.getInjector()
                    .getIndividualPickerFragment(deepLinkCollectionId);
            mIndividualPickerFragment.highlightAppliedWallpaper(mWallpaperIndex);
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.category_fragment_container, mIndividualPickerFragment)
                    .addToBackStack(null)
                    .commit();
            getChildFragmentManager().executePendingTransactions();
            intent.setData(null);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateWallpaperSurface();
        updateWorkspaceSurface();
    }

    @Override
    public CharSequence getDefaultTitle() {
        return getContext().getString(R.string.app_name);
    }

    @Override
    public void onResume() {
        super.onResume();

        WallpaperPreferences preferences = InjectorProvider.getInjector().getPreferences(getActivity());
        preferences.setLastAppActiveTimestamp(new Date().getTime());

        // Reset Glide memory settings to a "normal" level of usage since it may have been lowered in
        // PreviewFragment.
        Glide.get(getActivity()).setMemoryCategory(MemoryCategory.NORMAL);

        // The wallpaper may have been set while this fragment was paused, so force refresh the current
        // wallpapers and presentation mode.
        if (!mShowSelectedWallpaper) {
            refreshCurrentWallpapers(/* forceRefresh= */ true);
        }
        if (mWallpaperConnection != null) {
            mWallpaperConnection.setVisibility(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mWallpaperConnection != null) {
            mWallpaperConnection.setVisibility(false);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mWallpaperConnection != null) {
            mWallpaperConnection.disconnect();
            mWallpaperConnection = null;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mWallpaperSurfaceCallback.cleanUp();
        mWorkspaceSurfaceCallback.cleanUp();
        if (mWallpaperConnection != null) {
            mWallpaperConnection.disconnect();
            mWallpaperConnection = null;
        }
        mPreviewPager.setAdapter(null);
        mWallPaperPreviews.forEach(view -> ((ViewGroup) view).removeAllViews());
        mWallPaperPreviews.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWallpaperConnection != null) {
            mWallpaperConnection.disconnect();
            mWallpaperConnection = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SETTINGS_APP_INFO_REQUEST_CODE) {
            mCategorySelectorFragment.notifyDataSetChanged();
        }
    }

    @Override
    public void requestCustomPhotoPicker(PermissionChangedListener listener) {
        getFragmentHost().getMyPhotosStarter().requestCustomPhotoPicker(listener);
    }

    @Override
    public void show(Category category) {
        if (!(category instanceof WallpaperCategory)) {
            getFragmentHost().show(category.getCollectionId());
            return;
        }
        mIndividualPickerFragment = InjectorProvider.getInjector()
                .getIndividualPickerFragment(category.getCollectionId());
        mIndividualPickerFragment.highlightAppliedWallpaper(mWallpaperIndex);
        mIndividualPickerFragment.setOnWallpaperSelectedListener(position -> {
            // Scroll to the selected wallpaper and collapse the sheet if needed.
            // Resize and scroll here because we want to let the RecyclerView's scrolling and
            // BottomSheet's collapsing can be executed together instead of scrolling
            // the RecyclerView after the BottomSheet is collapsed.
            mIndividualPickerFragment.resizeLayout(mBottomSheetBehavior.getPeekHeight());
            mIndividualPickerFragment.scrollToPosition(position);
            if (mBottomSheetBehavior.getState() != STATE_COLLAPSED) {
                mBottomSheetBehavior.setState(STATE_COLLAPSED);
            }
        });
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.category_fragment_container, mIndividualPickerFragment)
                .addToBackStack(null)
                .commit();
        getChildFragmentManager().executePendingTransactions();
    }

    @Override
    public boolean isHostToolbarShown() {
        return true;
    }

    @Override
    public void setToolbarTitle(CharSequence title) {
        setTitle(title);
    }

    @Override
    public void setToolbarMenu(int menuResId) {
        setUpToolbarMenu(menuResId);
    }

    @Override
    public void removeToolbarMenu() {
        mToolbar.getMenu().clear();
    }

    @Override
    public void moveToPreviousFragment() {
        getChildFragmentManager().popBackStack();
    }

    @Override
    public void fetchCategories() {
        getFragmentHost().fetchCategories();
    }

    @Override
    public void cleanUp() {
        getFragmentHost().cleanUp();
    }

    @Override
    public void expandBottomSheet() {
        if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    @Override
    public int getBottomSheetState() {
        return mBottomSheetBehavior.getState();
    }

    @Override
    public void updateThumbnail(WallpaperInfo wallpaperInfo) {
        new android.os.Handler().post(() -> {
            // A config change may have destroyed the activity since the refresh started, so check
            // for that.
            if (getActivity() == null) {
                return;
            }

            mHomePreviewWallpaperInfo = wallpaperInfo;
            mLockPreviewWallpaperInfo = wallpaperInfo;
            updateThumbnail(mHomePreviewWallpaperInfo,
                    mWallpaperSurfaceCallback.getHomeImageWallpaper(), true);
            mShowSelectedWallpaper = true;
        });
    }

    @Override
    public void restoreThumbnails() {
        refreshCurrentWallpapers(/* forceRefresh= */ true);
        mShowSelectedWallpaper = false;
    }

    @Override
    public void onDestinationSet(@WallpaperPersister.Destination int destination) {
        if (destination == WallpaperPersister.DEST_BOTH) {
            return;
        }
        mPreviewPager.switchPreviewPage(destination);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.daily_rotation) {
            if (mIndividualPickerFragment != null && mIndividualPickerFragment.isVisible()) {
                mIndividualPickerFragment.showRotationDialog();
            }
            return true;
        }
        return super.onMenuItemClick(item);
    }

    /**
     * Gets the {@link CategorySelectorFragment} which is attached to {@link CategoryFragment}.
     */
    public CategorySelectorFragment getCategorySelectorFragment() {
        return mCategorySelectorFragment;
    }

    /**
     * Pops the child fragment from the stack if {@link CategoryFragment} is visible to the users.
     *
     * @return {@code true} if the child fragment is popped, {@code false} otherwise.
     */
    public boolean popChildFragment() {
        return isVisible() && getChildFragmentManager().popBackStackImmediate();
    }

    private boolean canShowCurrentWallpaper() {
        Activity activity = getActivity();
        CategoryFragmentHost host = getFragmentHost();
        PackageManager packageManager = activity.getPackageManager();
        String packageName = activity.getPackageName();

        boolean hasReadWallpaperInternal = packageManager.checkPermission(
                PERMISSION_READ_WALLPAPER_INTERNAL, packageName) == PackageManager.PERMISSION_GRANTED;
        return hasReadWallpaperInternal || host.isReadExternalStoragePermissionGranted();
    }

    private void showCurrentWallpaper(View rootView, boolean show) {
        // The category/wallpaper tiles page depends on the height of the preview pager.
        // So if we want to hide the preview pager, we should use INVISIBLE instead of GONE.
        rootView.findViewById(R.id.wallpaper_preview_pager)
                .setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        rootView.findViewById(R.id.permission_needed)
                .setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void setupCurrentWallpaperPreview(View rootView) {
        if (canShowCurrentWallpaper()) {
            showCurrentWallpaper(rootView, true);
        } else {
            showCurrentWallpaper(rootView, false);

            Button mAllowAccessButton = rootView
                    .findViewById(R.id.permission_needed_allow_access_button);
            mAllowAccessButton.setOnClickListener(view ->
                    getFragmentHost().requestExternalStoragePermission(
                            new PermissionChangedListener() {

                                @Override
                                public void onPermissionsGranted() {
                                    showCurrentWallpaper(rootView, true);
                                    mCategorySelectorFragment.notifyDataSetChanged();
                                }

                                @Override
                                public void onPermissionsDenied(boolean dontAskAgain) {
                                    if (!dontAskAgain) {
                                        return;
                                    }
                                    showPermissionNeededDialog();
                                }
                            })
            );

            // Replace explanation text with text containing the Wallpapers app name which replaces
            // the placeholder.
            String appName = getString(R.string.app_name);
            String explanation = getString(R.string.permission_needed_explanation, appName);
            TextView explanationView = rootView.findViewById(R.id.permission_needed_explanation);
            explanationView.setText(explanation);
        }
    }

    private void showPermissionNeededDialog() {
        String permissionNeededMessage = getString(
                R.string.permission_needed_explanation_go_to_settings);
        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.LightDialogTheme)
                .setMessage(permissionNeededMessage)
                .setPositiveButton(android.R.string.ok, /* onClickListener= */ null)
                .setNegativeButton(
                        R.string.settings_button_label,
                        (dialogInterface, i) -> {
                            Intent appInfoIntent = new Intent();
                            appInfoIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package",
                                    getActivity().getPackageName(), /* fragment= */ null);
                            appInfoIntent.setData(uri);
                            startActivityForResult(appInfoIntent, SETTINGS_APP_INFO_REQUEST_CODE);
                        })
                .create();
        dialog.show();
    }

    private CategoryFragmentHost getFragmentHost() {
        return (CategoryFragmentHost) getActivity();
    }

    private Intent getWallpaperIntent(android.app.WallpaperInfo info) {
        return new Intent(WallpaperService.SERVICE_INTERFACE)
                .setClassName(info.getPackageName(), info.getServiceName());
    }

    /**
     * Obtains the {@link WallpaperInfo} object(s) representing the wallpaper(s) currently set to
     * the device from the {@link CurrentWallpaperInfoFactory}.
     */
    private void refreshCurrentWallpapers(boolean forceRefresh) {
        CurrentWallpaperInfoFactory factory = InjectorProvider.getInjector()
                .getCurrentWallpaperFactory(getActivity().getApplicationContext());

        factory.createCurrentWallpaperInfos(new WallpaperInfoCallback() {
            @Override
            public void onWallpaperInfoCreated(
                    final WallpaperInfo homeWallpaper,
                    @Nullable final WallpaperInfo lockWallpaper,
                    @PresentationMode final int presentationMode) {

                // Update the metadata displayed on screen. Do this in a Handler so it is scheduled at the
                // end of the message queue. This is necessary to ensure we do not remove or add data from
                // the adapter while the layout is being computed. RecyclerView documentation therefore
                // recommends performing such changes in a Handler.
                new android.os.Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        final Activity activity = getActivity();
                        // A config change may have destroyed the activity since the refresh
                        // started, so check for that.
                        if (activity == null) {
                            return;
                        }

                        mHomePreviewWallpaperInfo = homeWallpaper;
                        mLockPreviewWallpaperInfo =
                                lockWallpaper == null ? homeWallpaper : lockWallpaper;
                        updateThumbnail(mHomePreviewWallpaperInfo,
                                mWallpaperSurfaceCallback.getHomeImageWallpaper(), true);
                    }
                });
            }
        }, forceRefresh);
    }

    private void setUpLiveWallpaperPreview(WallpaperInfo homeWallpaper) {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }
        if (mWallpaperConnection != null) {
            mWallpaperConnection.disconnect();
        }

        if (WallpaperConnection.isPreviewAvailable()) {
            ImageView previewView = mWallpaperSurfaceCallback.getHomeImageWallpaper();
            mWallpaperConnection = new WallpaperConnection(
                    getWallpaperIntent(homeWallpaper.getWallpaperComponent()), activity,
                    new WallpaperConnectionListener() {
                        @Override
                        public void onWallpaperColorsChanged(WallpaperColors colors,
                                                             int displayId) {
                            if (mLockPreviewWallpaperInfo instanceof LiveWallpaperInfo) {
                                mLockScreenPreviewer.setColor(colors);
                            }
                        }
                    }, mWallpaperSurface);


            mWallpaperConnection.setVisibility(true);
            previewView.post(() -> {
                if (mWallpaperConnection != null && !mWallpaperConnection.connect()) {
                    mWallpaperConnection = null;
                }
            });
        }
    }

    private void updateThumbnail(WallpaperInfo wallpaperInfo, ImageView thumbnailView,
                                 boolean isHomeWallpaper) {
        if (wallpaperInfo == null) {
            return;
        }

        if (thumbnailView == null) {
            return;
        }

        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        UserEventLogger eventLogger = InjectorProvider.getInjector().getUserEventLogger(activity);

        boolean renderInImageWallpaperSurface =
                !(wallpaperInfo instanceof LiveWallpaperInfo) && isHomeWallpaper;
        ImageView imageView = renderInImageWallpaperSurface
                ? mWallpaperSurfaceCallback.getHomeImageWallpaper() : thumbnailView;
        if (imageView != null) {
            wallpaperInfo.getThumbAsset(activity.getApplicationContext())
                    .loadPreviewImage(activity, imageView,
                            ResourceUtils.getColorAttr(
                                    getActivity(), android.R.attr.colorSecondary));
        }

        if (isHomeWallpaper) {
            if (wallpaperInfo instanceof LiveWallpaperInfo) {
                if (mWallpaperSurfaceCallback.getHomeImageWallpaper() != null) {
                    wallpaperInfo.getThumbAsset(activity.getApplicationContext()).loadPreviewImage(
                            activity, mWallpaperSurfaceCallback.getHomeImageWallpaper(),
                            ResourceUtils.getColorAttr(
                                    getActivity(), android.R.attr.colorSecondary));
                }
                setUpLiveWallpaperPreview(wallpaperInfo);
            } else {
                if (mWallpaperConnection != null) {
                    mWallpaperConnection.disconnect();
                    mWallpaperConnection = null;
                }
            }
        } else {
            // lock screen wallpaper
            if (!(wallpaperInfo instanceof LiveWallpaperInfo)
                    || !WallpaperConnection.isPreviewAvailable()) {
                // Load wallpaper color from thumbnail for static wallpaper.
                WallpaperColorsLoader.getWallpaperColors(
                        activity,
                        wallpaperInfo.getThumbAsset(activity),
                        mLockScreenPreviewer::setColor);
            }
        }

        ((View) thumbnailView.getParent()).setOnClickListener(view -> {
            getFragmentHost().showViewOnlyPreview(wallpaperInfo, isHomeWallpaper);
            eventLogger.logCurrentWallpaperPreviewed();
        });
    }

    private void updateWallpaperSurface() {
        mWallpaperSurface.getHolder().addCallback(mWallpaperSurfaceCallback);
        mWallpaperSurface.setZOrderMediaOverlay(true);
    }

    private void updateWorkspaceSurface() {
        mWorkspaceSurface.setZOrderMediaOverlay(true);
        mWorkspaceSurface.getHolder().addCallback(mWorkspaceSurfaceCallback);
    }

    /**
     * Interface to be implemented by an Activity hosting a {@link CategoryFragment}
     */
    public interface CategoryFragmentHost extends MyPhotosStarterProvider {

        void requestExternalStoragePermission(PermissionChangedListener listener);

        boolean isReadExternalStoragePermissionGranted();

        void showViewOnlyPreview(WallpaperInfo wallpaperInfo, boolean isViewAsHome);

        void show(String collectionId);

        void fetchCategories();

        void cleanUp();
    }

    private static class PreviewPagerAdapter extends PagerAdapter {

        private List<View> mPages;

        PreviewPagerAdapter(List<View> pages) {
            mPages = pages;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position,
                                @NonNull Object object) {
            container.removeView((View) object);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = mPages.get(position);
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            return mPages.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }
    }
}
