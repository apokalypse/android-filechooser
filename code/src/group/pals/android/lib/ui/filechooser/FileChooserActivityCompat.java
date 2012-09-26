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

package group.pals.android.lib.ui.filechooser;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

/**
 * Helper for accessing features in {@link Activity} introduced in newer API
 * levels in a backwards compatible fashion. This class is <i>only</i> used for
 * {@link FileChooserActivity}.<br>
 * <br>
 * <b>Note:</b> You must check API level first with
 * {@link Build.VERSION#SDK_INT} and {@link Build.VERSION_CODES}.
 * 
 * @author Hai Bison
 * @since v4.6 beta
 * 
 */
public class FileChooserActivityCompat {

    /**
     * This isolates some custom work that's necessary to maintain compatibility
     * with ealier API levels.<br>
     * 
     * @param a
     *            {@link FileChooserActivity}
     * @see <a
     *      href="http://code.google.com/p/android-filechooser/issues/detail?id=8"
     *      title="Issue #8">Issue #8</a>
     * @since v4.6 beta
     * @author Steel.Tycoon
     */
    @TargetApi(11)
    public static void onCreateModern(FileChooserActivity a) {
        ((LinearLayout) a.findViewById(R.id.afc_filechooser_activity_view_locations))
                .setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
    }// onCreateModern()

    /**
     * This isolates some custom work that's necessary to maintain compatibility
     * with ealier API levels.<br>
     * 
     * @see <a
     *      href="http://code.google.com/p/android-filechooser/issues/detail?id=8"
     *      title="Issue #8">Issue #8</a>
     * @since v4.6 beta
     * @author Steel.Tycoon
     */
    @TargetApi(11)
    public static void onCreateOptionsMenuModern(Menu menu) {
        for (int i = 0; i < menu.size(); i++)
            menu.getItem(i).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }// onCreateOptionsMenuModern()
}
