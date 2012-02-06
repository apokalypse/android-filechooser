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

import group.pals.android.lib.ui.filechooser.bean.FileContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used as a history store of {@link FileContainer}
 * @author Hai Bison
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

    if (list.size() > MaxSize) list.remove(0);
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
