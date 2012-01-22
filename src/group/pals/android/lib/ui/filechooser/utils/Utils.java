package group.pals.android.lib.ui.filechooser.utils;

import java.io.File;

public class Utils {

  /**
   * Checks whether the filename given is valid or not.<br>
   * See <a href="http://en.wikipedia.org/wiki/Filename">wiki</a> for more information.
   * @param name name of the file
   * @return {@code true} if the pathname is valid, and vice versa
   */
  public static boolean isFilenameValid(String name) {
    return name.matches("[^\\\\/?%*:|\"<>]*");
  }
}
