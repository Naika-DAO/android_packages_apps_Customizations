package com.dot.customizations.picker.extras

import android.content.Context
import android.provider.Settings
import com.dot.customizations.model.CustomizationSectionController
import com.dot.customizations.picker.AppbarFragment
import de.Maxr1998.modernpreferences.Preference
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.helpers.*
import de.Maxr1998.modernpreferences.preferences.SeekBarPreference
import de.Maxr1998.modernpreferences.preferences.choice.SelectionItem
import de.Maxr1998.modernpreferences.preferences.choice.SingleChoiceDialogPreference

fun PreferenceScreen.Builder.fragmentPreference(
    navigationController: CustomizationSectionController.CustomizationSectionNavigationController,
    fragment: AppbarFragment
): Preference {
    return pref(fragment::class.java.name) {
        onClickView { navigationController.navigateTo(fragment) }
    }
}

/**
 * [Note] : Selection keys must match Settings Provider's values
 */
fun PreferenceScreen.Builder.systemSettingsSingleChoice(
    context: Context,
    mTitle: CharSequence? = null,
    mTitleRes: Int? = null,
    mSelections: ArrayList<SelectionItem>,
    mDefault: Int,
    setting: String
) : SingleChoiceDialogPreference {
    return singleChoice(setting, mSelections) {
        title = if (mTitleRes != null) context.getString(mTitleRes) else mTitle.toString()
        initialSelection = Settings.System.getInt(context.contentResolver, setting.lowercase(), mDefault).toString()
        onSelectionChange {
            Settings.System.putInt(context.contentResolver, setting.lowercase(), it.toInt())
        }
    }
}

/**
 * [Note] : Selection keys must match Settings Provider's values
 */
fun PreferenceScreen.Builder.secureSettingsSingleChoice(
    context: Context,
    mTitle: CharSequence? = null,
    mTitleRes: Int? = null,
    mSelections: ArrayList<SelectionItem>,
    mDefault: Int,
    setting: String
) : SingleChoiceDialogPreference {
    return singleChoice(setting, mSelections) {
        title = if (mTitleRes != null) context.getString(mTitleRes) else mTitle.toString()
        initialSelection = Settings.Secure.getInt(context.contentResolver, setting.lowercase(), mDefault).toString()
        onSelectionChange {
            Settings.Secure.putInt(context.contentResolver, setting.lowercase(), it.toInt())
        }
    }
}

fun PreferenceScreen.Builder.secureSettingsSeekBar(
    context: Context,
    mTitle: CharSequence? = null,
    mTitleRes: Int? = null,
    mSummary: CharSequence? = null,
    mSummaryRes: Int? = null,
    mMin: Int = 0,
    mMax: Int = 5,
    mDefault: Int = 3,
    setting: String
): Preference {
    return seekBar(setting) {
        title = if (mTitleRes != null) context.getString(mTitleRes) else mTitle.toString()
        summary = if (mSummaryRes != null) context.getString(mSummaryRes) else mSummary
        min = mMin
        max = mMax
        default = Settings.Secure.getInt(context.contentResolver, setting.lowercase(), mDefault)
        seekListener = SeekBarPreference.OnSeekListener { _, _, value ->
            Settings.Secure.putInt(context.contentResolver, setting.lowercase(), value)
        }
    }
}

fun PreferenceScreen.Builder.systemSettingsSeekBar(
    context: Context,
    mTitle: CharSequence? = null,
    mTitleRes: Int? = null,
    mSummary: CharSequence? = null,
    mSummaryRes: Int? = null,
    mMin: Int = 0,
    mMax: Int = 5,
    mDefault: Int = 3,
    setting: String
): Preference {
    return seekBar(setting) {
        title = if (mTitleRes != null) context.getString(mTitleRes) else mTitle.toString()
        summary = if (mSummaryRes != null) context.getString(mSummaryRes) else mSummary
        min = mMin
        max = mMax
        default = Settings.System.getInt(context.contentResolver, setting.lowercase(), mDefault)
        seekListener = SeekBarPreference.OnSeekListener { _, _, value ->
            Settings.System.putInt(context.contentResolver, setting.lowercase(), value)
        }
    }
}

fun PreferenceScreen.Builder.globalSettingsSwitch(
    context: Context,
    mTitle: CharSequence? = null,
    mTitleRes: Int? = null,
    mSummary: CharSequence? = null,
    mSummaryRes: Int? = null,
    mSummaryOn: CharSequence? = null,
    mSummaryOnRes: Int? = null,
    mDefault: Int = 0,
    setting: String
): Preference {
    return switch(setting) {
        title = if (mTitleRes != null) context.getString(mTitleRes) else mTitle.toString()
        summary = if (mSummaryRes != null) context.getString(mSummaryRes) else mSummary
        summaryOn = if (mSummaryOnRes != null) context.getString(mSummaryOnRes) else mSummaryOn
        onCheckedChange {
            Settings.Global.putInt(context.contentResolver, setting.lowercase(), if (it) 1 else 0)
        }
        defaultValue = Settings.Global.getInt(context.contentResolver, setting.lowercase(), mDefault) == 1
    }
}

fun PreferenceScreen.Builder.systemSettingsSwitch(
    context: Context,
    mTitle: CharSequence? = null,
    mTitleRes: Int? = null,
    mSummary: CharSequence? = null,
    mSummaryRes: Int? = null,
    mSummaryOn: CharSequence? = null,
    mSummaryOnRes: Int? = null,
    mDefault: Int = 0,
    setting: String
): Preference {
    return switch(setting) {
        title = if (mTitleRes != null) context.getString(mTitleRes) else mTitle.toString()
        summary = if (mSummaryRes != null) context.getString(mSummaryRes) else mSummary
        summaryOn = if (mSummaryOnRes != null) context.getString(mSummaryOnRes) else mSummaryOn
        onCheckedChange {
            Settings.System.putInt(context.contentResolver, setting.lowercase(), if (it) 1 else 0)
        }
        defaultValue = Settings.System.getInt(context.contentResolver, setting.lowercase(), mDefault) == 1
    }
}

fun PreferenceScreen.Builder.secureSettingsSwitch(
    context: Context,
    mTitle: CharSequence? = null,
    mTitleRes: Int? = null,
    mSummary: CharSequence? = null,
    mSummaryRes: Int? = null,
    mSummaryOn: CharSequence? = null,
    mSummaryOnRes: Int? = null,
    mDefault: Int = 0,
    setting: String
): Preference {
    return switch(setting) {
        title = if (mTitleRes != null) context.getString(mTitleRes) else mTitle.toString()
        summary = if (mSummaryRes != null) context.getString(mSummaryRes) else mSummary
        summaryOn = if (mSummaryOnRes != null) context.getString(mSummaryOnRes) else mSummaryOn
        onCheckedChange {
            Settings.Secure.putInt(context.contentResolver, setting.lowercase(), if (it) 1 else 0)
        }
        defaultValue = Settings.Secure.getInt(context.contentResolver, setting.lowercase(), mDefault) == 1
    }
}