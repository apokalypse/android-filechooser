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
import android.os.Handler;

/**
 * An {@link AsyncTask}, used to show {@link ProgressDialog} while doing some
 * background tasks.<br>
 * 
 * @author Hai Bison
 * @since v2.1 alpha
 */
public abstract class LoadingDialog extends AsyncTask<Void, Void, Object> {

    private final ProgressDialog fDialog;
    /**
     * Default is {@code 500}ms
     */
    private int delayTime = 500;
    /**
     * Flag to use along with {@link #delayTime}
     */
    private boolean finished = false;

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

    /**
     * If you override this method, you must call {@code super.onPreExecute()}
     * at very first of the method.
     */
    protected void onPreExecute() {
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (!finished)
                    fDialog.show();
            }
        }, getDelayTime());
    }// onPreExecute()

    /**
     * If you override this method, you must call
     * {@code super.onPostExecute(result)} at very first of the method.
     */
    protected void onPostExecute(Object result) {
        finished = true;
        fDialog.dismiss();
    }// onPostExecute()

    /**
     * If you override this method, you must call {@code super.onCancelled()} at
     * very first of the method.
     */
    protected void onCancelled() {
        finished = true;
        fDialog.dismiss();
        super.onCancelled();
    }// onCancelled()

    /**
     * Gets the delay time before showing the dialog.
     * 
     * @return the delayTime
     */
    public int getDelayTime() {
        return delayTime;
    }

    /**
     * Sets the delay time before showing the dialog.
     * 
     * @param delayTime
     *            the delayTime to set
     * @return {@link LoadingDialog}
     */
    public LoadingDialog setDelayTime(int delayTime) {
        this.delayTime = delayTime >= 0 ? delayTime : 0;
        return this;
    }
}
