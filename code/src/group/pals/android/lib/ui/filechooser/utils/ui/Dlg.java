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

package group.pals.android.lib.ui.filechooser.utils.ui;

import group.pals.android.lib.ui.filechooser.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Utilities for message boxes.
 * 
 * @author Hai Bison
 * @since v2.1 alpha
 */
public class Dlg {

    public static final int LENGTH_SHORT = android.widget.Toast.LENGTH_SHORT;
    public static final int LENGTH_LONG = android.widget.Toast.LENGTH_LONG;

    private static android.widget.Toast mToast;

    public static void toast(Context context, CharSequence msg, int duration) {
        if (mToast != null)
            mToast.cancel();
        mToast = android.widget.Toast.makeText(context, msg, duration);
        mToast.show();
    }// mToast()

    public static void toast(Context context, int msgId, int duration) {
        toast(context, context.getString(msgId), duration);
    }// mToast()

    public static void showInfo(Context context, CharSequence msg) {
        AlertDialog dlg = newDlg(context);
        dlg.setIcon(android.R.drawable.ic_dialog_info);
        dlg.setTitle(R.string.afc_title_info);
        dlg.setMessage(msg);
        dlg.show();
    }// showInfo()

    public static void showInfo(Context context, int msgId) {
        showInfo(context, context.getString(msgId));
    }// showInfo()

    public static void showError(Context context, CharSequence msg, DialogInterface.OnCancelListener listener) {
        AlertDialog dlg = newDlg(context);
        dlg.setIcon(android.R.drawable.ic_dialog_alert);
        dlg.setTitle(R.string.afc_title_error);
        dlg.setMessage(msg);
        dlg.setOnCancelListener(listener);
        dlg.show();
    }// showError()

    public static void showError(Context context, int msgId, DialogInterface.OnCancelListener listener) {
        showError(context, context.getString(msgId), listener);
    }// showError()

    public static void showUnknownError(Context context, Throwable t, DialogInterface.OnCancelListener listener) {
        showError(context, String.format(context.getString(R.string.afc_pmsg_unknown_error), t), listener);
    }// showUnknownError()

    public static void confirmYesno(Context context, CharSequence msg, DialogInterface.OnClickListener onYes) {
        AlertDialog dlg = newDlg(context);
        dlg.setIcon(android.R.drawable.ic_dialog_alert);
        dlg.setTitle(R.string.afc_title_confirmation);
        dlg.setMessage(msg);
        dlg.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.yes), onYes);
        dlg.show();
    }// confirmYesno()

    /**
     * Creates new {@link AlertDialog}.<br>
     * 
     * <li>Set {@link DialogInterface#BUTTON_NEGATIVE} to Cancel button, and
     * cancels the dialog when user touches it.</li>
     * 
     * <li>Set canceled on touch outside to {@code true}.</li>
     * 
     * @param context
     *            {@link Context}
     * @return {@link AlertDialog}
     * @since v4.3 beta
     */
    public static AlertDialog newDlg(Context context) {
        AlertDialog res = newDlgBuilder(context).create();
        res.setCanceledOnTouchOutside(true);
        return res;
    }// newDlg()

    /**
     * Creates new {@link AlertDialog.Builder}. Set
     * {@link DialogInterface#BUTTON_NEGATIVE} to Cancel button, and cancels the
     * dialog when user touches it.
     * 
     * @param context
     *            {@link Context}
     * @return {@link AlertDialog}
     * @since v4.3 beta
     */
    public static AlertDialog.Builder newDlgBuilder(Context context) {
        AlertDialog.Builder res = new AlertDialog.Builder(context);
        res.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return res;
    }// newDlgBuilder()
}
