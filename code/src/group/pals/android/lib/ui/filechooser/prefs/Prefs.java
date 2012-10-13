/*
 *   Copyright 2012 Hai Bison
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package group.pals.android.lib.ui.filechooser.prefs;

import group.pals.android.lib.ui.filechooser.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Convenient class for working with preferences.
 * 
 * @author Hai Bison
 * @since v4.3 beta
 */
public class Prefs {

    /**
     * This unique ID is used for storing preferences.
     * 
     * @since v4.9 beta
     */
    public static final String _Uid = "9795e88b-2ab4-4b81-a548-409091a1e0c6";

    /**
     * Generates global preference filename of this library.
     * 
     * @param context
     *            {@link Context} - will be used to obtain the application
     *            context.
     * @return the global preference filename.
     */
    public static final String genPreferenceFilename(Context context) {
        return String.format("%s_%s", context.getString(R.string.afc_lib_name), _Uid);
    }

    /**
     * Gets new {@link SharedPreferences}
     * 
     * @param context
     *            {@link Context}
     * @return {@link SharedPreferences}
     */
    public static SharedPreferences p(Context context) {
        // always use application context
        return context.getApplicationContext().getSharedPreferences(genPreferenceFilename(context),
                Context.MODE_MULTI_PROCESS);
    }

    /**
     * Setup {@code pm} to use global unique filename and global access mode.
     * You must use this method if you let the user change preferences via UI
     * (such as {@link PreferenceActivity}, {@link PreferenceFragment}...).
     * 
     * @param c
     *            {@link Context}.
     * @param pm
     *            {@link PreferenceManager}.
     * @since v4.9 beta
     */
    public static void setupPreferenceManager(Context c, PreferenceManager pm) {
        pm.setSharedPreferencesMode(Context.MODE_MULTI_PROCESS);
        pm.setSharedPreferencesName(genPreferenceFilename(c));
    }// setupPreferenceManager()
}
