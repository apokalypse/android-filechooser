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

package group.pals.android.lib.ui.filechooser;

import group.pals.android.lib.ui.filechooser.io.IFile;
import group.pals.android.lib.ui.filechooser.io.IFileFilter;
import group.pals.android.lib.ui.filechooser.services.IFileProvider;
import group.pals.android.lib.ui.filechooser.services.IFileProvider.FilterMode;
import group.pals.android.lib.ui.filechooser.utils.Converter;
import group.pals.android.lib.ui.filechooser.utils.FileUtils;
import group.pals.android.lib.ui.filechooser.utils.MimeTypes;
import group.pals.android.lib.ui.filechooser.utils.ui.ContextMenuUtils;
import group.pals.android.lib.ui.filechooser.utils.ui.LoadingDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * The adapter to be used in {@link android.widget.ListView}
 * 
 * @author Hai Bison
 * 
 */
public class IFileAdapter extends BaseAdapter {

    /**
     * Used for logging...
     */
    public static final String _ClassName = IFileAdapter.class.getName();

    /**
     * Default short format for file time. Value = {@code "yyyy.MM.dd hh:mm a"}<br>
     * See <a href=
     * "http://developer.android.com/reference/java/text/SimpleDateFormat.html"
     * >API docs</a>.
     */
    public static final String _DefFileTimeShortFormat = "yyyy.MM.dd hh:mm a";

    /**
     * You can set your own short format for file time by this variable. If the
     * value is in wrong format, {@link #_DefFileTimeShortFormat} will be used.<br>
     * See <a href=
     * "http://developer.android.com/reference/java/text/SimpleDateFormat.html"
     * >API docs</a>.
     */
    public static String fileTimeShortFormat = _DefFileTimeShortFormat;

    private final Integer[] mAdvancedSelectionOptions;
    private final IFileProvider.FilterMode mFilterMode;

    private List<IFileDataModel> mData;
    private LayoutInflater mInflater;
    private boolean mMultiSelection;
    private boolean mUseThumbnailGenerator;
    private AbsListView.OnScrollListener mOnScrollListener;
    private boolean mScrolling;
    private final int mThumbnailSize;
    private final ThumbnailGeneratorManager mThumbnailGeneratorManager = new ThumbnailGeneratorManager();

    /**
     * Creates new {@link IFileAdapter}
     * 
     * @param context
     *            {@link Context}
     * @param objects
     *            the data
     * @param filterMode
     *            see {@link IFileProvider.FilterMode}
     * @param multiSelection
     *            see {@link FileChooserActivity#_MultiSelection}
     */
    public IFileAdapter(Context context, List<IFileDataModel> objects, IFileProvider.FilterMode filterMode,
            boolean multiSelection, boolean useThumbnailGenerator) {
        mData = objects;
        mInflater = LayoutInflater.from(context);
        mFilterMode = filterMode;
        mMultiSelection = multiSelection;
        mUseThumbnailGenerator = useThumbnailGenerator;

        switch (mFilterMode) {
        case DirectoriesOnly:
        case FilesOnly:
            mAdvancedSelectionOptions = new Integer[] { R.string.afc_cmd_advanced_selection_all,
                    R.string.afc_cmd_advanced_selection_none, R.string.afc_cmd_advanced_selection_invert };
            break;// DirectoriesOnly and FilesOnly
        default:
            mAdvancedSelectionOptions = new Integer[] { R.string.afc_cmd_advanced_selection_all,
                    R.string.afc_cmd_advanced_selection_none, R.string.afc_cmd_advanced_selection_invert,
                    R.string.afc_cmd_select_all_files, R.string.afc_cmd_select_all_folders };
            break;// FilesAndDirectories
        }

        mThumbnailSize = context.getResources().getDimensionPixelSize(R.dimen.afc_thumbnail_size);
        initOnScrollListener();
    }// IFileAdapter

