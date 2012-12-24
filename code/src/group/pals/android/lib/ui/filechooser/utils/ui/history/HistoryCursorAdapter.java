/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.utils.ui.history;

import group.pals.android.lib.ui.filechooser.BuildConfig;
import group.pals.android.lib.ui.filechooser.R;
import group.pals.android.lib.ui.filechooser.providers.BaseFileProviderUtils;
import group.pals.android.lib.ui.filechooser.providers.ProviderUtils;
import group.pals.android.lib.ui.filechooser.providers.basefile.BaseFileContract.BaseFile;
import group.pals.android.lib.ui.filechooser.providers.history.HistoryContract;
import group.pals.android.lib.ui.filechooser.utils.DateUtils;
import group.pals.android.lib.ui.filechooser.utils.Ui;
import group.pals.android.lib.ui.filechooser.utils.ui.ContextMenuUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

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
 * History cursor adapter.
 * 
 * @author Hai Bison
 * @since v5.1 beta
 * 
 */
public class HistoryCursorAdapter extends ResourceCursorTreeAdapter {

    private static final String _ClassName = HistoryCursorAdapter.class.getName();

    /**
     * @see android.text.format.DateUtils#DAY_IN_MILLIS
     */
    private static final long _DayInMillis = android.text.format.DateUtils.DAY_IN_MILLIS;

    /**
     * Advanced selection options: All, None, Invert.
     */
    public static final Integer[] _AdvancedSelectionOptions = new Integer[] { R.string.afc_cmd_advanced_selection_all,
            R.string.afc_cmd_advanced_selection_none, R.string.afc_cmd_advanced_selection_invert };

    private static class BagGroup {

        TextView mTextViewHeader;
    }// BagGroup

    private static class BagChild {

        TextView mTextViewTime;
        TextView mTextViewName;
        TextView mTextViewPath;
        TextView mTextViewType;
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

    private static final String[] _GroupCursorColumns = { HistoryContract.History._ID,
            HistoryContract.History._ColumnModificationTime, _ColumnOrgGroupPosition };

    private static final String[] _ChildCursorColumns = { HistoryContract.History._ID,
            HistoryContract.History._ColumnUri, HistoryContract.History._ColumnProviderId,
            HistoryContract.History._ColumnModificationTime };

    /**
     * Map of child IDs to {@link BagChildInfo}.
     */
    private final SparseArray<BagChildInfo> mSelectedChildrenMap = new SparseArray<BagChildInfo>();

    private Cursor mOrgCursor;
    private MatrixCursor mGroupCursor;
    private SparseArray<MatrixCursor> mChildrenCursor;
    private CharSequence mSearchText;

    /**
     * Creates new instance.
     * 
     * @param context
     *            {@link Context}.
     */
    public HistoryCursorAdapter(Context context) {
        super(context, null, R.layout.afc_view_history_item, R.layout.afc_view_history_sub_item);
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
        if (cursor != null) {
            long lastDayCount = 0;
            cursor.moveToFirst();
            do {
                long dayCount = (long) Math.floor((Long.parseLong(cursor.getString(cursor
                        .getColumnIndex(HistoryContract.History._ColumnModificationTime))) + TimeZone.getDefault()
                        .getRawOffset())
                        / _DayInMillis);

                if (dayCount != lastDayCount || newGroupCursor.getCount() == 0) {
                    newGroupCursor.addRow(new Object[] {
                            cursor.getInt(cursor.getColumnIndex(HistoryContract.History._ID)),
                            cursor.getString(cursor.getColumnIndex(HistoryContract.History._ColumnModificationTime)),
                            cursor.getPosition() });
                }
                lastDayCount = dayCount;
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
        long startOfDay = Long.parseLong(groupCursor.getString(groupCursor
                .getColumnIndex(HistoryContract.History._ColumnModificationTime)))
                + TimeZone.getDefault().getRawOffset();
        startOfDay -= startOfDay % _DayInMillis;
        do {
            childrenCursor.addRow(new Object[] {
                    mOrgCursor.getInt(mOrgCursor.getColumnIndex(HistoryContract.History._ID)),
                    mOrgCursor.getString(mOrgCursor.getColumnIndex(HistoryContract.History._ColumnUri)),
                    mOrgCursor.getString(mOrgCursor.getColumnIndex(HistoryContract.History._ColumnProviderId)),
                    mOrgCursor.getString(mOrgCursor.getColumnIndex(HistoryContract.History._ColumnModificationTime)) });
        } while (mOrgCursor.moveToNext()
                && Long.parseLong(mOrgCursor.getString(mOrgCursor
                        .getColumnIndex(HistoryContract.History._ColumnModificationTime)))
                        + TimeZone.getDefault().getRawOffset() >= startOfDay);

        /*
         * Put it to the map.
         */
        mChildrenCursor.put(orgGroupPosition, childrenCursor);
        return childrenCursor;
    }// getChildrenCursor()

    @Override
    protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        final int _id = cursor.getInt(cursor.getColumnIndex(HistoryContract.History._ID));
        final BagChild _b;

        if (view.getTag() == null) {
            _b = new BagChild();
            _b.mTextViewTime = (TextView) view.findViewById(R.id.afc_view_history_sub_item_textview_time);
            _b.mTextViewName = (TextView) view.findViewById(R.id.afc_view_history_sub_item_textview_name);
            _b.mTextViewPath = (TextView) view.findViewById(R.id.afc_view_history_sub_item_textview_path);
            _b.mTextViewType = (TextView) view.findViewById(R.id.afc_view_history_sub_item_textview_type);
            _b.mCheckBox = (CheckBox) view.findViewById(R.id.afc_view_history_sub_item_checkbox);

            view.setTag(_b);
        } else
            _b = (BagChild) view.getTag();

        final BagChildInfo _bci;
        if (mSelectedChildrenMap.get(_id) == null) {
            _bci = new BagChildInfo();
            mSelectedChildrenMap.put(_id, _bci);
        } else
            _bci = mSelectedChildrenMap.get(_id);

        Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(HistoryContract.History._ColumnUri)));

