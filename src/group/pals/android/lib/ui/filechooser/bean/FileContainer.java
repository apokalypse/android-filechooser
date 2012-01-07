package group.pals.android.lib.ui.filechooser.bean;

import java.io.File;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class FileContainer implements Parcelable {

  private File file;

  public FileContainer(File file) {
    this.file = file;
  }

  public File getFile() {
    return file;
  }

  public void setFile(File file) {
    this.file = file;
  }

  public void setFile(String pathname) {
    this.setFile(new File(pathname));
  }

  private static final String KeyFilepath = "0";

  @Override
  public int describeContents() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    Bundle b = new Bundle();
    b.putString(KeyFilepath, getFile().getAbsolutePath());
    dest.writeBundle(b);
  }

  public static final Parcelable.Creator<FileContainer> CREATOR = new Parcelable.Creator<FileContainer> () {

    public FileContainer createFromParcel(Parcel in) {
      return new FileContainer(in);
    }

    public FileContainer[] newArray(int size) {
      return new FileContainer[size];
    }
  };

  private FileContainer(Parcel in) {
    Bundle b = in.readBundle();
    setFile(b.getString(KeyFilepath));
  }
}
