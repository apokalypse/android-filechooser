/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.utils;

import android.content.Context;
import android.graphics.Paint;
import android.view.View;
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
     * Shows/ hides soft input (soft keyboard).
     * 
     * @param view
     *            {@link View}.
     * @param show
     *            {@code true} or {@code false}. If {@code true}, this method
     *            will use a {@link Runnable} to show the IMM. So you don't need
     *            to use it, and consider using
     *            {@link View#removeCallbacks(Runnable)} if you want to cancel.
     */
    public static void showSoftKeyboard(final View view, final boolean show) {
        final InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null)
            return;

        if (show) {
            view.post(new Runnable() {

                @Override
                public void run() {
                    imm.showSoftInput(view, 0, null);
                }// run()
            });
        } else
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0, null);
    }// showSoftKeyboard()

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
            view.setPaintFlags(view.getPaintFlags()
                    | Paint.STRIKE_THRU_TEXT_FLAG);
        else
            view.setPaintFlags(view.getPaintFlags()
                    & ~Paint.STRIKE_THRU_TEXT_FLAG);
    }// strikeOutText()
}
