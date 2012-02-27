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

import java.util.ArrayList;
import java.util.List;

/**
 * A history store of any object.
 * 
 * @author Hai Bison
 * @since v2.0 alpha
 */
public class HistoryStore<A> implements History<A> {

    private List<A> list = new ArrayList<A>();
    private final int fMaxSize;

    /**
     * Creates new {@link HistoryStore}
     * 
     * @param maxSize
     *            the maximum size that allowed, if it is &lt;= {@code 0},
     *            {@code 11} will be used
     */
    public HistoryStore(int maxSize) {
        this.fMaxSize = maxSize > 0 ? maxSize : 11;
    }

    @Override
    public void push(A currentItem, A newItem) {
        int idx = currentItem == null ? -1 : list.indexOf(currentItem);
        if (idx < 0 || idx == size() - 1)
            list.add(newItem);
        else {
            list = list.subList(0, idx + 1);
            list.add(newItem);
        }

        if (list.size() > fMaxSize)
            list.remove(0);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public int indexOf(A a) {
        return list.indexOf(a);
    }

    @Override
    public A prevOf(A a) {
        int idx = list.indexOf(a);
        if (idx > 0)
            return list.get(idx - 1);
        return null;
    }

    @Override
    public A nextOf(A a) {
        int idx = list.indexOf(a);
        if (idx >= 0 && idx < list.size() - 1)
            return list.get(idx + 1);
        return null;
    }
}
