/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.utils.ui.bookmark;

import group.pals.android.lib.ui.filechooser.BuildConfig;
import group.pals.android.lib.ui.filechooser.R;
import group.pals.android.lib.ui.filechooser.providers.BaseFileProviderUtils;
import group.pals.android.lib.ui.filechooser.providers.ProviderUtils;
import group.pals.android.lib.ui.filechooser.providers.bookmark.BookmarkContract.Bookmark;
import group.pals.android.lib.ui.filechooser.utils.Ui;
import group.pals.android.lib.ui.filechooser.utils.ui.ContextMenuUtils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ResourceCursorTreeAdapter;
import android.widget.TextView;

/**
 * Bookmark cursor adapter.
 * 
 * @author Hai Bison
 * @since v5.1 beta
 * 
 */
public class BookmarkCursorAdapter extends ResourceCursorTreeAdapter {

    private static final String _ClassName = BookmarkCursorAdapter.class.getName();

    /**
     * Advanced selection options: All, None, Invert.
     */
    public static final Integer[] _AdvancedSelectionOptions = new Integer[] { R.string.afc_cmd_advanced_selection_all,
            R.string.afc_cmd_advanced_selection_none, R.string.afc_cmd_advanced_selection_invert };

    /**
     * The "view holder".
     * 
     * @author Hai Bison
     * 
     */
    private static class BagGroup {

        TextView mTextHeader;
    }// BagGroup

    /**
     * The "view holder".
     * 
     * @author Hai Bison
     * 
     */
    private static class BagChild {

        TextView mTextName;
        TextView mTextPath;
        CheckBox mCheckBox;
    }// BagChild

    private static class BagChildInfo {

        boolean mChecked = false;
        boolean mMarkedAsDeleted = false;
    }// BagChildInfo

    /**
     * This column holds the original position of group cursor in original
     * cursor.
     * <p>
     * Type: {@code Integer}
     * </p>
     */
    private static final String _ColumnOrgGroupPosition = "org_group_position";

    private static final String[] _GroupCursorColumns = { Bookmark._ID, Bookmark._ColumnProviderId,
            _ColumnOrgGroupPosition };

    private static final String[] _ChildCursorColumns = { Bookmark._ID, Bookmark._ColumnName, Bookmark._ColumnUri,
            Bookmark._ColumnProviderId, Bookmark._ColumnModificationTime };

    /**
     * Map of child IDs to {@link BagChildInfo}.
     */
    private final SparseArray<BagChildInfo> mSelectedChildrenMap = new SparseArray<BagChildInfo>();
    private boolean mEditor;

    private Cursor mOrgCursor;
    private MatrixCursor mGroupCursor;
    private SparseArray<MatrixCursor> mChildrenCursor;

    /**
     * Creates new instance.
     * 
     * @param context
     *            {@link Context}.
     */
    public BookmarkCursorAdapter(Context context) {
        super(context, null, R.layout.afc_view_bookmark_item, R.layout.afc_view_bookmark_sub_item);
    }// BookmarkCursorAdapter()

    /**
     * Changes new cursor.
     * <p>
     * You have to query the items in descending order of modification time.
     * </p>
     * 
     * @param cursor
     *            the cursor.
     * @param notificationUri
     *            the notification URI.
     */
    @Override
    public synchronized void changeCursor(Cursor cursor) {
        if (BuildConfig.DEBUG)
            Log.d(_ClassName, "changeCursor()");

        if (mOrgCursor != null)
            mOrgCursor.close();
        mOrgCursor = cursor;

        MatrixCursor newGroupCursor = cursor != null ? new MatrixCursor(_GroupCursorColumns) : null;
        SparseArray<MatrixCursor> newChildrenCursor = cursor != null ? new SparseArray<MatrixCursor>() : null;

        /*
         * Build new group cursor.
         */
        if (cursor != null && cursor.moveToFirst()) {
            String lastProviderId = null;
            do {
                String providerId = cursor.getString(cursor.getColumnIndex(Bookmark._ColumnProviderId));

                if (!providerId.equals(lastProviderId)) {
                    newGroupCursor
                            .addRow(new Object[] { cursor.getInt(cursor.getColumnIndex(Bookmark._ID)),
                                    cursor.getString(cursor.getColumnIndex(Bookmark._ColumnProviderId)),
                                    cursor.getPosition() });
                }
                lastProviderId = providerId;
            } while (cursor.moveToNext());
        }

        /*
         * Clean up children cursor.
         */
        if (mChildrenCursor != null) {
            for (int i = 0; i < mChildrenCursor.size(); i++)
                mChildrenCursor.valueAt(i).close();
            mChildrenCursor.clear();
        }

        /*
         * Apply new changes... Note that we don't need to close the old group
         * cursor. The call to `super.changeCursor()` will do that.
         */
        mGroupCursor = newGroupCursor;
        mChildrenCursor = newChildrenCursor;
        super.changeCursor(mGroupCursor);
    }// changeCursor()

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        if (BuildConfig.DEBUG)
            Log.d(_ClassName, "getChildrenCursor()");

