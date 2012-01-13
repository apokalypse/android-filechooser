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
