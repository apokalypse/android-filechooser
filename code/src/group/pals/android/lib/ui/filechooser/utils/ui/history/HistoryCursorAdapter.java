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
import group.pals.android.lib.ui.filechooser.providers.DbUtils;
import group.pals.android.lib.ui.filechooser.providers.history.HistoryContract;
import group.pals.android.lib.ui.filechooser.utils.DateUtils;
import group.pals.android.lib.ui.filechooser.utils.Ui;
import group.pals.android.lib.ui.filechooser.utils.ui.ContextMenuUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import android.content.AsyncQueryHandler;
import android.content.Context;
import android.database.Cursor;
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

    private final class QueryHandler extends AsyncQueryHandler {

        public QueryHandler(Context context) {
            super(context.getContentResolver());
        }// QueryHandler()

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            try {
                int groupPosition = (Integer) cookie;
                HistoryCursorAdapter.this.setChildrenCursor(groupPosition, cursor);
            } catch (NullPointerException e) {
                Log.e(_ClassName, "onQueryComplete() >> " + e);
                e.printStackTrace();
            }
        }// onQueryComplete()
    }// QueryHandler

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

    private final QueryHandler mQueryHandler;
    /**
     * Map of child IDs to {@link BagChildInfo}.
     */
    private final SparseArray<BagChildInfo> mSelectedChildrenMap = new SparseArray<BagChildInfo>();
    /**
     * Map of provider IDs to their names, to avoid of querying multiple times
     * for a same ID.
     */
    private final Map<String, String> mMapProviderName = new HashMap<String, String>();

    private CharSequence mSearchText;

    /**
     * Creates new instance.
     * 
     * @param context
     *            {@link Context}.
     */
    public HistoryCursorAdapter(Context context) {
        super(context, null, R.layout.afc_view_history_item, R.layout.afc_view_history_sub_item);
        mQueryHandler = new QueryHandler(context);
    }// BookmarkCursorAdapter()

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

        _b.mTextViewTime.setText(formatTime(view.getContext(), Long.parseLong(cursor.getString(cursor
                .getColumnIndex(HistoryContract.History._ColumnModificationTime)))));
        _b.mTextViewName.setText(uri.getLastPathSegment());
        Ui.strikeOutText(_b.mTextViewName, _bci.mMarkedAsDeleted);
        _b.mTextViewPath.setText(uri.getPath());

        /*
         * Provider name.
         */
        String providerId = cursor.getString(cursor.getColumnIndex(HistoryContract.History._ColumnProviderId));
        if (mMapProviderName.get(providerId) == null)
            mMapProviderName.put(providerId, BaseFileProviderUtils.getProviderName(context, providerId));
        _b.mTextViewType.setText(mMapProviderName.get(providerId));

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

        b.mTextViewHeader.setText(formatDate(view.getContext(),
                Long.parseLong(cursor.getString(cursor.getColumnIndex(HistoryContract.History._ColumnDaysOfGroup)))
                        * _DayInMillis));
    }// bindGroupView()

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        if (BuildConfig.DEBUG)
            Log.d(_ClassName, "getChildrenCursor()");

        /*
         * order by %s desc limit %d offset %d
         */
        int maxChildCount = groupCursor.getInt(groupCursor.getColumnIndex(HistoryContract.History._COUNT));
        long date = groupCursor.getLong(groupCursor.getColumnIndex(HistoryContract.History._ColumnDaysOfGroup))
                * _DayInMillis - TimeZone.getDefault().getRawOffset();
        if (BuildConfig.DEBUG) {
            Log.d(_ClassName, String.format("StartDateOfGroup #%s= %s [%,d]", groupCursor.getPosition(),
                    new java.util.Date(date), date));
            Log.d(_ClassName, "max child count = " + maxChildCount);
        }

        String finalWhere = "";
        if (getSearchText() != null) {
            finalWhere = DbUtils.rawSqlEscapeString(Uri.encode(getSearchText().toString()));
            finalWhere = String.format(" AND %s LIKE '%%://%%%s%%'", HistoryContract.History._ColumnUri, finalWhere);
            if (BuildConfig.DEBUG)
                Log.d(_ClassName, "finalWhere = " + finalWhere);
        }
        if (BuildConfig.DEBUG)
            Log.d(_ClassName, "FILTER = " + finalWhere);

        mQueryHandler.startQuery(
                0,
                groupCursor.getPosition(),
                HistoryContract.History._ContentUri,
                null,
                HistoryContract.History._ColumnModificationTime + " >= '" + DbUtils.formatNumber(date) + "' AND "
                        + HistoryContract.History._ColumnModificationTime + " < '"
                        + DbUtils.formatNumber(date + _DayInMillis) + "'" + finalWhere, null,
                HistoryContract.History._ColumnModificationTime + " desc limit " + maxChildCount);

        return null;
    }// getChildrenCursor()

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
