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

import group.pals.android.lib.ui.filechooser.FileChooserActivity.ViewType;
import group.pals.android.lib.ui.filechooser.R;
import group.pals.android.lib.ui.filechooser.services.IFileProvider.SortType;
import android.content.Context;

/**
 * Display preferences.
 * 
 * @author Hai Bison
 * @since v4.3 beta
 * 
 */
public class DisplayPrefs extends Prefs {

    /**
     * Delay time for waiting for other threads inside a thread... This is in
     * milliseconds.
     */
    public static final int _DelayTimeWaitingThreads = 10;

    /**
     * Gets view type.
     * 
     * @param c
     *            {@link Context}
     * @return {@link ViewType}
     */
    public static ViewType getViewType(Context c) {
        return ViewType.List.ordinal() == p(c).getInt(c.getString(R.string.afc_pkey_display_view_type),
                c.getResources().getInteger(R.integer.afc_pkey_display_view_type_def)) ? ViewType.List : ViewType.Grid;
    }

    /**
     * Sets view type.
     * 
     * @param c
     *            {@link Context}
     * @param v
     *            {@link ViewType}, if {@code null}, default value will be used.
     */
    public static void setViewType(Context c, ViewType v) {
        if (v == null)
            p(c).edit()
                    .putInt(c.getString(R.string.afc_pkey_display_view_type),
                            c.getResources().getInteger(R.integer.afc_pkey_display_view_type_def)).commit();
        else
            p(c).edit().putInt(c.getString(R.string.afc_pkey_display_view_type), v.ordinal()).commit();
    }

    /**
     * Gets sort type.
     * 
     * @param c
     *            {@link Context}
     * @return {@link SortType}
     */
    public static SortType getSortType(Context c) {
        for (SortType s : SortType.values())
            if (s.ordinal() == p(c).getInt(c.getString(R.string.afc_pkey_display_sort_type),
                    c.getResources().getInteger(R.integer.afc_pkey_display_sort_type_def)))
                return s;
        return SortType.SortByName;
    }

    /**
     * Sets {@link SortType}
     * 
     * @param c
     *            {@link Context}
     * @param v
     *            {@link SortType}, if {@code null}, default value will be used.
     */
    public static void setSortType(Context c, SortType v) {
        if (v == null)
            p(c).edit()
                    .putInt(c.getString(R.string.afc_pkey_display_sort_type),
                            c.getResources().getInteger(R.integer.afc_pkey_display_sort_type_def)).commit();
        else
            p(c).edit().putInt(c.getString(R.string.afc_pkey_display_sort_type), v.ordinal()).commit();
    }

    /**
     * Gets sort ascending.
     * 
     * @param c
     *            {@link Context}
     * @return {@code true} if sort is ascending, {@code false} otherwise.
     */
    public static boolean isSortAscending(Context c) {
        return p(c).getBoolean(c.getString(R.string.afc_pkey_display_sort_ascending),
                c.getResources().getBoolean(R.bool.afc_pkey_display_sort_ascending_def));
    }

    /**
     * Sets sort ascending.
     * 
     * @param c
     *            {@link Context}
     * @param v
     *            {@link Boolean}, if {@code null}, default value will be used.
     */
    public static void setSortAscending(Context c, Boolean v) {
        if (v == null)
            p(c).edit()
                    .putBoolean(c.getString(R.string.afc_pkey_display_sort_ascending),
                            c.getResources().getBoolean(R.bool.afc_pkey_display_sort_ascending_def)).commit();
        else
            p(c).edit().putBoolean(c.getString(R.string.afc_pkey_display_sort_ascending), v).commit();
    }
}