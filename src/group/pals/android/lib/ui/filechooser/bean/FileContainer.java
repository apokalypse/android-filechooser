package group.pals.android.lib.ui.filechooser.bean;

import java.io.File;

/**
 * To store {@link File}
 * @author Haiti Meid
 *
 */
public class FileContainer {

  private File file;

  /**
   * Creates new {@link FileContainer}
   * @param file
   */
  public FileContainer(File file) {
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
   * Sets file by a {@link File}
   * @param file
   */
  public void setFile(File file) {
    this.file = file;
  }

  /**
   * Sets file by a pathname
   * @param pathname
   */
  public void setFile(String pathname) {
    this.setFile(new File(pathname));
  }
}
