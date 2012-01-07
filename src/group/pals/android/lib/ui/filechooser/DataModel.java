package group.pals.android.lib.ui.filechooser;

import java.io.File;

public class DataModel {

  private File file;
  private boolean selected;

  public DataModel(File file) {
    this.file = file;
  }

  public File getFile() {
    return file;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }
}
