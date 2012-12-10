/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.utils.ui.bookmark;

import group.pals.android.lib.ui.filechooser.BuildConfig;
import group.pals.android.lib.ui.filechooser.R;
import group.pals.android.lib.ui.filechooser.prefs.DisplayPrefs;
import group.pals.android.lib.ui.filechooser.providers.DbUtils;
import group.pals.android.lib.ui.filechooser.providers.bookmark.BookmarkContract;
import group.pals.android.lib.ui.filechooser.providers.bookmark.BookmarkContract.Bookmark;
import group.pals.android.lib.ui.filechooser.utils.TextUtils;
import group.pals.android.lib.ui.filechooser.utils.ui.ContextMenuUtils;
import group.pals.android.lib.ui.filechooser.utils.ui.Dlg;
import group.pals.android.lib.ui.filechooser.utils.ui.GestureUtils;
import group.pals.android.lib.ui.filechooser.utils.ui.GestureUtils.FlingDirection;
import group.pals.android.lib.ui.filechooser.utils.ui.TaskListener;
import group.pals.android.lib.ui.filechooser.utils.ui.history.HistoryFragment;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ExpandableListView;

/**
 * Fragment to manage bookmarks.
 * 
 * @since v5.1 beta
 * @author Hai Bison
 * 
 */
