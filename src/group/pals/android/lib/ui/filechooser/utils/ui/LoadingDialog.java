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
import android.util.Log;

/**
 * An {@link AsyncTask}, used to show {@link ProgressDialog} while doing some
 * background tasks.<br>
 * 
 * @author Hai Bison
 * @since v2.1 alpha
 */
public abstract class LoadingDialog extends AsyncTask<Void, Void, Object> {

    public static final String ClassName = LoadingDialog.class.getName();

    private final ProgressDialog fDialog;
    /**
     * Default is {@code 500}ms
     */
    private int delayTime = 500;
    /**
     * Flag to use along with {@link #delayTime}
     */
    private boolean finished = false;

    private Throwable lastException;

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
                if (!finished) {
                    try {
                        /*
                         * sometime the activity has been finished before we
                         * show this dialog, it will raise error
                         */
                        fDialog.show();
                    } catch (Throwable t) {
                        // TODO
                        Log.e(ClassName, "onPreExecute() - show dialog: " + t);
                    }
                }
            }
        }, getDelayTime());
    }// onPreExecute()

    /**
     * If you override this method, you must call
     * {@code super.onPostExecute(result)} at very first of the method.
     */
    protected void onPostExecute(Object result) {
        doFinish();
    }// onPostExecute()

    /**
     * If you override this method, you must call {@code super.onCancelled()} at
     * very first of the method.
     */
    protected void onCancelled() {
        doFinish();
        super.onCancelled();
    }// onCancelled()

    private void doFinish() {
        finished = true;
        try {
            /*
             * sometime the activity has been finished before we dismiss this
             * dialog, it will raise error
             */
            fDialog.dismiss();
        } catch (Throwable t) {
            // TODO
            Log.e(ClassName, "doFinish() - dismiss dialog: " + t);
        }
    }// doFinish()

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

    /**
     * Sets last exception. This method is useful in case an exception raises
     * inside {@link #doInBackground(Void...)}
     * 
     * @param t
     *            {@link Throwable}
     */
    protected void setLastException(Throwable t) {
        lastException = t;
    }

    /**
     * Gets last exception.
     * 
     * @return {@link Throwable}
     */
    protected Throwable getLastException() {
        return lastException;
    }
}
