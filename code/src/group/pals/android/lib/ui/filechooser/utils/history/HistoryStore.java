/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.utils.history;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * A history store of any object extending {@link Parcelable}.<br>
 * <b>Note:</b> This class does not support storing its {@link HistoryListener}
 * 's into {@link Parcelable}. You must re-build all listeners after getting
 * your {@link HistoryStore} from a {@link Bundle} for example.
 * 
 * @author Hai Bison
 * @since v2.0 alpha
 */
public class HistoryStore<A extends Parcelable> implements History<A> {

    /**
     * Uses for debugging...
     */
    private static final String _ClassName = HistoryStore.class.getName();

    /**
     * The default capacity of this store.
     */
    public static final int _DefaultCapacity = 100;

    private final ArrayList<A> mHistoryList = new ArrayList<A>();
    private final List<HistoryListener<A>> mListeners = new ArrayList<HistoryListener<A>>();
    private int mCapacity;

    /**
     * Creates new instance with {@link #_DefaultCapacity}.
     */
    public HistoryStore() {
        this(_DefaultCapacity);
    }// HistoryStore()

    /**
     * Creates new {@link HistoryStore}
     * 
     * @param capcacity
     *            the maximum size that allowed, if it is {@code <= 0},
     *            {@link #_DefaultCapacity} will be used
     */
    public HistoryStore(int capcacity) {
        mCapacity = capcacity > 0 ? capcacity : _DefaultCapacity;
    }// HistoryStore()

    /**
     * Gets the capacity.
     * 
     * @return the capacity.
     */
    public int getCapacity() {
        return mCapacity;
    }// getCapacity()

    @Override
    public void push(A newItem) {
        if (newItem == null)
            return;

        if (!mHistoryList.isEmpty() && mHistoryList.indexOf(newItem) == mHistoryList.size() - 1)
            return;

        mHistoryList.add(newItem);
        if (mHistoryList.size() > mCapacity)
            mHistoryList.remove(0);

        notifyHistoryChanged();
    }// push()

    @Override
    public void truncateAfter(A item) {
        if (item == null)
            return;

        int idx = mHistoryList.indexOf(item);
        if (idx >= 0 && idx < mHistoryList.size() - 1) {
            mHistoryList.subList(idx + 1, mHistoryList.size()).clear();
            notifyHistoryChanged();
        }
    }// truncateAfter()

    @Override
    public void remove(A item) {
        if (mHistoryList.remove(item))
            notifyHistoryChanged();
    }// remove()

    @Override
    public void removeAll(HistoryFilter<A> filter) {
        boolean changed = false;
        for (int i = mHistoryList.size() - 1; i >= 0; i--) {
            if (filter.accept(mHistoryList.get(i))) {
                mHistoryList.remove(i);
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
    }// notifyHistoryChanged()

    @Override
    public int size() {
        return mHistoryList.size();
    }// size()

    @Override
    public int indexOf(A a) {
        return mHistoryList.indexOf(a);
    }// indexOf()

    @Override
    public A prevOf(A a) {
        int idx = mHistoryList.indexOf(a);
        if (idx > 0)
            return mHistoryList.get(idx - 1);
        return null;
    }// prevOf()

    @Override
    public A nextOf(A a) {
        int idx = mHistoryList.indexOf(a);
        if (idx >= 0 && idx < mHistoryList.size() - 1)
            return mHistoryList.get(idx + 1);
        return null;
    }// nextOf()

    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<A> items() {
        return (ArrayList<A>) mHistoryList.clone();
    }// items()

    @Override
    public boolean isEmpty() {
        return mHistoryList.isEmpty();
    }// isEmpty()

    @Override
    public void clear() {
        mHistoryList.clear();
        notifyHistoryChanged();
    }// clear()

    @Override
    public void addListener(HistoryListener<A> listener) {
        mListeners.add(listener);
    }// addListener()

    @Override
    public void removeListener(HistoryListener<A> listener) {
        mListeners.remove(listener);
    }// removeListener()

    /*-----------------------------------------------------
     * Parcelable
     */

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }// describeContents()

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mCapacity);

        dest.writeInt(size());
        for (int i = 0; i < size(); i++)
            dest.writeParcelable(mHistoryList.get(i), flags);
    }// writeToParcel()

    /**
     * Reads data from {@code in}.
     * 
     * @param in
     *            {@link Parcel}.
     */

    @SuppressWarnings("unchecked")
    public void readFromParcel(Parcel in) {
        mCapacity = in.readInt();

        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            try {
                mHistoryList.add((A) in.readParcelable(null));
            } catch (ClassCastException e) {
                Log.e(_ClassName, "readFromParcel() >> " + e);
                e.printStackTrace();
                break;
            }
        }
    }// readFromParcel()

    @SuppressWarnings("rawtypes")
    public static final Parcelable.Creator<HistoryStore> CREATOR = new Parcelable.Creator<HistoryStore>() {

        public HistoryStore createFromParcel(Parcel in) {
            return new HistoryStore(in);
        }// createFromParcel()

        public HistoryStore[] newArray(int size) {
            return new HistoryStore[size];
        }// newArray()
    };// CREATOR

    private HistoryStore(Parcel in) {
        readFromParcel(in);
    }// HistoryStore()
}