        String fileName = null;
        String filePath = null;
        Cursor fileInfo = context.getContentResolver().query(uri, null, null, null, null);
        try {
            if (fileInfo != null && fileInfo.moveToFirst()) {
                fileName = fileInfo.getString(fileInfo.getColumnIndex(BaseFile._ColumnName));
                filePath = fileInfo.getString(fileInfo.getColumnIndex(BaseFile._ColumnPath));
            }
        } finally {
            if (fileInfo != null)
                fileInfo.close();
        }

        _b.mTextViewTime.setText(formatTime(view.getContext(), Long.parseLong(cursor.getString(cursor
                .getColumnIndex(HistoryContract.History._ColumnModificationTime)))));
        _b.mTextViewName.setText(fileName);
        Ui.strikeOutText(_b.mTextViewName, _bci.mMarkedAsDeleted);
        _b.mTextViewPath.setText(filePath);

        /*
         * Provider name.
         */
        String providerId = cursor.getString(cursor.getColumnIndex(HistoryContract.History._ColumnProviderId));
        if (ProviderUtils.getProviderName(providerId) == null)
            ProviderUtils.setProviderName(providerId, BaseFileProviderUtils.getProviderName(context, providerId));
        _b.mTextViewType.setText(ProviderUtils.getProviderName(providerId));

        /*
         * Check box.
         */
        _b.mCheckBox.setOnCheckedChangeListener(null);
        _b.mCheckBox.setChecked(_bci.mChecked);
        _b.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (BuildConfig.DEBUG)
                    Log.d(_ClassName, "onCheckedChanged() >> _id = " + _id);
                _bci.mChecked = isChecked;
            }// onCheckedChanged()
        });

        _b.mCheckBox.setOnLongClickListener(new View.OnLongClickListener() {

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
            b.mTextViewHeader = (TextView) view.findViewById(R.id.afc_view_history_item_textview_header);

            view.setTag(b);
        } else
            b = (BagGroup) view.getTag();

        b.mTextViewHeader.setText(formatDate(view.getContext(), Long.parseLong(cursor.getString(cursor
                .getColumnIndex(HistoryContract.History._ColumnModificationTime)))));
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
     * Gets the search text.
     * 
     * @return the search text, can be {@code null}.
     */
    public CharSequence getSearchText() {
        return mSearchText;
    }// getSearchText()

    /**
     * Sets search text.
     * 
     * @param searchText
     *            the search text.
     */
    public void setSearchText(CharSequence searchText) {
        mSearchText = searchText;
    }// setSearchText()

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
            final int _id = cursor.getInt(cursor.getColumnIndex(HistoryContract.History._ID));
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
            final int _id = cursor.getInt(cursor.getColumnIndex(HistoryContract.History._ID));
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

    /*
     * STATIC UTILITIES
     */

    /**
     * Formats {@code millis} to time.
     * 
     * @param c
     *            {@link Context}.
     * @param millis
     *            the time in milliseconds.
     * @return the formatted time.
     */
    private static String formatDate(Context c, long millis) {
        if (android.text.format.DateUtils.isToday(millis))
            return c.getString(R.string.afc_today);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);

        final Calendar _yesterday = Calendar.getInstance();
        _yesterday.add(Calendar.DAY_OF_YEAR, -1);

        if (cal.get(Calendar.YEAR) == _yesterday.get(Calendar.YEAR)) {
            if (cal.get(Calendar.DAY_OF_YEAR) == _yesterday.get(Calendar.DAY_OF_YEAR))
                return c.getString(R.string.afc_yesterday);
            else
                return android.text.format.DateUtils.formatDateTime(c, millis, DateUtils._FormatMonthAndDay);
        }

        return android.text.format.DateUtils.formatDateTime(c, millis, DateUtils._FormatMonthAndDay
                | DateUtils._FormatYear);
    }// formatDate()

    /**
     * Formats {@code millis} to short time. E.g: "10:01am".
     * 
     * @param c
     *            {@link Context}.
     * @param millis
     *            time in milliseconds.
     * @return the formatted time.
     */
    private static String formatTime(Context c, long millis) {
        return android.text.format.DateUtils.formatDateTime(c, millis, DateUtils._FormatShortTime);
    }// formatTime()
}