    private void initOnScrollListener() {
        mOnScrollListener = new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                    mScrolling = false;

                    int first = view.getFirstVisiblePosition();
                    int count = view.getChildCount();
                    for (int i = 0; i < count; i++) {
                        View childView = view.getChildAt(i);
                        if (childView.getTag() instanceof Bag)
                            updateThumbnail(getItem(first + i).getFile(), (Bag) childView.getTag());
                    }// for

                    break;// SCROLL_STATE_IDLE
                default:
                    mScrolling = true;
                    mThumbnailGeneratorManager.cancelAll();

                    break;// default
                }
            }// onScrollStateChanged()

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // TODO Auto-generated method stub
            }// onScroll()
        };
    }// initOnScrollListener()

    public void initListView(AbsListView listView) {
        listView.setOnScrollListener(mOnScrollListener);
    }// initListView()

    @Override
    public int getCount() {
        return mData != null ? mData.size() : 0;
    }

    @Override
    public IFileDataModel getItem(int position) {
        return mData != null ? mData.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public boolean isMultiSelection() {
        return mMultiSelection;
    }

    /**
     * Sets multi-selection mode.<br>
     * <b>Note:</b><br>
     * 
     * <li>If {@code v = true}, this method will also update adapter.</li>
     * 
     * <li>If {@code v = false}, this method will iterate all items, set their
     * selection to {@code false}. So you should consider using a
     * {@link LoadingDialog}. This will not update adapter, you must do it
     * yourself.</li>
     * 
     * @param v
     *            {@code true} if multi-selection is enabled
     */
    public void setMultiSelection(boolean v) {
        if (mMultiSelection != v) {
            mMultiSelection = v;
            if (mMultiSelection) {
                notifyDataSetChanged();
            } else {
                if (getCount() > 0) {
                    for (int i = 0; i < mData.size(); i++)
                        mData.get(i).setSelected(false);
                }
            }
        }
    }// setMultiSelection()

    /**
     * Gets selected items.
     * 
     * @return list of selected items, can be empty but never be {@code null}
     */
    public ArrayList<IFileDataModel> getSelectedItems() {
        ArrayList<IFileDataModel> res = new ArrayList<IFileDataModel>();

        for (int i = 0; i < getCount(); i++)
            if (getItem(i).isSelected())
                res.add(getItem(i));

        return res;
    }// getSelectedItems()

    /**
     * Adds an {@code item}. <b>Note:</b> this does not notify the adapter that
     * data set has been changed.
     * 
     * @param item
     *            {@link IFileDataModel}
     */
    public void add(IFileDataModel item) {
        if (mData != null)
            mData.add(item);
    }

    /**
     * Removes {@code item}. <b>Note:</b> this does not notify the adapter that
     * data set has been changed.
     * 
     * @param item
     *            {@link IFileDataModel}
     */
    public void remove(IFileDataModel item) {
        if (mData != null) {
            mData.remove(item);
        }
    }// remove()

    /**
     * Removes all {@code items}. <b>Note:</b> this does not notify the adapter
     * that data set has been changed.
     * 
     * @param items
     *            the items you want to remove.
     */
    public void removeAll(Collection<IFileDataModel> items) {
        if (mData != null)
            mData.removeAll(items);
    }// removeAll()

    /**
     * Clears all items. <b>Note:</b> this does not notify the adapter that data
     * set has been changed.
     */
    public void clear() {
        if (mData != null)
            mData.clear();
    }// clear()

    /**
     * The "view holder"
     * 
     * @author Hai Bison
     * 
     */
    private static final class Bag {

        ViewGroup mImageViewContainer;
        ImageView mImageIcon;
        boolean mThumbnailIsProceessed;
        TextView mTxtFileName;
        TextView mTxtFileInfo;
        CheckBox mCheckboxSelection;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        IFileDataModel data = getItem(position);
        Bag bag;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.afc_file_item, null);

            bag = new Bag();
            bag.mImageViewContainer = (ViewGroup) convertView.findViewById(R.id.afc_file_item_imageview_container);
            bag.mImageIcon = (ImageView) convertView.findViewById(R.id.afc_file_item_imageview_icon);
            bag.mTxtFileName = (TextView) convertView.findViewById(R.id.afc_file_item_textview_filename);
            bag.mTxtFileInfo = (TextView) convertView.findViewById(R.id.afc_file_item_textview_file_info);
            bag.mCheckboxSelection = (CheckBox) convertView.findViewById(R.id.afc_file_item_checkbox_selection);

            convertView.setTag(bag);
        } else {
            bag = (Bag) convertView.getTag();
        }

        // update view
        updateView(parent, bag, data, data.getFile());

        return convertView;
    }

    /**
     * Updates the view.
     * 
     * @param parent
     *            the parent view
     * @param bag
     *            the "view holder", see {@link Bag}
     * @param data
     *            {@link IFileDataModel}
     * @param file
     *            {@link IFile}
     * @since v2.0 alpha
     */
    private void updateView(ViewGroup parent, Bag bag, final IFileDataModel data, IFile file) {
        // if parent is list view, enable multiple lines
        boolean useSingleLine = parent instanceof GridView;
        for (TextView tv : new TextView[] { bag.mTxtFileName, bag.mTxtFileInfo }) {
            tv.setSingleLine(useSingleLine);
            if (useSingleLine)
                tv.setEllipsize(TextUtils.TruncateAt.END);
        }

        // image icon
        bag.mImageIcon.setImageResource(FileUtils.getResIcon(file));
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) bag.mImageViewContainer.getLayoutParams();
        lp.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        lp.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        bag.mImageViewContainer.setLayoutParams(lp);

        if (mUseThumbnailGenerator
                && file.isFile()
                && (file.getName().matches(MimeTypes._RegexFileTypeImages) || file.getName().matches(
                        MimeTypes._RegexFileTypeVideos))) {
            bag.mThumbnailIsProceessed = false;
            if (!mScrolling)
                updateThumbnail(file, bag);
        }

        // filename
        bag.mTxtFileName.setText(file.getName());
        // check if this file has been marked as to be deleted or not
        if (data.isTobeDeleted())
            bag.mTxtFileName.setPaintFlags(bag.mTxtFileName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        else
            bag.mTxtFileName.setPaintFlags(bag.mTxtFileName.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);

        // file info
        String time = null;
        try {
            time = new SimpleDateFormat(fileTimeShortFormat).format(file.lastModified());
        } catch (Exception e) {
            try {
                time = new SimpleDateFormat(_DefFileTimeShortFormat).format(file.lastModified());
            } catch (Exception ex) {
                time = new Date(file.lastModified()).toString();
            }
        }
        if (file.isDirectory())
            bag.mTxtFileInfo.setText(time);
        else {
            bag.mTxtFileInfo.setText(String.format("%s, %s", Converter.sizeToStr(file.length()), time));
        }

        // checkbox
        if (mMultiSelection) {
            if (mFilterMode == FilterMode.FilesOnly && file.isDirectory()) {
                bag.mCheckboxSelection.setVisibility(View.GONE);
            } else {
                bag.mCheckboxSelection.setVisibility(View.VISIBLE);
                bag.mCheckboxSelection.setFocusable(false);
                bag.mCheckboxSelection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        data.setSelected(isChecked);
                    }
                });

                bag.mCheckboxSelection.setOnLongClickListener(mCheckboxSelectionOnLongClickListener);

                bag.mCheckboxSelection.setChecked(data.isSelected());
            }
        } else
            bag.mCheckboxSelection.setVisibility(View.GONE);
    }// updateView

    /**
     * Updates thumbnail for {@code file}.
     * 
     * @param file
     *            {@link IFile}
     * @param bag
     *            {@link Bag}
     */
    private void updateThumbnail(IFile file, Bag bag) {
        if (bag.mThumbnailIsProceessed)
            return;

        ThumbnailGenerator generator = new ThumbnailGenerator(file, bag.mImageViewContainer, bag.mImageIcon,
                mThumbnailSize);
        mThumbnailGeneratorManager.addGenerator(generator);
        bag.mThumbnailIsProceessed = true;
    }// updateThumbnail()

    // =========
    // UTILITIES

    /**
     * Selects all items.
     * 
     * @param notifyDataSetChanged
     *            {@code true} if you want to notify that data set changed
     * @param filter
     *            {@link IFileFilter}
     */
    public void selectAll(boolean notifyDataSetChanged, IFileFilter filter) {
        for (int i = 0; i < getCount(); i++) {
            IFileDataModel item = getItem(i);
            item.setSelected(filter == null ? true : filter.accept(item.getFile()));
        }
        if (notifyDataSetChanged)
            notifyDataSetChanged();
    }// selectAll()

    /**
     * Selects no items.
     * 
     * @param notifyDataSetChanged
     *            {@code true} if you want to notify that data set changed
     */
    public void selectNone(boolean notifyDataSetChanged) {
        for (int i = 0; i < getCount(); i++)
            getItem(i).setSelected(false);
        if (notifyDataSetChanged)
            notifyDataSetChanged();
    }// selectNone()

    /**
     * Inverts selection.
     * 
     * @param notifyDataSetChanged
     *            {@code true} if you want to notify that data set changed
     */
    public void invertSelection(boolean notifyDataSetChanged) {
        for (int i = 0; i < getCount(); i++) {
            IFileDataModel item = getItem(i);
            item.setSelected(!item.isSelected());
        }
        if (notifyDataSetChanged)
            notifyDataSetChanged();
    }// invertSelection()

    // =========
    // LISTENERS

    private final View.OnLongClickListener mCheckboxSelectionOnLongClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(final View view) {
            ContextMenuUtils.showContextMenu(view.getContext(), 0, R.string.afc_title_advanced_selection,
                    mAdvancedSelectionOptions, new ContextMenuUtils.OnMenuItemClickListener() {

                        @Override
                        public void onClick(final int resId) {
                            new LoadingDialog(view.getContext(), R.string.afc_msg_loading, false) {

                                @Override
                                protected Object doInBackground(Void... arg0) {
                                    if (resId == R.string.afc_cmd_advanced_selection_all) {
                                        selectAll(false, null);
                                    } else if (resId == R.string.afc_cmd_advanced_selection_none) {
                                        selectNone(false);
                                    } else if (resId == R.string.afc_cmd_advanced_selection_invert) {
                                        invertSelection(false);
                                    } else if (resId == R.string.afc_cmd_select_all_files) {
                                        selectAll(false, new IFileFilter() {

                                            @Override
                                            public boolean accept(IFile pathname) {
                                                return pathname.isFile();
                                            }
                                        });
                                    } else if (resId == R.string.afc_cmd_select_all_folders) {
                                        selectAll(false, new IFileFilter() {

                                            @Override
                                            public boolean accept(IFile pathname) {
                                                return pathname.isDirectory();
                                            }
                                        });
                                    }

                                    return null;
                                }// doInBackground()

                                @Override
                                protected void onPostExecute(Object result) {
                                    super.onPostExecute(result);
                                    notifyDataSetChanged();
                                }// onPostExecute()
                            }.execute();// LoadingDialog
                        }// onClick()
                    });

            return true;
        }// onLongClick()
    };// mCheckboxSelectionOnLongClickListener

    // ===================
    // THUMBNAIL UTILITIES

    /**
     * A thread - which try to generate thumbnail for an {@lin IFile}.
     * 
     * @author Hai Bison
     * 
     */
    private static class ThumbnailGenerator extends Thread {

        private static final int _MsgUpdateImageView = 0;
        private static final int _MsgUpdateImageViewContainer = 1;

        private final IFile mFile;
        private final ViewGroup mImageViewContainer;
        private final ImageView mImageView;
        private final int mSize;
        private Bitmap mBitmap;

        private ThumbnailGeneratorListener mListener;

        private final Handler mHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mImageViewContainer.getLayoutParams();

                switch (msg.what) {
                case _MsgUpdateImageView:
                    mImageView.setImageBitmap(mBitmap);

                    lp.width = mSize;
                    lp.height = mSize;

                    break;// _MsgUpdateImageView
                case _MsgUpdateImageViewContainer:
                    lp.width = LinearLayout.LayoutParams.WRAP_CONTENT;
                    lp.height = LinearLayout.LayoutParams.WRAP_CONTENT;

                    break;// _MsgUpdateImageViewContainer
                }

                mImageViewContainer.setLayoutParams(lp);
            }// handleMessage()
        };// mHandler

        /**
         * Creates new instance of {@link ThumbnailGenerator}.
         * 
         * @param file
         *            {@link IFile}
         * @param imageViewContainer
         *            {@link ViewGroup}
         * @param imageView
         *            {@link ImageView}
         * @param size
         *            preferred dimension of the thumbnail to be generated.
         */
        public ThumbnailGenerator(IFile file, ViewGroup imageViewContainer, ImageView imageView, int size) {
            mFile = file;
            mImageViewContainer = imageViewContainer;
            mImageView = imageView;
            mSize = size;
        }// ThumbnailGenerator()

        /**
         * Sets a {@link ThumbnailGeneratorListener}.
         * 
         * @param listener
         *            {@link ThumbnailGeneratorListener}.
         */
        public void setListener(ThumbnailGeneratorListener listener) {
            mListener = listener;
        }

        @Override
        public void run() {
            mBitmap = mFile.genThumbnail(mSize, mSize);
            Log.d(_ClassName, String.format("Thumbnail for \"%s\" = %s", mFile.getName(), mBitmap));
            if (mBitmap != null) {
                if (isInterrupted()) {
                    mBitmap.recycle();
                    mHandler.sendEmptyMessage(_MsgUpdateImageViewContainer);
                } else {
                    mHandler.sendEmptyMessage(_MsgUpdateImageView);
                }
            }

            if (mListener != null)
                mListener.onFinished(this);
        }// run()
    }// ThumbnailGenerator

    /**
     * Listener for {@link ThumbnailGenerator}.
     * 
     * @author Hai Bison
     * 
     */
    private static interface ThumbnailGeneratorListener {

        /**
         * Will be called after the progress is done.
         * 
         * @param generator
         *            the generator, see {@link ThumbnailGenerator}.
         */
        void onFinished(ThumbnailGenerator generator);
    }// ThumbnailGeneratorListener

    /**
     * Manager for group of {@link ThumbnailGenerator}'s.
     * 
     * @author Hai Bison
     * 
     */
    private static class ThumbnailGeneratorManager {

        private final List<Thread> mGenerators;

        /**
         * Creates new instance of {@link ThumbnailGeneratorManager}.
         */
        public ThumbnailGeneratorManager() {
            mGenerators = new ArrayList<Thread>();
        }// ThumbnailGeneratorManager()

        private final ThumbnailGeneratorListener mThumbnailGeneratorListener = new ThumbnailGeneratorListener() {

            @Override
            public void onFinished(ThumbnailGenerator generator) {
                mGenerators.remove(generator);
            }// onFinished()
        };// mThumbnailGeneratorListener

        /**
         * Adds new {@code generator} to this group and start it.
         * 
         * @param generator
         *            {@link ThumbnailGenerator}.
         */
        public void addGenerator(ThumbnailGenerator generator) {
            generator.setListener(mThumbnailGeneratorListener);
            mGenerators.add(generator);
            generator.start();
        }// addGenerator()

        /**
         * Cancels all generators.
         */
        public void cancelAll() {
            for (Thread generator : mGenerators)
                generator.interrupt();
            mGenerators.clear();
        }// cancelAll()
    }// ThumbnailGeneratorManager
}
