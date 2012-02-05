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

/**
 * Utilities.
 */
public class Utils {

  /**
   * Checks whether the filename given is valid or not.<br>
   * See <a href="http://en.wikipedia.org/wiki/Filename">wiki</a> for more information.
   * @param name name of the file
   * @return {@code true} if the {@code name} is valid, and vice versa (if it
   * contains invalid characters or it is {@code null}/ empty)
   */
  public static boolean isFilenameValid(String name) {
    return name != null && name.trim().matches("[^\\\\/?%*:|\"<>]+");
  }
}
