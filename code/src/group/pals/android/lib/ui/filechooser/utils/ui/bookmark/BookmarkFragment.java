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
import group.pals.android.lib.ui.filechooser.utils.EnvUtils;
import group.pals.android.lib.ui.filechooser.utils.TextUtils;
import group.pals.android.lib.ui.filechooser.utils.Ui;
import group.pals.android.lib.ui.filechooser.utils.ui.ContextMenuUtils;
import group.pals.android.lib.ui.filechooser.utils.ui.Dlg;
import group.pals.android.lib.ui.filechooser.utils.ui.GestureUtils;
import group.pals.android.lib.ui.filechooser.utils.ui.GestureUtils.FlingDirection;
import group.pals.android.lib.ui.filechooser.utils.ui.history.HistoryFragment;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;

/**
 * Fragment to manage bookmarks.
 * 
 * @since v5.1 beta
 * @author Hai Bison
 * 
 */
public class BookmarkFragment extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * As the name means.
     * 
     * @since v5.1 beta
     * @author Hai Bison
     * 
     */
    public static interface OnBookmarkItemClickListener {

        /**
         * Will be called after the bookmark was clicked.
         * 
         * @param providerId
         *            the original provider ID.
         * @param uri
         *            the URI to a directory.
         */
        void onItemClick(String providerId, Uri uri);
    }// OnBookmarkItemClickListener

    /**
     * Used for debugging or something...
     */
    private static final String _ClassName = BookmarkFragment.class.getName();

    private static final String _ModeEditor = _ClassName + ".mode_editor";

    private final int _LoaderBookmarkData = EnvUtils.genId();
    private final int _LoaderBookmarkCounter = EnvUtils.genId();

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
    private OnBookmarkItemClickListener mOnBookmarkItemClickListener;

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
        getLoaderManager().initLoader(_LoaderBookmarkData, null, this);
        getLoaderManager().initLoader(_LoaderBookmarkCounter, null, this);
    }// onActivityCreated()

    /*
     * LOADERMANAGER.LOADERCALLBACKS
     */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (BuildConfig.DEBUG)
            Log.d(_ClassName, "onCreateLoader()");
        if (id == _LoaderBookmarkCounter) {
            return new CursorLoader(getActivity(), BookmarkContract.Bookmark._ContentUri,
                    new String[] { BookmarkContract.Bookmark._COUNT }, null, null, null);
        } // _LoaderBookmarkCounter
        else if (id == _LoaderBookmarkData) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler.postDelayed(mViewLoadingShower, DisplayPrefs._DelayTimeForSimpleAnimation);

            mBookmarkCursorAdapter.changeCursor(null);

            return new CursorLoader(getActivity(), BookmarkContract.Bookmark._ContentUri, null, null, null,
                    BookmarkContract.Bookmark._ColumnProviderId);
        }// _LoaderBookmarkData

        return null;
    }// onCreateLoader()

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (BuildConfig.DEBUG)
            Log.d(_ClassName, "onLoadFinished() -- data = " + data);

        if (loader.getId() == _LoaderBookmarkCounter) {
            if (mCursorCounter != null)
                mCursorCounter.close();

            mCursorCounter = data;
            if (mCursorCounter.moveToFirst()) {
                int newItemCount = mCursorCounter.getInt(mCursorCounter
                        .getColumnIndex(BookmarkContract.Bookmark._COUNT));
                if (mItemCount >= 0 && newItemCount != mItemCount)
                    getLoaderManager().restartLoader(_LoaderBookmarkData, null, this);
                mItemCount = newItemCount;
            } else {
                mItemCount = 0;
            }

            updateUI();
        }// _LoaderBookmarkCounter
        else if (loader.getId() == _LoaderBookmarkData) {
            mBookmarkCursorAdapter.changeCursor(data);

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
        }// _LoaderBookmarkData
    }// onLoadFinished()

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (BuildConfig.DEBUG)
            Log.d(_ClassName, "onLoaderReset()");

        if (loader.getId() == _LoaderBookmarkData) {
            mBookmarkCursorAdapter.changeCursor(null);
            mViewLoading.setVisibility(View.VISIBLE);
        }// _LoaderBookmarkData
        else if (loader.getId() == _LoaderBookmarkCounter) {
            /*
             * NOTE: if using an adapter, set its cursor to null to release
             * memory.
             */
            if (mCursorCounter != null) {
                mCursorCounter.close();
                mCursorCounter = null;
            }
        }// _LoaderBookmarkCounter
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

    /**
     * Sets a listener to {@link OnBookmarkItemClickListener}.
     * 
     * @param listener
     *            the listener.
     */
    public void setOnBookmarkItemClickListener(OnBookmarkItemClickListener listener) {
        mOnBookmarkItemClickListener = listener;
    }// setOnBookmarkItemClickListener()

    /**
     * Gets the listener of {@link OnBookmarkItemClickListener}.
     * 
     * @return the listener.
     */
    public OnBookmarkItemClickListener getOnBookmarkItemClickListener() {
        return mOnBookmarkItemClickListener;
    }// getOnBookmarkItemClickListener()

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
            if (getOnBookmarkItemClickListener() != null) {
                Cursor cursor = mBookmarkCursorAdapter.getChild(groupPosition, childPosition);
                getOnBookmarkItemClickListener().onItemClick(
                        cursor.getString(cursor.getColumnIndex(BookmarkContract.Bookmark._ColumnProviderId)),
                        Uri.parse(cursor.getString(cursor.getColumnIndex(BookmarkContract.Bookmark._ColumnUri))));
            }

            if (getDialog() != null)
                getDialog().dismiss();

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
                Cursor cursor = mBookmarkCursorAdapter.getChild(_iGroup, _iChild);
                final String _providerId = cursor.getString(cursor
                        .getColumnIndex(BookmarkContract.Bookmark._ColumnProviderId));
                final int _id = cursor.getInt(cursor.getColumnIndex(BookmarkContract.Bookmark._ID));
                final Uri _uri = Uri
                        .parse(cursor.getString(cursor.getColumnIndex(BookmarkContract.Bookmark._ColumnUri)));
                final String _name = cursor.getString(cursor.getColumnIndex(BookmarkContract.Bookmark._ColumnName));

                ContextMenuUtils.showContextMenu(getActivity(), R.drawable.afc_bookmarks_dark, TextUtils.quote(_name),
                        new Integer[] { R.string.afc_cmd_rename, R.string.afc_cmd_sort_by_name },
                        new ContextMenuUtils.OnMenuItemClickListener() {

                            @Override
                            public void onClick(int resId) {
                                if (resId == R.string.afc_cmd_rename) {
                                    doEnterNewNameOrRenameBookmark(getActivity(), _providerId, _id, _uri, _name);
                                } else if (resId == R.string.afc_cmd_sort_by_name) {
                                    sortBookmarks(_iGroup);
                                }
                            }// onClick()
                        });
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
            Map<String, Integer> bookmarks = new HashMap<String, Integer>();

            for (int i = 0; i < mBookmarkCursorAdapter.getChildrenCount(groupPosition); i++) {
                Cursor cursor = mBookmarkCursorAdapter.getChild(groupPosition, i);
                bookmarks.put(cursor.getString(cursor.getColumnIndex(BookmarkContract.Bookmark._ColumnName)),
                        cursor.getInt(cursor.getColumnIndex(BookmarkContract.Bookmark._ID)));
            }

            List<String> names = new ArrayList<String>(bookmarks.keySet());
            Collections.sort(names, new Comparator<String>() {

                final Collator mCollator = Collator.getInstance();

                @Override
                public int compare(String lhs, String rhs) {
                    return mCollator.compare(lhs, rhs);
                }// compare()
            });

            ContentResolver contentResolver = getActivity().getContentResolver();
            /*
             * The list was sorted ascending by name (A-Z), now we add "i" to
             * timestamp (last modified), so the list will be obtained ascending
             * by name (A-Z) as it will be obtained from DB descending by last
             * modified.
             */
            ContentValues values = new ContentValues();
            for (int i = names.size() - 1; i >= 0; i--) {
                values.put(BookmarkContract.Bookmark._ColumnModificationTime,
                        DbUtils.formatNumber(new Date().getTime() + i));
                contentResolver.update(BookmarkContract.Bookmark._ContentUri, values,
                        String.format("%s = %d", DbUtils._SqliteFtsColumnRowId, bookmarks.get(names.get(i))), null);
            }
        }// sortBookmarks()
    };// mListViewOnItemLongClickListener

    /**
     * Shows a dialog to let user enter new name or change current name of a
     * bookmark.
     * 
     * @param context
     *            {@link Context}
     * @param providerId
     *            the provider ID.
     * @param id
     *            the bookmark ID.
     * @param uri
     *            the URI to the bookmark.
     * @param name
     *            the name. To enter new name, this is the suggested name you
     *            provide. To rename, this is the old name.
     */
    public static void doEnterNewNameOrRenameBookmark(final Context context, final String providerId, final int id,
            final Uri uri, final String name) {
        final AlertDialog _dlg = Dlg.newDlg(context);

        View view = LayoutInflater.from(context).inflate(R.layout.afc_simple_text_input_view, null);
        final EditText _txtName = (EditText) view.findViewById(R.id.afc_simple_text_input_view_text1);
        _txtName.setText(name);
        _txtName.selectAll();
        _txtName.setHint(R.string.afc_hint_new_name);
        _txtName.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Ui.showSoftKeyboard(_txtName, false);
                    Button btn = _dlg.getButton(DialogInterface.BUTTON_POSITIVE);
                    if (btn.isEnabled())
                        btn.performClick();
                    return true;
                }
                return false;
            }// onEditorAction()
        });

        _dlg.setView(view);
        _dlg.setIcon(R.drawable.afc_bookmarks_dark);
        _dlg.setTitle(id < 0 ? R.string.afc_title_new_bookmark : R.string.afc_title_rename);
        _dlg.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = _txtName.getText().toString().trim();
                        if (android.text.TextUtils.isEmpty(newName)) {
                            Dlg.toast(context, R.string.afc_msg_bookmark_name_is_invalid, Dlg._LengthShort);
                            return;
                        }

                        Ui.showSoftKeyboard(_txtName, false);

                        ContentValues values = new ContentValues();
                        values.put(BookmarkContract.Bookmark._ColumnName, newName);

                        if (id >= 0) {
                            values.put(BookmarkContract.Bookmark._ColumnModificationTime,
                                    DbUtils.formatNumber(new Date().getTime()));
                            context.getContentResolver().update(
                                    Uri.withAppendedPath(BookmarkContract.Bookmark._ContentIdUriBase,
                                            Uri.encode(Integer.toString(id))), values, null, null);
                        } else {
                            /*
                             * Check if the URI exists or doesn't. If it exists,
                             * update it instead of inserting the new one.
                             */
                            Cursor cursor = context.getContentResolver().query(
                                    BookmarkContract.Bookmark._ContentUri,
                                    null,
                                    String.format("%s = %s AND %s LIKE %s",
                                            BookmarkContract.Bookmark._ColumnProviderId,
                                            DatabaseUtils.sqlEscapeString(providerId),
                                            BookmarkContract.Bookmark._ColumnUri,
                                            DatabaseUtils.sqlEscapeString(uri.toString())), null, null);
                            try {
                                if (cursor != null && cursor.moveToFirst()) {
                                    values.put(BookmarkContract.Bookmark._ColumnModificationTime,
                                            DbUtils.formatNumber(new Date().getTime()));
                                    context.getContentResolver().update(
                                            Uri.withAppendedPath(BookmarkContract.Bookmark._ContentIdUriBase, Uri
                                                    .encode(cursor.getString(cursor
                                                            .getColumnIndex(BookmarkContract.Bookmark._ID)))), values,
                                            null, null);
                                } else {
                                    values.put(BookmarkContract.Bookmark._ColumnProviderId, providerId);
                                    values.put(BookmarkContract.Bookmark._ColumnUri, uri.toString());

                                    context.getContentResolver().insert(BookmarkContract.Bookmark._ContentUri, values);
                                }
                            } finally {
                                if (cursor != null)
                                    cursor.close();
                            }
                        }

                        Dlg.toast(context, context.getString(R.string.afc_msg_done), Dlg._LengthShort);
                    }// onClick()
                });

        _dlg.show();
        Ui.showSoftKeyboard(_txtName, true);

        final Button _btnOk = _dlg.getButton(DialogInterface.BUTTON_POSITIVE);
        _btnOk.setEnabled(id < 0);

        _txtName.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                String newName = s.toString().trim();
                boolean enabled = !android.text.TextUtils.isEmpty(newName);
                _btnOk.setEnabled(enabled);

                /*
                 * If renaming, only enable button OK if new name is not equal
                 * to the old one.
                 */
                if (enabled && id >= 0)
                    _btnOk.setEnabled(!newName.equals(name));
            }
        });
    }// doEnterNewNameOrRenameBookmark()

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
