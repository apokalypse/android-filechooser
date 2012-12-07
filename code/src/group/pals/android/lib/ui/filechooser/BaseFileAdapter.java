/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser;

import group.pals.android.lib.ui.filechooser.prefs.DisplayPrefs;
import group.pals.android.lib.ui.filechooser.prefs.DisplayPrefs.FileTimeDisplay;
import group.pals.android.lib.ui.filechooser.providers.basefile.BaseFileContract.BaseFile;
import group.pals.android.lib.ui.filechooser.utils.Converter;
import group.pals.android.lib.ui.filechooser.utils.DateUtils;
import group.pals.android.lib.ui.filechooser.utils.FileUtils;
import group.pals.android.lib.ui.filechooser.utils.Ui;
import group.pals.android.lib.ui.filechooser.utils.ui.ContextMenuUtils;
import group.pals.android.lib.ui.filechooser.utils.ui.LoadingDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.ResourceCursorAdapter;
import android.util.SparseArray;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class BaseFileAdapter extends ResourceCursorAdapter {

    private final int mFilterMode;
    private final FileTimeDisplay mFileTimeDisplay;
    private final Integer[] mAdvancedSelectionOptions;
    private boolean mMultiSelection;

    public BaseFileAdapter(Context context, int filterMode, boolean multiSelection) {
        super(context, R.layout.afc_file_item, null, 0);
        mFilterMode = filterMode;
        mMultiSelection = multiSelection;

        switch (mFilterMode) {
        case BaseFile._FilterFilesAndDirectories:
            mAdvancedSelectionOptions = new Integer[] { R.string.afc_cmd_advanced_selection_all,
                    R.string.afc_cmd_advanced_selection_none, R.string.afc_cmd_advanced_selection_invert,
                    R.string.afc_cmd_select_all_files, R.string.afc_cmd_select_all_folders };
            break;// _FilterFilesAndDirectories
        default:
            mAdvancedSelectionOptions = new Integer[] { R.string.afc_cmd_advanced_selection_all,
                    R.string.afc_cmd_advanced_selection_none, R.string.afc_cmd_advanced_selection_invert };
            break;// _FilterDirectoriesOnly and _FilterFilesOnly
        }

        mFileTimeDisplay = new FileTimeDisplay(DisplayPrefs.isShowTimeForOldDaysThisYear(context),
                DisplayPrefs.isShowTimeForOldDays(context));
    }// BaseFileAdapter()

    /**
     * The "view holder"
     * 
     * @author Hai Bison
     * 
     */
    private static final class Bag {

        ImageView mImageIcon;
        ImageView mImageLockedSymbol;
        TextView mTxtFileName;
        TextView mTxtFileInfo;
        CheckBox mCheckboxSelection;
    }// Bag

    private static class BagInfo {

        boolean mChecked = false;
        boolean mMarkedAsDeleted = false;
    }// BagChildInfo

    /**
     * Map of child IDs to {@link BagChildInfo}.
     */
    private final SparseArray<BagInfo> mSelectedChildrenMap = new SparseArray<BagInfo>();

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Bag bag = (Bag) view.getTag();

        if (bag == null) {
            bag = new Bag();
            bag.mImageIcon = (ImageView) view.findViewById(R.id.afc_file_item_imageview_icon);
            bag.mImageLockedSymbol = (ImageView) view.findViewById(R.id.afc_file_item_imageview_locked_symbol);
            bag.mTxtFileName = (TextView) view.findViewById(R.id.afc_file_item_textview_filename);
            bag.mTxtFileInfo = (TextView) view.findViewById(R.id.afc_file_item_textview_file_info);
            bag.mCheckboxSelection = (CheckBox) view.findViewById(R.id.afc_file_item_checkbox_selection);

            view.setTag(bag);
        }

        final int _id = cursor.getInt(cursor.getColumnIndex(BaseFile._ID));

        final BagInfo _bagInfo;
        if (mSelectedChildrenMap.get(_id) == null) {
            _bagInfo = new BagInfo();
            mSelectedChildrenMap.put(_id, _bagInfo);
        } else
            _bagInfo = mSelectedChildrenMap.get(_id);

        /*
         * Update views.
         */

        /*
         * Use single line for grid view, multiline for list view
         */
        bag.mTxtFileName.setSingleLine(view.getParent() instanceof GridView);

        Uri uri = Uri.parse(cursor.getString(cursor.getColumnIndex(BaseFile._ColumnUri)));

        /*
         * File icon.
         */
        bag.mImageIcon.setImageResource(FileUtils.getResIcon(
                cursor.getInt(cursor.getColumnIndex(BaseFile._ColumnType)), uri.getLastPathSegment()));
        bag.mImageLockedSymbol
                .setVisibility(cursor.getInt(cursor.getColumnIndex(BaseFile._ColumnCanRead)) > 0 ? View.GONE
                        : View.VISIBLE);

        /*
         * Filename.
         */
        bag.mTxtFileName.setText(uri.getLastPathSegment());
        Ui.strikeOutText(bag.mTxtFileName, _bagInfo.mMarkedAsDeleted);

        /*
         * File info.
         */
        String time = DateUtils.formatDate(mContext,
                cursor.getLong(cursor.getColumnIndex(BaseFile._ColumnModificationTime)), mFileTimeDisplay);
        if (cursor.getInt(cursor.getColumnIndex(BaseFile._ColumnType)) == BaseFile._FileTypeFile)
            bag.mTxtFileInfo.setText(String.format("%s, %s",
                    Converter.sizeToStr(cursor.getLong(cursor.getColumnIndex(BaseFile._ColumnSize))), time));
        else
            bag.mTxtFileInfo.setText(time);

        /*
         * Check box.
         */
        if (mMultiSelection) {
            if (mFilterMode == BaseFile._FilterFilesOnly
                    && cursor.getInt(cursor.getColumnIndex(BaseFile._ColumnType)) == BaseFile._FileTypeDirectory) {
                bag.mCheckboxSelection.setVisibility(View.GONE);
            } else {
                bag.mCheckboxSelection.setVisibility(View.VISIBLE);

                bag.mCheckboxSelection.setOnCheckedChangeListener(null);
                bag.mCheckboxSelection.setChecked(_bagInfo.mChecked);
                bag.mCheckboxSelection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        _bagInfo.mChecked = isChecked;
                    }// onCheckedChanged()
                });

                bag.mCheckboxSelection.setOnLongClickListener(mCheckboxSelectionOnLongClickListener);
            }
        } else
            bag.mCheckboxSelection.setVisibility(View.GONE);
    }// bindView()

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        // TODO
        // synchronized (mSelectedChildrenMap) {
        // mSelectedChildrenMap.clear();
        // }
    }// notifyDataSetChanged()

    @Override
    public void notifyDataSetInvalidated() {
        super.notifyDataSetInvalidated();
        // TODO
        // synchronized (mSelectedChildrenMap) {
        // mSelectedChildrenMap.clear();
        // }
    }// notifyDataSetInvalidated()

    /*
     * UTILITIES.
     */

    /**
     * Selects all items.<br>
     * <b>Note:</b> This will <i>not</i> notify data set for changes after done.
     * 
     * @param selected
     *            {@code true} or {@code false}.
     */
    private void asyncSelectAll(boolean selected) {
        int count = getCount();
        for (int i = 0; i < count; i++) {
            Cursor cursor = (Cursor) getItem(i);

            int fileType = cursor.getInt(cursor.getColumnIndex(BaseFile._ColumnType));
            if ((mFilterMode == BaseFile._FilterDirectoriesOnly && fileType == BaseFile._FileTypeFile)
                    || (mFilterMode == BaseFile._FilterFilesOnly && fileType == BaseFile._FileTypeDirectory))
                continue;

            final int _id = cursor.getInt(cursor.getColumnIndex(BaseFile._ID));
            BagInfo b = mSelectedChildrenMap.get(_id);
            if (b == null) {
                b = new BagInfo();
                mSelectedChildrenMap.put(_id, b);
            }
            b.mChecked = selected;
        }// for i
    }// asyncSelectAll()

    /**
     * Selects all items.<br>
     * <b>Note:</b> This calls {@link #notifyDataSetChanged()} after done.
     * 
     * @param selected
     *            {@code true} or {@code false}.
     */
    public synchronized void selectAll(boolean selected) {
        asyncSelectAll(selected);
        notifyDataSetChanged();
    }// selectAll()

    /**
     * Inverts selection of all items.<br>
     * <b>Note:</b> This will <i>not</i> notify data set for changes after done.
     */
    private void asyncInvertSelection() {
        int count = getCount();
        for (int i = 0; i < count; i++) {
            Cursor cursor = (Cursor) getItem(i);

            int fileType = cursor.getInt(cursor.getColumnIndex(BaseFile._ColumnType));
            if ((mFilterMode == BaseFile._FilterDirectoriesOnly && fileType == BaseFile._FileTypeFile)
                    || (mFilterMode == BaseFile._FilterFilesOnly && fileType == BaseFile._FileTypeDirectory))
                continue;

            final int _id = cursor.getInt(cursor.getColumnIndex(BaseFile._ID));
            BagInfo b = mSelectedChildrenMap.get(_id);
            if (b == null) {
                b = new BagInfo();
                mSelectedChildrenMap.put(_id, b);
            }
            b.mChecked = !b.mChecked;
        }// for i
    }// asyncInvertSelection()

    /**
     * Inverts selection of all items.<br>
     * <b>Note:</b> This calls {@link #notifyDataSetChanged()} after done.
     */
    public synchronized void invertSelection() {
        asyncInvertSelection();
        notifyDataSetChanged();
    }// invertSelection()

    /*
     * LISTENERS
     */

    private final View.OnLongClickListener mCheckboxSelectionOnLongClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(final View v) {
            ContextMenuUtils.showContextMenu(v.getContext(), 0, R.string.afc_title_advanced_selection,
                    mAdvancedSelectionOptions, new ContextMenuUtils.OnMenuItemClickListener() {

                        @Override
                        public void onClick(final int resId) {
                            new LoadingDialog(v.getContext(), R.string.afc_msg_loading, false) {

                                @Override
                                protected Object doInBackground(Void... params) {
                                    if (resId == R.string.afc_cmd_advanced_selection_all)
                                        asyncSelectAll(true);
                                    else if (resId == R.string.afc_cmd_advanced_selection_none)
                                        asyncSelectAll(false);
                                    else if (resId == R.string.afc_cmd_advanced_selection_invert)
                                        asyncInvertSelection();
                                    else if (resId == R.string.afc_cmd_select_all_files)
                                        asyncInvertSelection();
                                    else if (resId == R.string.afc_cmd_select_all_folders)
                                        asyncInvertSelection();

                                    return null;
                                }// doInBackground()

                                @Override
                                protected void onPostExecute(Object result) {
                                    super.onPostExecute(result);
                                    notifyDataSetChanged();
                                }// onPostExecute()
                            };
                        }// onClick()
                    });

            return true;
        }// onLongClick()
    };// mCheckboxSelectionOnLongClickListener
}
