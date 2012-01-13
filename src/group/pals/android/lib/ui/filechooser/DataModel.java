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

package group.pals.android.lib.ui.filechooser;

import java.io.File;

/**
 * This class is used to hold data ({@link File}) in {@link android.widget.ArrayAdapter}
 * @author Hai Bison
 *
 */
public class DataModel {

  private File file;
  private boolean selected;

  /**
   * Creates new {@link DataModel} with a {@link File}
   * @param file
   */
  public DataModel(File file) {
    this.file = file;
  }

  /**
   * Gets the file.
   * @return {@link File}
   */
  public File getFile() {
    return file;
  }

  /**
   * Gets the status of this item (listed in {@link android.widget.ListView})
   * @return {@code true} if the item is selected, {@code false} otherwise
   */
  public boolean isSelected() {
    return selected;
  }

  /**
   * Sets the status of this item (listed in {@link android.widget.ListView})
   * @param selected
   */
  public void setSelected(boolean selected) {
    this.selected = selected;
  }
}
