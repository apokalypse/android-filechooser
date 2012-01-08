package group.pals.android.lib.ui.filechooser.utils;

import android.content.Context;
import android.os.IBinder;
import android.view.inputmethod.InputMethodManager;

public class UI {

  public static void hideSoftKeyboard(Context context, IBinder iBinder) {
    /*
     * hide soft keyboard
     * http://stackoverflow.com/questions/1109022/how-to-close-hide-the-android-soft-keyboard
     */
    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    if (imm != null)
      imm.hideSoftInputFromWindow(iBinder, 0);
  }

}