        /*
         * Try to find the child cursor in the map. If found then it'd be great
         * :-)
         */
        int orgGroupPosition = groupCursor.getInt(groupCursor.getColumnIndex(_ColumnOrgGroupPosition));
        int idx = mChildrenCursor.indexOfKey(orgGroupPosition);
        if (idx >= 0)
            return mChildrenCursor.valueAt(idx);

        /*
         * If not found, create new cursor.
         */
        MatrixCursor childrenCursor = new MatrixCursor(_ChildCursorColumns);

        mOrgCursor.moveToPosition(orgGroupPosition);
        String providerId = groupCursor.getString(groupCursor.getColumnIndex(Bookmark._ColumnProviderId));
        do {
            childrenCursor.addRow(new Object[] { mOrgCursor.getInt(mOrgCursor.getColumnIndex(Bookmark._ID)),
                    mOrgCursor.getString(mOrgCursor.getColumnIndex(Bookmark._ColumnName)),
                    mOrgCursor.getString(mOrgCursor.getColumnIndex(Bookmark._ColumnUri)),
                    mOrgCursor.getString(mOrgCursor.getColumnIndex(Bookmark._ColumnProviderId)),
                    mOrgCursor.getString(mOrgCursor.getColumnIndex(Bookmark._ColumnModificationTime)) });
        } while (mOrgCursor.moveToNext()
                && mOrgCursor.getString(mOrgCursor.getColumnIndex(Bookmark._ColumnProviderId)).equals(providerId));

