package group.pals.android.lib.ui.filechooser.utils;

import group.pals.android.lib.ui.filechooser.bean.FileContainer;

import java.util.ArrayList;
import java.util.List;

public class HistoryPath {

  private List<FileContainer> list = new ArrayList<FileContainer>();
  private final int MaxSize;

  public HistoryPath(int maxSize) {
    this.MaxSize = (maxSize > 0 && maxSize <= 100) ? maxSize : 11;
  }

  public void push(FileContainer currentPath, FileContainer newPath) {
    int idx = currentPath == null ? -1 : list.indexOf(currentPath);
    if (idx < 0 || idx == size() - 1)
      list.add(newPath);
    else {
      list = list.subList(0, idx + 1);
      list.add(newPath);
    }

    if (list.size() > MaxSize)
      list.remove(0);
  }

  public int size() {
    return list.size();
  }

  public int indexOf(FileContainer path) {
    return list.indexOf(path);
  }

  public FileContainer getPrev(FileContainer path) {
    int idx = list.indexOf(path);
    if (idx > 0)
      return list.get(idx - 1);
    return null;
  }

  public FileContainer getNext(FileContainer path) {
    int idx = list.indexOf(path);
    if (idx >= 0 && idx < list.size() - 1)
      return list.get(idx + 1);
    return null;
  }
}
