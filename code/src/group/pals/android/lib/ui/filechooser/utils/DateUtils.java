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

package group.pals.android.lib.ui.filechooser.utils;

import group.pals.android.lib.ui.filechooser.R;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;

/**
 * Date utilities.
 * 
 * @author Hai Bison
 * @since v4.7 beta
 * 
 */
public class DateUtils {

    /**
     * Default format for file time. Value =
     * {@link DateFormat#getDateInstance()}<br>
     * See <a href=
     * "http://developer.android.com/reference/java/text/SimpleDateFormat.html"
     * >API docs</a>.
     */
    public static final DateFormat _DefFileTimeShortFormat = DateFormat.getDateInstance();

    /**
     * You can set your own short format for file time by this variable. Default
     * is equal to {@link #_DefFileTimeShortFormat}<br>
     * See <a href=
     * "http://developer.android.com/reference/java/text/SimpleDateFormat.html"
     * >API docs</a>.
     */
    public static DateFormat fileTimeShortFormat = _DefFileTimeShortFormat;

    /**
     * Used with format methods of {@link android.text.format.DateUtils}. For
     * example: "10:01 AM".
     */
    public static final int _FormatShortTime = android.text.format.DateUtils.FORMAT_12HOUR
            | android.text.format.DateUtils.FORMAT_SHOW_TIME;

    /**
     * Used with format methods of {@link android.text.format.DateUtils}. For
     * example: "Oct 01".
     */
    public static final int _FormatMonthAndDay = android.text.format.DateUtils.FORMAT_ABBREV_MONTH
            | android.text.format.DateUtils.FORMAT_SHOW_DATE | android.text.format.DateUtils.FORMAT_NO_YEAR;

    /**
     * Formats date.
     * 
     * @param millis
     *            time in milliseconds
     * @return the formatted string
     */
    public static String formatDate(Context context, long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        return formatDate(context, cal);
    }// formatDate()

    /**
     * Formats date.
     * 
     * @param date
     *            {@link Date}
     * @return the formatted string
     */
    public static String formatDate(Context context, Calendar date) {
        final Calendar _yesterday = Calendar.getInstance();
        _yesterday.add(Calendar.DAY_OF_YEAR, -1);

        String res;

        if (android.text.format.DateUtils.isToday(date.getTimeInMillis())) {
            res = android.text.format.DateUtils.formatDateTime(context, date.getTimeInMillis(), _FormatShortTime);
        }// today
        else if (date.get(Calendar.YEAR) == _yesterday.get(Calendar.YEAR)
                && date.get(Calendar.DAY_OF_YEAR) == _yesterday.get(Calendar.DAY_OF_YEAR)) {
            res = String.format("%s, %s", context.getString(R.string.afc_yesterday),
                    android.text.format.DateUtils.formatDateTime(context, date.getTimeInMillis(), _FormatShortTime));
        }// yesterday
        else if (date.get(Calendar.YEAR) == _yesterday.get(Calendar.YEAR)) {
            res = android.text.format.DateUtils.formatDateTime(context, date.getTimeInMillis(), _FormatShortTime
                    | _FormatMonthAndDay);
        }// this year
        else {
            res = fileTimeShortFormat.format(date.getTime());
        }// older

        return res;
    }// formatDate()
}
