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

package group.pals.android.lib.ui.filechooser.utils.history;

import java.util.ArrayList;
import java.util.List;

/**
 * A history store of any object.
 * 
 * @author Hai Bison
 * @since v2.0 alpha
 */
public class HistoryStore<A> implements History<A> {

    private List<A> mList = new ArrayList<A>();
    private final int mMaxSize;
    private final List<HistoryListener<A>> mListeners = new ArrayList<HistoryListener<A>>();

    /**
     * Creates new {@link HistoryStore}
     * 
     * @param maxSize
     *            the maximum size that allowed, if it is &lt;= {@code 0},
     *            {@code 11} will be used
     */
    public HistoryStore(int maxSize) {
        this.mMaxSize = maxSize > 0 ? maxSize : 11;
    }

    @Override
    public void push(A currentItem, A newItem) {
        int idx = currentItem == null ? -1 : mList.indexOf(currentItem);
        if (idx < 0 || idx == size() - 1)
            mList.add(newItem);
        else {
            mList = mList.subList(0, idx + 1);
            mList.add(newItem);
        }

        if (mList.size() > mMaxSize)
            mList.remove(0);

        notifyHistoryChanged();
    }// push()

    @Override
    public void remove(A item) {
        if (mList.remove(item))
            notifyHistoryChanged();
    }

    @Override
    public void removeAll(HistoryFilter<A> filter) {
        boolean changed = false;
        for (int i = mList.size() - 1; i >= 0; i--) {
            if (filter.accept(mList.get(i))) {
                mList.remove(i);
                if (!changed)
                    changed = true;
            }
        }// for

        if (changed)
            notifyHistoryChanged();
    }// removeAll()

    @Override
    public void notifyHistoryChanged() {
        for (HistoryListener<A> listener : mListeners)
            listener.onChanged(this);
    }

    @Override
    public int size() {
        return mList.size();
    }

    @Override
    public int indexOf(A a) {
        return mList.indexOf(a);
    }

    @Override
    public A prevOf(A a) {
        int idx = mList.indexOf(a);
        if (idx > 0)
            return mList.get(idx - 1);
        return null;
    }

    @Override
    public A nextOf(A a) {
        int idx = mList.indexOf(a);
        if (idx >= 0 && idx < mList.size() - 1)
            return mList.get(idx + 1);
        return null;
    }

    @Override
    public void addListener(HistoryListener<A> listener) {
        mListeners.add(listener);
    }

    @Override
    public void removeListener(HistoryListener<A> listener) {
        mListeners.remove(listener);
    }
}
