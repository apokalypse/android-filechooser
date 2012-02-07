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
 * A history store of any object.
 * @author Hai Bison
 *
 * @param <A> any type
 * @since v2.0 alpha
 */
public interface History<A> {

  /**
   * Pushes new item to the history.
   * @param currentItem usage: assume we have history of: 1-2-3-4,
   * if current item is 3, and we push 5 to the history, then 4 will be truncated,
   * and new history will be 1-2-3-5
   * @param newItem the new item
   */
  public void push(A currentItem, A newItem);

  /**
   * Gets size of the history
   * @return the size of the history
   */
  public int size();

  /**
   * Gets index of item {@code a} 
   * @param a an item
   * @return index of the {@code a}, or -1 if there is no one
   */
  public int indexOf(A a);

  /**
   * Gets previous item of {@code a}
   * @param a current item
   * @return the previous item, can be {@code null}
   */
  public A prevOf(A a);

  /**
   * Gets next item of {@code a}
   * @param a current item
   * @return the next item, can be {@code null}
   */
  public A nextOf(A a);
}
