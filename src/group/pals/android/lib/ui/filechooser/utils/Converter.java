package group.pals.android.lib.ui.filechooser.utils;

public class Converter {

  /**
   * Converts <code>size</code> (in bytes) to string.
   * @param size the size in bytes.
   * @return e.g.:<br>
   * - 1 byte<br>
   * - 128 bytes<br>
   * - 1.5 KB<br>
   * - 10 MB<br>
   * - ...
   */
  public static String sizeToStr(double size) {
    final short BlockSize = 1024;
    final String DisplayUnits[] = {"byte", "KB", "MB", "GB", "TB"};

    byte i = 0;
    while (true) {
      if ((size < BlockSize) || (i == (DisplayUnits.length - 1))) {
        if (i == 0) {
          String result = String.format("%.0f %s", size, DisplayUnits[i]);
          if (size > 1)
            result += "s";
          return result;
        } else {
          return String.format("%02.02f %s", size, DisplayUnits[i]);
        }
      } else {
        size /= BlockSize;
      }
      i++;
    }
  }//sizeToStr()
}
