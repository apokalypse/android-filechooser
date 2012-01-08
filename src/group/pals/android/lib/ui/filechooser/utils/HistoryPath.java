package group.pals.android.lib.ui.filechooser.utils;

import group.pals.android.lib.ui.filechooser.bean.FileContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used as a history store of {@link FileContainer}
 * @author Haiti Meid
 *
 */
public class HistoryPath {

  private List<FileContainer> list = new ArrayList<FileContainer>();
  private final int MaxSize;

  /**
   * Creates new {@link HistoryPath}
   * @param maxSize the maximum size that allowed
   */
  public HistoryPath(int maxSize) {
    this.MaxSize = (maxSize > 0 && maxSize <= 100) ? maxSize : 11;
  }

  /**
   * Pushes new path to the history.
   * @param currentPath usage: assume we have history of: 1-2-3-4,
   * if current path is 3, and we push 5 to the history, then
   * new history will be 1-2-3-5
   * @param newPath the new path
   */
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

  /**
   * 
   * @return the size of this history
   */
  public int size() {
    return list.size();
  }

  /**
   * 
   * @param path a {@link FileContainer}
   * @return index of the path, or -1 if there is no one
   */
  public int indexOf(FileContainer path) {
    return list.indexOf(path);
  }

  /**
   * Gets previous path of a path
   * @param path current path
   * @return the previous path
   */
  public FileContainer getPrev(FileContainer path) {
    int idx = list.indexOf(path);
    if (idx > 0)
      return list.get(idx - 1);
    return null;
  }

  /**
   * Gets next path of a path
   * @param path current path
   * @return the next path
   */
  public FileContainer getNext(FileContainer path) {
    int idx = list.indexOf(path);
    if (idx >= 0 && idx < list.size() - 1)
      return list.get(idx + 1);
    return null;
  }
}
