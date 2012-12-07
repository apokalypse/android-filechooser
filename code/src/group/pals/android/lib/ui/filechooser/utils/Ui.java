/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.utils;

import android.content.Context;
import android.graphics.Paint;
import android.os.IBinder;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

/**
 * UI utilities.
 * 
 * @author Hai Bison
 * 
 */
public class Ui {

    /**
     * Hides soft keyboard.
     * 
     * @param context
     *            {@link Context}.
     * @param binder
     *            {@link IBinder}.
     */
    public static void hideSoftKeyboard(Context context, IBinder binder) {
        /*
         * hide soft keyboard
         * http://stackoverflow.com/questions/1109022/how-to-close
         * -hide-the-android-soft-keyboard
         */
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null)
            imm.hideSoftInputFromWindow(binder, 0);
    }// hideSoftKeyboard()

    /**
     * Strikes out text of {@code view}.
     * 
     * @param view
     *            {@link TextView}.
     * @param strikeOut
     *            {@code true} to strike out the text.
     */
    public static void strikeOutText(TextView view, boolean strikeOut) {
        if (strikeOut)
            view.setPaintFlags(view.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        else
            view.setPaintFlags(view.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
    }// strikeOutText()
}
