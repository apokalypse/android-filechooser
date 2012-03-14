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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

/**
 * An {@link AsyncTask}, used to show {@link ProgressDialog} while doing some background
 * tasks.<br>
 * 
 * @author Hai Bison
 * @since v2.1 alpha
 */
public abstract class LoadingDialog extends AsyncTask<Void, Void, Object> {

    private final ProgressDialog fDialog;

    /**
     * Creates new {@link LoadingDialog}
     * 
     * @param context
     *            {@link Context}
     * @param msg
     *            message will be shown in the dialog.
     * @param cancelable
     *            as the name means.
     */
    public LoadingDialog(Context context, String msg, boolean cancelable) {
        fDialog = new ProgressDialog(context);
        fDialog.setMessage(msg);
        fDialog.setIndeterminate(true);
        fDialog.setCancelable(cancelable);

        fDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                cancel(false);
            }
        });
    }// LoadingDialog

    /**
     * Creates new {@link LoadingDialog}
     * 
     * @param context
     *            {@link Context}
     * @param msgId
     *            resource id of the message will be shown in the dialog.
     * @param cancelable
     *            as the name means.
     */
    public LoadingDialog(Context context, int msgId, boolean cancelable) {
        this(context, context.getString(msgId), cancelable);
    }

    protected void onPreExecute() {
        fDialog.show();
    }// onPreExecute()

    protected void onPostExecute(Object result) {
        fDialog.dismiss();
    }// onPostExecute()

    protected void onCancelled () {
        fDialog.dismiss();
        super.onCancelled();
    }// onCancelled()
}
