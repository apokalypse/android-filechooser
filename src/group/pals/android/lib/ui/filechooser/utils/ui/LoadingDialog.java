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
import android.os.Handler;
import android.os.Message;

/**
 * A thread, used to show {@link ProgressDialog} while doing some background tasks.
 * @author Hai Bison
 * @since v1.8
 */
public abstract class LoadingDialog extends Thread {

  private final ProgressDialog Dialog;

  private final int MsgShowProgressDialog = 0;
  private final int MsgShowException = 1;
  private final int MsgFinish = 2;

  private final Handler _Handler;

  /**
   * Creates new {@link LoadingDialog}
   * @param context {@link Context}
   * @param msg message will be shown in the dialog.
   * @param cancelable as the name means.
   */
  public LoadingDialog(Context context, String msg, boolean cancelable) {
    Dialog = new ProgressDialog(context);
    Dialog.setMessage(msg);
    Dialog.setIndeterminate(true);
    Dialog.setCancelable(cancelable);

    _Handler = new Handler() {

      @Override
      public void handleMessage(Message msg) {
        switch (msg.what) {
          case MsgShowProgressDialog:
            Dialog.show();
            break;
          case MsgShowException:
            onRaise((Throwable) msg.obj);
            break;
          case MsgFinish:
            onFinish();
            break;
        }
      }
    };
  }

  /**
   * Creates new {@link LoadingDialog}
   * @param context {@link Context}
   * @param msgId resource id of the message will be shown in the dialog.
   * @param cancelable as the name means.
   */
  public LoadingDialog(Context context, int msgId, boolean cancelable) {
    this(context, context.getString(msgId), cancelable);
  }

  @Override
  public void run() {
    _Handler.sendEmptyMessage(MsgShowProgressDialog);

    boolean hasError = false;
    try {
      onExecute();
    } catch (Throwable t) {
      hasError = true;

      Message msg = new Message();
      msg.obj = t;
      msg.what = MsgShowException;
      _Handler.sendMessage(msg);
    } finally {
      Dialog.dismiss();
      if (!hasError) _Handler.sendEmptyMessage(MsgFinish);
    }
  }//run

  /**
   * See {@link ProgressDialog#setOnCancelListener(android.content.DialogInterface.OnCancelListener)}
   * @param listener {@link DialogInterface.OnCancelListener}
   */
  public void setOnCancelListener(DialogInterface.OnCancelListener listener) {
    Dialog.setOnCancelListener(listener);
  }

  /**
   * Will be called inside {@code run} method.
   */
  public abstract void onExecute() throws Throwable;

  /**
   * Will be called after {@code run} method finished.
   */
  public abstract void onFinish();

  /**
   * Will be called when any exception raises.
   */
  public abstract void onRaise(Throwable t);
}