public class BookmarkFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Used for debugging or something...
     */
    private static final String _ClassName = BookmarkFragment.class.getName();

    private static final String _ModeEditor = _ClassName + ".mode_editor";

    private static final int _LoaderHistoryData = 0;
    private static final int _LoaderHistoryCounter = 1;

    /**
     * Creates a new instance of {@link HistoryFragment}.
     * 
     * @param editor
     *            {@code true} if you want to use this as an editor, and
     *            {@code false} as a viewer.
     * @return {@link BookmarkFragment}.
     */
    public static BookmarkFragment newInstance(boolean editor) {
        Bundle args = new Bundle();
        args.putBoolean(_ModeEditor, editor);

        BookmarkFragment res = new BookmarkFragment();
        res.setArguments(args);

        return res;
    }// newInstance()

    /*
     * Controls.
     */

    private View mViewGroupControls;
    private ExpandableListView mListView;
    private ViewGroup mViewFooter;
    private Button mBtnClear;
    private Button mBtnOk;
    private View mViewLoading;

    /*
     * Fields.
     */

    private final Handler mHandler = new Handler();
    private boolean mEditor = false;
    private BookmarkCursorAdapter mBookmarkCursorAdapter;
    private Cursor mCursorCounter;
    /**
     * Initializes to {@code -1} to avoid of re-loading the first load.
     */
    private int mItemCount = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEditor = getArguments().getBoolean(_ModeEditor);
    }// onCreate()

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG)
            Log.d(_ClassName, "onCreateDialog()");
        return Dlg.newDlgBuilder(getActivity()).setIcon(R.drawable.afc_bookmarks_dark)
                .setTitle(R.string.afc_title_bookmark_manager)
                .setView(initContentView(getActivity().getLayoutInflater(), null)).create();
    }// onCreateDialog()

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (BuildConfig.DEBUG)
            Log.d(_ClassName, "onCreateView()");
        if (getDialog() != null) {
            getDialog().setCanceledOnTouchOutside(true);
            getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {

                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    /*
                     * Don't let the Search key dismiss this dialog.
                     */
                    return keyCode == KeyEvent.KEYCODE_SEARCH;
                }// onKey()
            });

            return null;
        }

        return initContentView(inflater, container);
    }// onCreateView()

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /*
         * Prepare the loader. Either re-connect with an existing one, or start
         * a new one.
         */
        getLoaderManager().initLoader(_LoaderHistoryData, null, this);
        getLoaderManager().initLoader(_LoaderHistoryCounter, null, this);
    }// onActivityCreated()

    /*
     * LOADERMANAGER.LOADERCALLBACKS
     */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (BuildConfig.DEBUG)
            Log.d(_ClassName, "onCreateLoader()");
        switch (id) {
        case _LoaderHistoryCounter:
            return new CursorLoader(getActivity(), BookmarkContract.Bookmark._ContentUri,
                    new String[] { BookmarkContract.Bookmark._COUNT }, null, null, null);
            // _LoaderHistoryCounter

        case _LoaderHistoryData:
            mHandler.removeCallbacksAndMessages(null);
            mHandler.postDelayed(mViewLoadingShower, DisplayPrefs._DelayTimeForSimpleAnimation);

            mBookmarkCursorAdapter.changeCursor(null);

            return new CursorLoader(getActivity(), BookmarkContract.Bookmark._ContentUriGroupBySameProvider, null,
                    null, null, null);
            // _LoaderHistoryData
        }

        return null;
    }// onCreateLoader()

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (BuildConfig.DEBUG)
            Log.d(_ClassName, "onLoadFinished() -- data = " + data);

        switch (loader.getId()) {
        case _LoaderHistoryCounter:
            if (mCursorCounter != null)
                mCursorCounter.close();

            mCursorCounter = data;
            if (mCursorCounter.moveToFirst()) {
                int newItemCount = mCursorCounter.getInt(mCursorCounter
                        .getColumnIndex(BookmarkContract.Bookmark._COUNT));
                if (mItemCount >= 0 && newItemCount != mItemCount)
                    getLoaderManager().restartLoader(_LoaderHistoryData, null, this);
                mItemCount = newItemCount;
            } else {
                mItemCount = 0;
            }

            updateUI();

            break;// _LoaderHistoryCounter

        case _LoaderHistoryData:
            mBookmarkCursorAdapter.setGroupCursor(data);

            for (int i = 0; i < mBookmarkCursorAdapter.getGroupCount(); i++)
                mListView.expandGroup(i);

            /*
             * Views visibilities. Always call these to make sure all views are
             * in right visibilities.
             */
            mHandler.removeCallbacksAndMessages(null);
            mViewLoading.setVisibility(View.GONE);
            mViewGroupControls.setVisibility(View.VISIBLE);

            mListView.post(new Runnable() {

                @Override
                public void run() {
                    mListView.setSelection(-1);
                }
            });

            break;// _LoaderHistoryData
        }
    }// onLoadFinished()

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (BuildConfig.DEBUG)
            Log.d(_ClassName, "onLoaderReset()");

        switch (loader.getId()) {
        case _LoaderHistoryData:
            mBookmarkCursorAdapter.changeCursor(null);
            mViewLoading.setVisibility(View.VISIBLE);

            break;// _LoaderHistoryData

        case _LoaderHistoryCounter:
            /*
             * NOTE: if using an adapter, set its cursor to null to release
             * memory.
             */
            if (mCursorCounter != null) {
                mCursorCounter.close();
                mCursorCounter = null;
            }

            break;// _LoaderHistoryCounter
        }
    }// onLoaderReset()

    /**
     * Loads content view from XML and init controls.
     */
    private View initContentView(LayoutInflater inflater, ViewGroup container) {
        View mainView = inflater.inflate(R.layout.afc_viewgroup_bookmarks, container, false);

        /*
         * Maps controls.
         */

        mViewGroupControls = mainView.findViewById(R.id.afc_viewgroup_bookmarks_viewgroup_controls);
        mListView = (ExpandableListView) mainView.findViewById(R.id.afc_viewgroup_bookmarks_listview_bookmarks);
        mViewFooter = (ViewGroup) mainView.findViewById(R.id.afc_viewgroup_bookmarks_viewgroup_footer);
        mBtnClear = (Button) mainView.findViewById(R.id.afc_viewgroup_bookmarks_button_clear);
        mBtnOk = (Button) mainView.findViewById(R.id.afc_viewgroup_bookmarks_button_ok);
        mViewLoading = mainView.findViewById(R.id.afc_viewgroup_bookmarks_view_loading);

        if (mEditor) {
            mViewFooter.setVisibility(View.VISIBLE);
        }

        /*
         * Listview.
         */

        mListView.setEmptyView(mainView.findViewById(R.id.afc_viewgroup_bookmarks_empty_view));
        mListView.setOnChildClickListener(mListViewOnChildClickListener);
        mListView.setOnItemLongClickListener(mListViewOnItemLongClickListener);
        initListViewGestureListener();

        /*
         * Adapter.
         */

        mBookmarkCursorAdapter = new BookmarkCursorAdapter(getActivity());
        mBookmarkCursorAdapter.setEditor(mEditor);
        mListView.setAdapter(mBookmarkCursorAdapter);

        /*
         * Events.
         */

        mBtnClear.setOnClickListener(mBtnClearOnClickListener);
        mBtnOk.setOnClickListener(mBtnOkOnClickListener);

        return mainView;
    }// initContentView()

    /**
     * As the name means.
     */
    private void initListViewGestureListener() {
        GestureUtils.setupGestureDetector(mListView, new GestureUtils.SimpleOnGestureListener() {

            @Override
            public boolean onFling(View view, Object data, FlingDirection flingDirection) {
                if (!isEditor() || !(data instanceof Cursor))
                    return false;

                List<Integer> ids = new ArrayList<Integer>();

                final int _id = ((Cursor) data).getInt(((Cursor) data).getColumnIndex(BookmarkContract.Bookmark._ID));
                if (mBookmarkCursorAdapter.isSelected(_id))
                    ids.addAll(mBookmarkCursorAdapter.getSelectedItemIds());
                else
                    ids.add(_id);

                if (ids.size() <= 1)
                    mBookmarkCursorAdapter.markItemAsDeleted(_id, true);
                else
                    mBookmarkCursorAdapter.markSelectedItemsAsDeleted(true);

                final StringBuilder _sb = new StringBuilder(String.format("%s in (", DbUtils._SqliteFtsColumnRowId));
                for (int id : ids)
                    _sb.append(Integer.toString(id)).append(',');
                _sb.setCharAt(_sb.length() - 1, ')');

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        getActivity().getContentResolver().delete(BookmarkContract.Bookmark._ContentUri,
                                _sb.toString(), null);
                    }// run()
                }, DisplayPrefs._DelayTimeForVeryShortAnimation);

                return true;
            }// onFling()
        });
    }// initListViewGestureListener()

    /**
     * Updates UI.
     */
    private void updateUI() {
        mViewFooter.setVisibility(isEditor() ? View.VISIBLE : View.GONE);
        mBtnClear.setEnabled(mItemCount > 0);
    }// updateUI()

    /*
     * UTILITIES
     */

    /**
     * Enables or disables editor mode.
     * 
     * @param editor
     *            {@code true} to enable, {@code false} to disable.
     */
    public void setEditor(boolean editor) {
        if (mEditor != editor) {
            mEditor = editor;
            if (mBookmarkCursorAdapter != null)
                mBookmarkCursorAdapter.setEditor(mEditor);

            updateUI();
        }
    }// setEditor()

    /**
     * Checks if current mode is editor or not.
     * 
     * @return {@code true} if current mode is editor.
     */
    public boolean isEditor() {
        return mEditor;
    }// isEditor()

    // /**
    // * Adds {@code listener}.
    // *
    // * @param listener
    // * {@link OnBookmarkItemClickListener}.
    // */
    // public void addOnBookmarkItemClickListener(OnBookmarkItemClickListener
    // listener) {
    // mOnBookmarkItemClickListeners.add(listener);
    // }// addOnBookmarkItemClickListener()
    //
    // /**
    // * Removes {@code listener}.
    // *
    // * @param listener
    // * {@link OnBookmarkItemClickListener}.
    // */
    // public void removeOnBookmarkItemClickListener(OnBookmarkItemClickListener
    // listener) {
    // mOnBookmarkItemClickListeners.remove(listener);
    // }// removeOnBookmarkItemClickListener()

    /*
     * LISTENERS
     */

    private final Runnable mViewLoadingShower = new Runnable() {

        @Override
        public void run() {
            if (isAdded()) {
                mViewGroupControls.setVisibility(View.GONE);
                mViewLoading.setVisibility(View.VISIBLE);
            }
        }// run()
    };// mViewLoadingShower

    private final ExpandableListView.OnChildClickListener mListViewOnChildClickListener = new ExpandableListView.OnChildClickListener() {

        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            Cursor cursor = mBookmarkCursorAdapter.getChild(groupPosition, childPosition);
            // TODO
            // Bookmark b = Bookmark.toBookmark(cursor);
            // if (b != null) {
            // for (OnBookmarkItemClickListener listener :
            // mOnBookmarkItemClickListeners)
            // listener.onItemClick(b);
            // if (getDialog() != null)
            // getDialog().dismiss();
            // return true;
            // }

            return false;
        }// onChildClick()
    };// mListViewOnChildClickListener

    private final AdapterView.OnItemLongClickListener mListViewOnItemLongClickListener = new AdapterView.OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            final int _iGroup = ExpandableListView
                    .getPackedPositionGroup(mListView.getExpandableListPosition(position));
            final int _iChild = ExpandableListView
                    .getPackedPositionChild(mListView.getExpandableListPosition(position));

            switch (ExpandableListView.getPackedPositionType(id)) {
            case ExpandableListView.PACKED_POSITION_TYPE_GROUP:
                if (!isEditor())
                    return false;

                if (!mListView.isGroupExpanded(_iGroup))
                    return false;

                if (BuildConfig.DEBUG)
                    Log.d(_ClassName, String.format("onItemLongClick() -- group = %,d", _iGroup));
                ContextMenuUtils.showContextMenu(getActivity(), 0, R.string.afc_title_advanced_selection,
                        BookmarkCursorAdapter._AdvancedSelectionOptions,
                        new ContextMenuUtils.OnMenuItemClickListener() {

                            @Override
                            public void onClick(final int resId) {
                                if (resId == R.string.afc_cmd_advanced_selection_all)
                                    mBookmarkCursorAdapter.selectAll(_iGroup, true);
                                else if (resId == R.string.afc_cmd_advanced_selection_none)
                                    mBookmarkCursorAdapter.selectAll(_iGroup, false);
                                else if (resId == R.string.afc_cmd_advanced_selection_invert)
                                    mBookmarkCursorAdapter.invertSelection(_iGroup);
                            }// onClick()
                        });

                return true;// PACKED_POSITION_TYPE_GROUP

            case ExpandableListView.PACKED_POSITION_TYPE_CHILD:
                // TODO
                // final Bookmark _bookmark =
                // Bookmark.toBookmark(mBookmarkCursorAdapter.getChild(_iGroup,
                // _iChild));
                // ContextMenuUtils.showContextMenu(getActivity(),
                // R.drawable.afc_bookmarks_dark,
                // TextUtils.quote(_bookmark.getName()), null, new Integer[][] {
                // { R.string.afc_cmd_rename },
                // { R.string.afc_cmd_sort_by_name } }, null,
                // new ContextMenuUtils.OnMenuItemClickListener() {
                //
                // @Override
                // public void onClick(int resId) {
                // if (resId == R.string.afc_cmd_rename) {
                // BookmarkBarContextMenuUtils.doEnterNewNameOrRenameBookmark(getActivity(),
                // _bookmark, false, new TaskListener() {
                //
                // @Override
                // public void onFinish(boolean ok, Object any) {
                // getActivity().getContentResolver().update(
                // BookmarkContract.Bookmark._ContentUri,
                // Bookmark.toContentValues(_bookmark),
                // String.format("%s = %s and %s like %s",
                // BookmarkContract.Bookmark._ColumnProviderId,
                // DatabaseUtils.sqlEscapeString(_bookmark
                // .getProvider()),
                // BookmarkContract.Bookmark._ColumnUri, DatabaseUtils
                // .sqlEscapeString(_bookmark.getUri()
                // .toString())), null);
                // }// onFinish()
                // });
                // } else if (resId == R.string.afc_cmd_sort_by_name) {
                // sortBookmarks(_iGroup);
                // }
                // }// onClick()
                // });
                return true;// PACKED_POSITION_TYPE_CHILD
            }

            return false;
        }// onItemLongClick()

        /**
         * Sorts bookmarks.
         * 
         * @param groupPosition
         *            the group position.
         */
        private void sortBookmarks(int groupPosition) {
            // TODO
            // List<Bookmark> bookmarks = new ArrayList<Bookmark>();
            //
            // for (int i = 0; i <
            // mBookmarkCursorAdapter.getChildrenCount(groupPosition); i++)
            // bookmarks.add(Bookmark.toBookmark(mBookmarkCursorAdapter.getChild(groupPosition,
            // i)));
            // final Collator _collator = Collator.getInstance();
            // /*
            // * Sorts descending.
            // */
            // Collections.sort(bookmarks, new Comparator<Bookmark>() {
            //
            // @Override
            // public int compare(Bookmark lhs, Bookmark rhs) {
            // return _collator.compare(rhs.getName(), lhs.getName());
            // }// compare()
            // });
            //
            // ContentResolver contentResolver =
            // getActivity().getContentResolver();
            // for (int i = 0; i < bookmarks.size(); i++) {
            // /*
            // * The list was sorted descending by name (Z-A), now we add "i"
            // * to timestamp (last modified), so the list will be obtained
            // * ascending by name (A-Z) as it will be obtained from DB
            // * descending by last modified.
            // */
            // contentResolver.update(BookmarkContract.Bookmark._ContentUri,
            // Bookmark.toContentValues(bookmarks.get(i).setLastModified(new
            // Date().getTime() + i)),
            // String.format("%s = %d", DbUtils._SqliteFtsColumnRowId,
            // bookmarks.get(i).getId()), null);
            // }
            // mBookmarkCursorAdapter.notifyDataSetChanged();
        }// doConfirmAndSortBookmarks()
    };// mListViewOnItemLongClickListener

    private final View.OnClickListener mBtnClearOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (mBookmarkCursorAdapter.getGroupCount() == 1 && mBookmarkCursorAdapter.getChildrenCount(0) == 1) {
                clearBookmarksAndDismiss();
            } else {
                Dlg.confirmYesno(getActivity(), getString(R.string.afc_msg_confirm_clear_all_bookmarks),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                clearBookmarksAndDismiss();
                            }// onClick()
                        });
            }
        }// onClick()

        private void clearBookmarksAndDismiss() {
            getActivity().getContentResolver().delete(BookmarkContract.Bookmark._ContentUri, null, null);
            updateUI();
            if (getDialog() != null)
                getDialog().dismiss();
        }// clearBookmarks()
    };// mBtnClearOnClickListener

    private final View.OnClickListener mBtnOkOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (getDialog() != null)
                getDialog().dismiss();
            else
                setEditor(false);
        }// onClick()
    };// mBtnOkOnClickListener
}
