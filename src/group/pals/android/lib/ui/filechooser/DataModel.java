package group.pals.android.lib.ui.filechooser;

import java.io.File;

/**
 * This class is used to hold data ({@link File}) in {@link android.widget.ArrayAdapter}
 * @author Haiti Meid
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