        /*
         * Put it to the map.
         */
        mChildrenCursor.put(orgGroupPosition, childrenCursor);
        return childrenCursor;
    }// getChildrenCursor()

    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        final int _id = cursor.getInt(cursor.getColumnIndex(Bookmark._ID));
        Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(Bookmark._ColumnUri)));

        /*
         * Child Info
         */
        final BagChildInfo _bci;
        if (mSelectedChildrenMap.get(_id) == null) {
            _bci = new BagChildInfo();
            mSelectedChildrenMap.put(_id, _bci);
        } else
            _bci = mSelectedChildrenMap.get(_id);

        /*
         * Child
         */
        BagChild bag = (BagChild) view.getTag();

        if (bag == null) {
            bag = new BagChild();
            bag.mTextName = (TextView) view.findViewById(R.id.afc_view_bookmark_sub_item_text_name);
            bag.mTextPath = (TextView) view.findViewById(R.id.afc_view_bookmark_sub_item_text_path);
            bag.mCheckBox = (CheckBox) view.findViewById(R.id.afc_view_bookmark_sub_item_checkbox);

            view.setTag(bag);
        }

        /*
         * Name.
         */

        bag.mTextName.setText(cursor.getString(cursor.getColumnIndex(Bookmark._ColumnName)));
        Ui.strikeOutText(bag.mTextName, _bci.mMarkedAsDeleted);

        /*
         * Path.
         */

        if (isEditor()) {
            bag.mTextPath.setVisibility(View.VISIBLE);
            bag.mTextPath.setText(BaseFileProviderUtils.getFilePath(context, uri));
        } else
            bag.mTextPath.setVisibility(View.GONE);

        /*
         * Checkbox.
         */

        bag.mCheckBox.setVisibility(isEditor() ? View.VISIBLE : View.GONE);
        bag.mCheckBox.setOnCheckedChangeListener(null);
        bag.mCheckBox.setChecked(_bci.mChecked);
        bag.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                _bci.mChecked = isChecked;
            }// onCheckedChanged()
        });

        bag.mCheckBox.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                ContextMenuUtils.showContextMenu(v.getContext(), 0, R.string.afc_title_advanced_selection,
                        _AdvancedSelectionOptions, new ContextMenuUtils.OnMenuItemClickListener() {

                            @Override
                            public void onClick(final int resId) {
                                if (resId == R.string.afc_cmd_advanced_selection_all)
                                    selectAll(true);
                                else if (resId == R.string.afc_cmd_advanced_selection_none)
                                    selectAll(false);
                                else if (resId == R.string.afc_cmd_advanced_selection_invert)
                                    invertSelection();
                            }// onClick()
                        });
                return true;
            }// onLongClick()
        });
    }// bindChildView()

    @Override
    protected void bindGroupView(View view, Context context, final Cursor cursor, boolean isExpanded) {
        BagGroup b;
        if (view.getTag() == null) {
            b = new BagGroup();
            b.mTextHeader = (TextView) view.findViewById(R.id.afc_view_bookmark_item_textview_header);

            view.setTag(b);
        } else
            b = (BagGroup) view.getTag();

        /*
         * Provider name.
         */
        String providerId = cursor.getString(cursor.getColumnIndex(Bookmark._ColumnProviderId));
        if (ProviderUtils.getProviderName(providerId) == null)
            ProviderUtils.setProviderName(providerId, BaseFileProviderUtils.getProviderName(context, providerId));
        b.mTextHeader.setText(ProviderUtils.getProviderName(providerId));
    }// bindGroupView()

    @Override
    public void notifyDataSetChanged(boolean releaseCursors) {
        super.notifyDataSetChanged(releaseCursors);
        if (releaseCursors)
            synchronized (mSelectedChildrenMap) {
                mSelectedChildrenMap.clear();
            }
    }// notifyDataSetChanged()

    @Override
    public void notifyDataSetInvalidated() {
        super.notifyDataSetInvalidated();
        synchronized (mSelectedChildrenMap) {
            mSelectedChildrenMap.clear();
        }
    }// notifyDataSetInvalidated()

    /*
     * UTILITIES
     */

    /**
     * Checks if this is in editor mode.
     * 
     * @return {@code true} or {@code false}.
     */
    public boolean isEditor() {
        return mEditor;
    }// isEditor()

    /**
     * Sets editor mode.<br>
     * <b>Note:</b> This calls {@link #notifyDataSetChanged(boolean)} (with
     * {@code false}) after done.
     * 
     * @param v
     *            {@code true} or {@code false}.
     */
    public void setEditor(boolean v) {
        if (mEditor != v) {
            mEditor = v;
            notifyDataSetChanged(false);
        }
    }// setEditor()

    /**
     * Selects all items in a specified group.<br>
     * <b>Note:</b> This will <i>not</i> notify data set for changes after done.
     * 
     * @param groupPosition
     *            the group position.
     * @param selected
     *            {@code true} or {@code false}.
     */
    private void asyncSelectAll(int groupPosition, boolean selected) {
        int chidrenCount = getChildrenCount(groupPosition);
        for (int iChild = 0; iChild < chidrenCount; iChild++) {
            Cursor cursor = getChild(groupPosition, iChild);
            final int _id = cursor.getInt(cursor.getColumnIndex(Bookmark._ID));
            BagChildInfo b = mSelectedChildrenMap.get(_id);
            if (b == null) {
                b = new BagChildInfo();
                mSelectedChildrenMap.put(_id, b);
            }
            b.mChecked = selected;
        }// for children
    }// asyncSelectAll()

    /**
     * Selects all items of a specified group.<br>
     * <b>Note:</b> This calls {@link #notifyDataSetChanged(boolean)} (with
     * {@code false}) after done.
     * 
     * @param groupPosition
     *            the group position.
     * @param selected
     *            {@code true} or {@code false}.
     */
    public synchronized void selectAll(int groupPosition, boolean selected) {
        asyncSelectAll(groupPosition, selected);
        notifyDataSetChanged(false);
    }// selectAll()

    /**
     * Selects all items.<br>
     * <b>Note:</b> This calls {@link #notifyDataSetChanged(boolean)} (with
     * {@code false}) after done.
     * 
     * @param selected
     *            {@code true} or {@code false}.
     */
    public synchronized void selectAll(boolean selected) {
        for (int iGroup = 0; iGroup < getGroupCount(); iGroup++)
            asyncSelectAll(iGroup, selected);
        notifyDataSetChanged(false);
    }// selectAll()

    /**
     * Inverts selection.<br>
     * <b>Note:</b> This will <i>not</i> notify data set for changes after done.
     * 
     * @param groupPosition
     *            the group position.
     */
    private void asyncInvertSelection(int groupPosition) {
        int chidrenCount = getChildrenCount(groupPosition);
        for (int iChild = 0; iChild < chidrenCount; iChild++) {
            Cursor cursor = getChild(groupPosition, iChild);
            final int _id = cursor.getInt(cursor.getColumnIndex(Bookmark._ID));
            BagChildInfo b = mSelectedChildrenMap.get(_id);
            if (b == null) {
                b = new BagChildInfo();
                mSelectedChildrenMap.put(_id, b);
            }
            b.mChecked = !b.mChecked;
        }// for children
    }// asyncInvertSelection()

    /**
     * Inverts selection of all items of a specified group.<br>
     * <b>Note:</b> This calls {@link #notifyDataSetChanged(boolean)} (with
     * {@code false}) after done.
     * 
     * @param groupPosition
     *            the group position.
     */
    public synchronized void invertSelection(int groupPosition) {
        asyncInvertSelection(groupPosition);
        notifyDataSetChanged(false);
    }// invertSelection()

    /**
     * Inverts selection of all items.<br>
     * <b>Note:</b> This calls {@link #notifyDataSetChanged(boolean)} (with
     * {@code false}) after done.
     */
    public synchronized void invertSelection() {
        for (int iGroup = 0; iGroup < getGroupCount(); iGroup++)
            asyncInvertSelection(iGroup);
        notifyDataSetChanged(false);
    }// invertSelection()

    /**
     * Checks if item with {@code id} (the database ID) is selected or not.
     * 
     * @param id
     *            the database ID.
     * @return {@code true} or {@code false}.
     */
    public boolean isSelected(int id) {
        synchronized (mSelectedChildrenMap) {
            return mSelectedChildrenMap.get(id) != null ? mSelectedChildrenMap.get(id).mChecked : false;
        }
    }// isSelected()

    /**
     * Gets IDs of selected items.
     * 
     * @return list of IDs, can be empty.
     */
    public List<Integer> getSelectedItemIds() {
        List<Integer> res = new ArrayList<Integer>();

        synchronized (mSelectedChildrenMap) {
            for (int i = 0; i < mSelectedChildrenMap.size(); i++)
                if (mSelectedChildrenMap.get(mSelectedChildrenMap.keyAt(i)).mChecked)
                    res.add(mSelectedChildrenMap.keyAt(i));
        }

        return res;
    }// getSelectedItemIds()

    /**
     * Marks all selected items as deleted.<br>
     * <b>Note:</b> This calls {@link #notifyDataSetChanged()} after done.
     * 
     * @param deleted
     *            {@code true} or {@code false}.
     */
    public void markSelectedItemsAsDeleted(boolean deleted) {
        synchronized (mSelectedChildrenMap) {
            for (int i = 0; i < mSelectedChildrenMap.size(); i++)
                if (mSelectedChildrenMap.get(mSelectedChildrenMap.keyAt(i)).mChecked)
                    mSelectedChildrenMap.get(mSelectedChildrenMap.keyAt(i)).mMarkedAsDeleted = deleted;
        }

        notifyDataSetChanged(false);
    }// markSelectedItemsAsDeleted()

    /**
     * Marks specified item as deleted.<br>
     * <b>Note:</b> This calls {@link #notifyDataSetChanged()} after done.
     * 
     * @param id
     *            the database ID of the item.
     * @param deleted
     *            {@code true} or {@code false}.
     */
    public void markItemAsDeleted(int id, boolean deleted) {
        synchronized (mSelectedChildrenMap) {
            if (mSelectedChildrenMap.get(id) != null) {
                mSelectedChildrenMap.get(id).mMarkedAsDeleted = deleted;
                notifyDataSetChanged(false);
            }
        }
    }// markItemAsDeleted()
}
