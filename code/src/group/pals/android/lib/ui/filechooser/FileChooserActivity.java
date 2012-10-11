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
import group.pals.android.lib.ui.filechooser.prefs.DisplayPrefs;
import group.pals.android.lib.ui.filechooser.services.FileProviderService;
import group.pals.android.lib.ui.filechooser.services.IFileProvider;
import group.pals.android.lib.ui.filechooser.services.IFileProvider.FilterMode;
import group.pals.android.lib.ui.filechooser.services.IFileProvider.SortOrder;
import group.pals.android.lib.ui.filechooser.services.IFileProvider.SortType;
import group.pals.android.lib.ui.filechooser.services.LocalFileProvider;
import group.pals.android.lib.ui.filechooser.utils.ActivityCompat;
import group.pals.android.lib.ui.filechooser.utils.E;
import group.pals.android.lib.ui.filechooser.utils.FileComparator;
import group.pals.android.lib.ui.filechooser.utils.FileUtils;
import group.pals.android.lib.ui.filechooser.utils.Ui;
import group.pals.android.lib.ui.filechooser.utils.Utils;
import group.pals.android.lib.ui.filechooser.utils.history.History;
import group.pals.android.lib.ui.filechooser.utils.history.HistoryFilter;
import group.pals.android.lib.ui.filechooser.utils.history.HistoryListener;
import group.pals.android.lib.ui.filechooser.utils.history.HistoryStore;
import group.pals.android.lib.ui.filechooser.utils.ui.Dlg;
import group.pals.android.lib.ui.filechooser.utils.ui.LoadingDialog;
import group.pals.android.lib.ui.filechooser.utils.ui.TaskListener;
import group.pals.android.lib.ui.filechooser.utils.ui.ViewFilesContextMenuUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Main activity for this library.
 * 
 * @author Hai Bison
 * 
 */
public class FileChooserActivity extends Activity {

    /**
     * The full name of this class. Generally used for debugging.
     */
    public static final String _ClassName = FileChooserActivity.class.getName();

    /**
     * Types of view.
     * 
     * @author Hai Bison
     * @since v4.0 beta
     */
    public static enum ViewType {
        /**
         * Use {@link ListView} to display file list.
         */
        List,
        /**
         * Use {@link GridView} to display file list.
         */
        Grid
    }

    /*---------------------------------------------
     * KEYS
     */

    /**
     * Sets value of this key to a theme in {@code android.R.style.Theme_*}.<br>
     * Default is:<br>
     * 
     * <li>{@link android.R.style#Theme_DeviceDefault} for {@code SDK >= }
     * {@link Build.VERSION_CODES#ICE_CREAM_SANDWICH}</li>
     * 
     * <li>{@link android.R.style#Theme_Holo} for {@code SDK >= }
     * {@link Build.VERSION_CODES#HONEYCOMB}</li>
     * 
     * <li>{@link android.R.style#Theme} for older systems</li>
     * 
     * @since v4.3 beta
     */
    public static final String _Theme = _ClassName + ".theme";

    /**
     * Key to hold the root path.<br>
     * <br>
     * If {@link LocalFileProvider} is used, then default is sdcard, if sdcard
     * is not available, "/" will be used.<br>
     * <br>
     * <b>Note</b>: The value of this key is a {@link IFile}
     */
    public static final String _Rootpath = _ClassName + ".rootpath";

    /**
     * Key to hold the service class which implements {@link IFileProvider}.<br>
     * Default is {@link LocalFileProvider}
     */
    public static final String _FileProviderClass = _ClassName + ".file_provider_class";

    // ---------------------------------------------------------

    /**
     * Key to hold {@link IFileProvider.FilterMode}, default is
     * {@link IFileProvider.FilterMode#FilesOnly}.
     */
    public static final String _FilterMode = IFileProvider.FilterMode.class.getName();

    // flags

    // ---------------------------------------------------------

    /**
     * Key to hold max file count that's allowed to be listed, default =
     * {@code 1024}
     */
    public static final String _MaxFileCount = _ClassName + ".max_file_count";
    /**
     * Key to hold multi-selection mode, default = {@code false}
     */
    public static final String _MultiSelection = _ClassName + ".multi_selection";
    /**
     * Key to hold regex filename filter, default = {@code null}
     */
    public static final String _RegexFilenameFilter = _ClassName + ".regex_filename_filter";
    /**
     * Key to hold display-hidden-files, default = {@code false}
     */
    public static final String _DisplayHiddenFiles = _ClassName + ".display_hidden_files";
    /**
     * Sets this to {@code true} to enable double tapping to choose files/
     * directories. In older versions, double tapping is default. However, since
     * v4.7 beta, single tapping is default. So if you want to keep the old way,
     * please set this key to {@code true}.
     * 
     * @since v4.7 beta
     */
    public static final String _DoubleTapToChooseFiles = _ClassName + ".double_tap_to_choose_files";

    // ---------------------------------------------------------

    /**
     * Key to hold property save-dialog, default = {@code false}
     */
    public static final String _SaveDialog = _ClassName + ".save_dialog";
    /**
     * Key to hold default filename, default = {@code null}
     */
    public static final String _DefaultFilename = _ClassName + ".default_filename";
    /**
     * Key to hold results (can be one or multiple files)
     */
    public static final String _Results = _ClassName + ".results";

    /**
     * This key holds current location (an {@link IFile}), to restore it after
     * screen orientation changed
     */
    static final String _CurrentLocation = _ClassName + ".current_location";
    /**
     * This key holds current history (a {@link History}&lt;{@link IFile}&gt;),
     * to restore it after screen orientation changed
     */
    static final String _History = _ClassName + ".history";

    /**
     * This key holds current full history (a {@link History}&lt; {@link IFile}
     * &gt;), to restore it after screen orientation changed.
     */
    static final String _FullHistory = History.class.getName() + "_full";

    // ====================
    // "CONSTANT" VARIABLES

    private Class<?> mFileProviderServiceClass;
    /**
     * The file provider service.
     */
    private IFileProvider mFileProvider;
    /**
     * The service connection.
     */
    private ServiceConnection mServiceConnection;

    private IFile mRoot;
    private boolean mIsMultiSelection;
    private boolean mIsSaveDialog;
    private boolean mDoubleTapToChooseFiles;

    /**
     * The history.
     */
    private History<IFile> mHistory;

    /**
     * The full history, to store and show the users whatever they have been
     * gone to.
     */
    private History<IFile> mFullHistory;

    /**
     * The adapter of list view.
     */
    private IFileAdapter mFileAdapter;

    /*
     * controls
     */
    private HorizontalScrollView mViewLocationsContainer;
    private ViewGroup mViewLocations;
    private ViewGroup mViewFilesContainer;
    private TextView mTxtFullDirName;
    private AbsListView mViewFiles;
    private TextView mFooterView;
    private Button mBtnOk;
    private EditText mTxtSaveas;
    private ImageView mViewGoBack;
    private ImageView mViewGoForward;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        /*
         * THEME
         */

        if (getIntent().hasExtra(_Theme)) {
            int theme;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                theme = getIntent().getIntExtra(_Theme, android.R.style.Theme_DeviceDefault);
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                theme = getIntent().getIntExtra(_Theme, android.R.style.Theme_Holo);
            else
                theme = getIntent().getIntExtra(_Theme, android.R.style.Theme);
            setTheme(theme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.afc_file_chooser);

        initGestureDetector();

        // load configurations

        mFileProviderServiceClass = (Class<?>) getIntent().getSerializableExtra(_FileProviderClass);
        if (mFileProviderServiceClass == null)
            mFileProviderServiceClass = LocalFileProvider.class;

        mIsMultiSelection = getIntent().getBooleanExtra(_MultiSelection, false);

        mIsSaveDialog = getIntent().getBooleanExtra(_SaveDialog, false);
        if (mIsSaveDialog)
            mIsMultiSelection = false;

        mDoubleTapToChooseFiles = getIntent().getBooleanExtra(_DoubleTapToChooseFiles, false);

        // load controls

        mViewGoBack = (ImageView) findViewById(R.id.afc_filechooser_activity_button_go_back);
        mViewGoForward = (ImageView) findViewById(R.id.afc_filechooser_activity_button_go_forward);
        mViewLocations = (ViewGroup) findViewById(R.id.afc_filechooser_activity_view_locations);
        mViewLocationsContainer = (HorizontalScrollView) findViewById(R.id.afc_filechooser_activity_view_locations_container);
        mTxtFullDirName = (TextView) findViewById(R.id.afc_filechooser_activity_textview_full_dir_name);
        mViewFilesContainer = (ViewGroup) findViewById(R.id.afc_filechooser_activity_view_files_container);
        mFooterView = (TextView) findViewById(R.id.afc_filechooser_activity_view_files_footer_view);
        mTxtSaveas = (EditText) findViewById(R.id.afc_filechooser_activity_textview_saveas_filename);
        mBtnOk = (Button) findViewById(R.id.afc_filechooser_activity_button_ok);

        // history
        if (savedInstanceState != null && savedInstanceState.get(_History) instanceof HistoryStore<?>)
            mHistory = savedInstanceState.getParcelable(_History);
        else
            mHistory = new HistoryStore<IFile>(DisplayPrefs._DefHistoryCapacity);
        mHistory.addListener(new HistoryListener<IFile>() {

            @Override
            public void onChanged(History<IFile> history) {
                int idx = history.indexOf(getLocation());
                mViewGoBack.setEnabled(idx > 0);
                mViewGoForward.setEnabled(idx >= 0 && idx < history.size() - 1);
            }
        });

        // full history
        if (savedInstanceState != null && savedInstanceState.get(_FullHistory) instanceof HistoryStore<?>)
            mFullHistory = savedInstanceState.getParcelable(_FullHistory);
        else
            mFullHistory = new HistoryStore<IFile>(DisplayPrefs._DefHistoryCapacity) {

                @Override
                public void push(IFile newItem) {
                    int i = indexOf(newItem);
                    if (i >= 0) {
                        if (i == size() - 1)
                            return;
                        else
                            remove(newItem);
                    }
                    super.push(newItem);
                }// push()
            };

        // make sure RESULT_CANCELED is default
        setResult(RESULT_CANCELED);

        bindService(savedInstanceState);
    }// onCreate()

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.afc_file_chooser_activity, menu);
        return true;
    }// onCreateOptionsMenu()

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        /*
         * sorting
         */

        final boolean _sortAscending = DisplayPrefs.isSortAscending(this);
        MenuItem miSort = menu.findItem(R.id.afc_filechooser_activity_menuitem_sort);

        switch (DisplayPrefs.getSortType(this)) {
        case SortByName:
            miSort.setIcon(_sortAscending ? R.drawable.afc_ic_menu_sort_by_name_asc
                    : R.drawable.afc_ic_menu_sort_by_name_desc);
            break;
        case SortBySize:
            miSort.setIcon(_sortAscending ? R.drawable.afc_ic_menu_sort_by_size_asc
                    : R.drawable.afc_ic_menu_sort_by_size_desc);
            break;
        case SortByDate:
            miSort.setIcon(_sortAscending ? R.drawable.afc_ic_menu_sort_by_date_asc
                    : R.drawable.afc_ic_menu_sort_by_date_desc);
            break;
        }

        /*
         * view type
         */

        MenuItem menuItem = menu.findItem(R.id.afc_filechooser_activity_menuitem_switch_viewmode);
        switch (DisplayPrefs.getViewType(this)) {
        case Grid:
            menuItem.setIcon(R.drawable.afc_ic_menu_listview);
            menuItem.setTitle(R.string.afc_cmd_list_view);
            break;
        case List:
            menuItem.setIcon(R.drawable.afc_ic_menu_gridview);
            menuItem.setTitle(R.string.afc_cmd_grid_view);
            break;
        }

        return true;
    }// onPrepareOptionsMenu()

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getGroupId() == R.id.afc_filechooser_activity_menugroup_sorter) {
            doResortViewFiles();
        }// group_sorter
        else if (item.getItemId() == R.id.afc_filechooser_activity_menuitem_new_folder) {
            doCreateNewDir();
        } else if (item.getItemId() == R.id.afc_filechooser_activity_menuitem_switch_viewmode) {
            doSwitchViewType();
        } else if (item.getItemId() == R.id.afc_filechooser_activity_menuitem_home) {
            doGoHome();
        } else if (item.getItemId() == R.id.afc_filechooser_activity_menuitem_reload) {
            doReloadCurrentLocation();
        }

        return true;
    }// onOptionsItemSelected()

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }// onConfigurationChanged()

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(_CurrentLocation, getLocation());
        outState.putParcelable(_History, mHistory);
        outState.putParcelable(_FullHistory, mFullHistory);
    }// onSaveInstanceState()

    @Override
    protected void onStart() {
        super.onStart();
        if (!mIsMultiSelection && !mIsSaveDialog && mDoubleTapToChooseFiles)
            Dlg.toast(this, R.string.afc_hint_double_tap_to_select_file, Dlg._LengthShort);
    }// onStart()

    @Override
    protected void onPause() {
        super.onPause();
        if (DisplayPrefs.isRememberLastLocation(this) && getLocation() != null) {
            DisplayPrefs.setLastLocation(this, getLocation().getAbsolutePath());
        } else
            DisplayPrefs.setLastLocation(this, null);
    }// onPause()

    @Override
    protected void onDestroy() {
        if (mFileProvider != null) {
            try {
                unbindService(mServiceConnection);
            } catch (Throwable t) {
                /*
                 * due to this error:
                 * https://groups.google.com/d/topic/android-developers
                 * /Gv-80mQnyhc/discussion
                 */
                Log.e(_ClassName, "onDestroy() - unbindService() - exception: " + t);
            }

            try {
                stopService(new Intent(this, mFileProviderServiceClass));
            } catch (SecurityException e) {
                /*
                 * we have permission to stop our own service, so this exception
                 * should never be thrown
                 */
            }
        }

        super.onDestroy();
    }

    /**
     * Connects to file provider service, then loads root directory. If can not,
     * then finishes this activity with result code =
     * {@link Activity#RESULT_CANCELED}
     * 
     * @param savedInstanceState
     */
    private void bindService(final Bundle savedInstanceState) {
        if (startService(new Intent(this, mFileProviderServiceClass)) == null) {
            doShowCannotConnectToServiceAndFinish();
            return;
        }

        mServiceConnection = new ServiceConnection() {

            public void onServiceConnected(ComponentName className, IBinder service) {
                try {
                    mFileProvider = ((FileProviderService.LocalBinder) service).getService();
                } catch (Throwable t) {
                    Log.e(_ClassName, "mServiceConnection.onServiceConnected() -> " + t);
                }
            }// onServiceConnected()

            public void onServiceDisconnected(ComponentName className) {
                mFileProvider = null;
            }// onServiceDisconnected()
        };

        bindService(new Intent(this, mFileProviderServiceClass), mServiceConnection, Context.BIND_AUTO_CREATE);

        new LoadingDialog(this, R.string.afc_msg_loading, false) {

            private static final int _WaitTime = 200;
            private static final int _MaxWaitTime = 3000; // 3 seconds

            @Override
            protected Object doInBackground(Void... params) {
                int totalWaitTime = 0;
                while (mFileProvider == null) {
                    try {
                        totalWaitTime += _WaitTime;
                        Thread.sleep(_WaitTime);
                        if (totalWaitTime >= _MaxWaitTime)
                            break;
                    } catch (InterruptedException e) {
                        break;
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Object result) {
                super.onPostExecute(result);

                if (mFileProvider == null) {
                    doShowCannotConnectToServiceAndFinish();
                } else {
                    setupService();
                    setupHeader();
                    setupViewFiles();
                    setupFooter();

                    IFile path = savedInstanceState != null ? (IFile) savedInstanceState.get(_CurrentLocation) : null;
                    if (path == null && DisplayPrefs.isRememberLastLocation(FileChooserActivity.this)) {
                        String lastLocation = DisplayPrefs.getLastLocation(FileChooserActivity.this);
                        if (lastLocation != null)
                            path = mFileProvider.fromPath(lastLocation);
                    }

                    setLocation(path != null && path.isDirectory() ? path : mRoot, new TaskListener() {

                        @Override
                        public void onFinish(boolean ok, Object any) {
                            if (mRoot.equals(any)) {
                                mHistory.push(mRoot);
                                mFullHistory.push(mRoot);
                            } else
                                mHistory.notifyHistoryChanged();
                        }
                    });
                }
            }// onPostExecute()
        }.execute();// LoadingDialog
    }// bindService()

    /**
     * Setup the file provider:<br>
     * - filter mode;<br>
     * - display hidden files;<br>
     * - max file count;<br>
     * - ...
     */
    private void setupService() {
        /*
         * set root path, if not specified, try using
         * IFileProvider#defaultPath()
         */
        if (getIntent().getSerializableExtra(_Rootpath) != null)
            mRoot = (IFile) getIntent().getSerializableExtra(_Rootpath);
        if (mRoot == null || !mRoot.isDirectory())
            mRoot = mFileProvider.defaultPath();

        IFileProvider.FilterMode filterMode = (FilterMode) getIntent().getSerializableExtra(_FilterMode);
        if (filterMode == null)
            filterMode = IFileProvider.FilterMode.FilesOnly;

        IFileProvider.SortType sortType = DisplayPrefs.getSortType(this);
        boolean sortAscending = DisplayPrefs.isSortAscending(this);

        mFileProvider.setDisplayHiddenFiles(getIntent().getBooleanExtra(_DisplayHiddenFiles, false));
        mFileProvider.setFilterMode(mIsSaveDialog ? IFileProvider.FilterMode.FilesOnly : filterMode);
        mFileProvider.setMaxFileCount(getIntent().getIntExtra(_MaxFileCount, 1024));
        mFileProvider.setRegexFilenameFilter(mIsSaveDialog ? null : getIntent().getStringExtra(_RegexFilenameFilter));
        mFileProvider.setSortOrder(sortAscending ? IFileProvider.SortOrder.Ascending
                : IFileProvider.SortOrder.Descending);
        mFileProvider.setSortType(sortType);
    }// setupService()

    /**
     * Setup:<br>
     * - title of activity;<br>
     * - button go back;<br>
     * - button location;<br>
     * - button go forward;
     */
    private void setupHeader() {
        if (mIsSaveDialog) {
            setTitle(R.string.afc_title_save_as);
        } else {
            switch (mFileProvider.getFilterMode()) {
            case FilesOnly:
                setTitle(R.string.afc_title_choose_files);
                break;
            case FilesAndDirectories:
                setTitle(R.string.afc_title_choose_files_and_directories);
                break;
            case DirectoriesOnly:
                setTitle(R.string.afc_title_choose_directories);
                break;
            }
        }// title of activity

        mViewGoBack.setEnabled(false);
        mViewGoBack.setOnClickListener(mBtnGoBackOnClickListener);

        mViewGoForward.setEnabled(false);
        mViewGoForward.setOnClickListener(mBtnGoForwardOnClickListener);

        for (ImageView v : new ImageView[] { mViewGoBack, mViewGoForward })
            v.setOnLongClickListener(mBtnGoBackForwardOnLongClickListener);
    }// setupHeader()

    /**
     * Setup:<br>
     * - {@link #mViewFiles}<br>
     * - {@link #mViewFilesContainer}<br>
     * - {@link #mFileAdapter}
     */
    private void setupViewFiles() {
        switch (DisplayPrefs.getViewType(this)) {
        case Grid:
            mViewFiles = (AbsListView) getLayoutInflater().inflate(R.layout.afc_gridview_files, null);
            break;
        case List:
            mViewFiles = (AbsListView) getLayoutInflater().inflate(R.layout.afc_listview_files, null);
            break;
        }

        mViewFilesContainer.removeAllViews();
        mViewFilesContainer.addView(mViewFiles, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT, 1));

        mViewFiles.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mListviewFilesGestureDetector.onTouchEvent(event);
            }
        });

        createIFileAdapter();

        // no comments :-D
        mFooterView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                E.show(FileChooserActivity.this);
                return false;
            }
        });
    }// setupViewFiles()

    /**
     * Creates {@link IFileAdapter} and assign it to list view/ grid view of
     * files.
     */
    private void createIFileAdapter() {
        if (mFileAdapter != null)
            mFileAdapter.clear();

        mFileAdapter = new IFileAdapter(FileChooserActivity.this, new ArrayList<IFileDataModel>(),
                mFileProvider.getFilterMode(), mIsMultiSelection);
        /*
         * API 13+ does not recognize AbsListView.setAdapter(), so we cast it to
         * explicit class
         */
        if (mViewFiles instanceof ListView)
            ((ListView) mViewFiles).setAdapter(mFileAdapter);
        else
            ((GridView) mViewFiles).setAdapter(mFileAdapter);
    }// createIFileAdapter()

    /**
     * Setup:<br>
     * - button Cancel;<br>
     * - text field "save as" filename;<br>
     * - button Ok;
     */
    private void setupFooter() {
        // by default, view group footer and all its child views are hidden

        ViewGroup viewGroupFooterContainer = (ViewGroup) findViewById(R.id.afc_filechooser_activity_viewgroup_footer_container);
        ViewGroup viewGroupFooter = (ViewGroup) findViewById(R.id.afc_filechooser_activity_viewgroup_footer);

        if (mIsSaveDialog) {
            viewGroupFooterContainer.setVisibility(View.VISIBLE);
            viewGroupFooter.setVisibility(View.VISIBLE);

            mTxtSaveas.setVisibility(View.VISIBLE);
            mTxtSaveas.setText(getIntent().getStringExtra(_DefaultFilename));
            mTxtSaveas.setOnEditorActionListener(mTxtFilenameOnEditorActionListener);

            mBtnOk.setVisibility(View.VISIBLE);
            mBtnOk.setOnClickListener(mBtnOk_SaveDialog_OnClickListener);
            mBtnOk.setBackgroundResource(R.drawable.afc_selector_button_ok_saveas);

            int size = getResources().getDimensionPixelSize(R.dimen.afc_button_ok_saveas_size);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mBtnOk.getLayoutParams();
            lp.width = size;
            lp.height = size;
            mBtnOk.setLayoutParams(lp);
        }// this is in save mode
        else {
            if (mIsMultiSelection) {
                viewGroupFooterContainer.setVisibility(View.VISIBLE);
                viewGroupFooter.setVisibility(View.VISIBLE);

                ViewGroup.LayoutParams lp = viewGroupFooter.getLayoutParams();
                lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                viewGroupFooter.setLayoutParams(lp);

                mBtnOk.setMinWidth(getResources().getDimensionPixelSize(R.dimen.afc_single_button_min_width));
                mBtnOk.setText(android.R.string.ok);
                mBtnOk.setVisibility(View.VISIBLE);
                mBtnOk.setOnClickListener(mBtnOk_OpenDialog_OnClickListener);
            }
        }// this is in open mode
    }// setupFooter()

    private void doReloadCurrentLocation() {
        setLocation(getLocation(), null);
    }// doReloadCurrentLocation()

    private void doShowCannotConnectToServiceAndFinish() {
        Dlg.showError(FileChooserActivity.this, R.string.afc_msg_cannot_connect_to_file_provider_service,
                new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                });
    }// doShowCannotConnectToServiceAndFinish()

    private void doGoHome() {
        // TODO explain why?
        goTo(mRoot.clone());
    }// doGoHome()

    private static final int[] _BtnSortIds = { R.id.afc_settings_sort_view_button_sort_by_name_asc,
            R.id.afc_settings_sort_view_button_sort_by_name_desc, R.id.afc_settings_sort_view_button_sort_by_size_asc,
            R.id.afc_settings_sort_view_button_sort_by_size_desc, R.id.afc_settings_sort_view_button_sort_by_date_asc,
            R.id.afc_settings_sort_view_button_sort_by_date_desc };

    /**
     * Show a dialog for sorting options and resort file list after user
     * selected an option.
     */
    private void doResortViewFiles() {
        final AlertDialog _dialog = Dlg.newDlg(this);

        // get the index of button of current sort type
        int btnCurrentSortTypeIdx = 0;
        switch (DisplayPrefs.getSortType(this)) {
        case SortByName:
            btnCurrentSortTypeIdx = 0;
            break;
        case SortBySize:
            btnCurrentSortTypeIdx = 2;
            break;
        case SortByDate:
            btnCurrentSortTypeIdx = 4;
            break;
        }
        if (!DisplayPrefs.isSortAscending(this))
            btnCurrentSortTypeIdx++;

        View.OnClickListener listener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                _dialog.dismiss();

                Context c = FileChooserActivity.this;

                if (v.getId() == R.id.afc_settings_sort_view_button_sort_by_name_asc) {
                    DisplayPrefs.setSortType(c, SortType.SortByName);
                    DisplayPrefs.setSortAscending(c, true);
                } else if (v.getId() == R.id.afc_settings_sort_view_button_sort_by_name_desc) {
                    DisplayPrefs.setSortType(c, SortType.SortByName);
                    DisplayPrefs.setSortAscending(c, false);
                } else if (v.getId() == R.id.afc_settings_sort_view_button_sort_by_size_asc) {
                    DisplayPrefs.setSortType(c, SortType.SortBySize);
                    DisplayPrefs.setSortAscending(c, true);
                } else if (v.getId() == R.id.afc_settings_sort_view_button_sort_by_size_desc) {
                    DisplayPrefs.setSortType(c, SortType.SortBySize);
                    DisplayPrefs.setSortAscending(c, false);
                } else if (v.getId() == R.id.afc_settings_sort_view_button_sort_by_date_asc) {
                    DisplayPrefs.setSortType(c, SortType.SortByDate);
                    DisplayPrefs.setSortAscending(c, true);
                } else if (v.getId() == R.id.afc_settings_sort_view_button_sort_by_date_desc) {
                    DisplayPrefs.setSortType(c, SortType.SortByDate);
                    DisplayPrefs.setSortAscending(c, false);
                }

                resortViewFiles();
            }// onClick()
        };// listener

        View view = getLayoutInflater().inflate(R.layout.afc_settings_sort_view, null);
        for (int i = 0; i < _BtnSortIds.length; i++) {
            Button btn = (Button) view.findViewById(_BtnSortIds[i]);
            btn.setOnClickListener(listener);
            if (i == btnCurrentSortTypeIdx) {
                btn.setEnabled(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    btn.setText(R.string.afc_ellipsize);
            }
        }

        _dialog.setTitle(R.string.afc_title_sort_by);
        _dialog.setView(view);

        _dialog.show();
    }// doResortViewFiles()

    /**
     * Resort view files.
     */
    private void resortViewFiles() {
        if (mFileProvider.getSortType().equals(DisplayPrefs.getSortType(this))
                && mFileProvider.getSortOrder().isAsc() == (DisplayPrefs.isSortAscending(this)))
            return;

        /*
         * Re-sort the listview by re-loading current location; NOTE: re-sort
         * the adapter does not repaint the listview, even if we call
         * notifyDataSetChanged(), invalidateViews()...
         */
        mFileProvider.setSortType(DisplayPrefs.getSortType(this));
        mFileProvider.setSortOrder(DisplayPrefs.isSortAscending(this) ? SortOrder.Ascending : SortOrder.Descending);
        doReloadCurrentLocation();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            ActivityCompat.invalidateOptionsMenu(this);
    }// resortViewFiles()

    /**
     * Switch view type between {@link ViewType#List} and {@link ViewType#Grid}
     */
    private void doSwitchViewType() {
        new LoadingDialog(this, R.string.afc_msg_loading, false) {

            @Override
            protected void onPreExecute() {
                // call this first, to let the parent prepare the dialog
                super.onPreExecute();

                switch (DisplayPrefs.getViewType(FileChooserActivity.this)) {
                case Grid:
                    DisplayPrefs.setViewType(FileChooserActivity.this, ViewType.List);
                    break;
                case List:
                    DisplayPrefs.setViewType(FileChooserActivity.this, ViewType.Grid);
                    break;
                }

                setupViewFiles();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                    ActivityCompat.invalidateOptionsMenu(FileChooserActivity.this);

                doReloadCurrentLocation();
            }// onPreExecute()

            @Override
            protected Object doInBackground(Void... params) {
                // do nothing :-)
                return null;
            }// doInBackground()
        }.execute();
    }// doSwitchViewType()

    /**
     * Confirms user to create new directory.
     */
    private void doCreateNewDir() {
        if (mFileProvider instanceof LocalFileProvider
                && !Utils.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Dlg.toast(this, R.string.afc_msg_app_doesnot_have_permission_to_create_files, Dlg._LengthShort);
            return;
        }

        final AlertDialog _dlg = Dlg.newDlg(this);

        View view = getLayoutInflater().inflate(R.layout.afc_simple_text_input_view, null);
        final EditText _textFile = (EditText) view.findViewById(R.id.afc_simple_text_input_view_text1);
        _textFile.setHint(R.string.afc_hint_folder_name);
        _textFile.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Ui.hideSoftKeyboard(FileChooserActivity.this, _textFile.getWindowToken());
                    _dlg.getButton(DialogInterface.BUTTON_POSITIVE).performClick();
                    return true;
                }
                return false;
            }
        });

        _dlg.setView(view);
        _dlg.setTitle(R.string.afc_cmd_new_folder);
        _dlg.setIcon(android.R.drawable.ic_menu_add);
        _dlg.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = _textFile.getText().toString().trim();
                        if (!FileUtils.isFilenameValid(name)) {
                            Dlg.toast(FileChooserActivity.this, getString(R.string.afc_pmsg_filename_is_invalid, name),
                                    Dlg._LengthShort);
                            return;
                        }

                        IFile dir = mFileProvider.fromPath(String
                                .format("%s/%s", getLocation().getAbsolutePath(), name));
                        if (dir.mkdir()) {
                            Dlg.toast(FileChooserActivity.this, getString(R.string.afc_msg_done), Dlg._LengthShort);
                            setLocation(getLocation(), null);
                        } else
                            Dlg.toast(FileChooserActivity.this,
                                    getString(R.string.afc_pmsg_cannot_create_folder, name), Dlg._LengthShort);
                    }// onClick()
                });
        _dlg.show();

        final Button _btnOk = _dlg.getButton(DialogInterface.BUTTON_POSITIVE);
        _btnOk.setEnabled(false);

        _textFile.addTextChangedListener(new TextWatcher() {

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
                _btnOk.setEnabled(FileUtils.isFilenameValid(s.toString().trim()));
            }
        });
    }// doCreateNewDir()

    /**
     * Updates UI that {@code data} will not be deleted.
     * 
     * @param data
     *            {@link IFileDataModel}
     */
    private void notifyDataModelNotDeleted(IFileDataModel data) {
        data.setTobeDeleted(false);
        mFileAdapter.notifyDataSetChanged();
    }// notifyDataModelNotDeleted(()

    /**
     * Deletes a file.
     * 
     * @param file
     *            {@link IFile}
     */
    private void doDeleteFile(final IFileDataModel data) {
        if (mFileProvider instanceof LocalFileProvider
                && !Utils.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            notifyDataModelNotDeleted(data);
            Dlg.toast(this, R.string.afc_msg_app_doesnot_have_permission_to_delete_files, Dlg._LengthShort);
            return;
        }

        Dlg.confirmYesno(
                this,
                getString(R.string.afc_pmsg_confirm_delete_file, data.getFile().isFile() ? getString(R.string.afc_file)
                        : getString(R.string.afc_folder), data.getFile().getName()),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new LoadingDialog(FileChooserActivity.this, getString(R.string.afc_pmsg_deleting_file, data
                                .getFile().isFile() ? getString(R.string.afc_file) : getString(R.string.afc_folder),
                                data.getFile().getName()), true) {

                            private Thread mThread = FileUtils.createDeleteFileThread(data.getFile(), mFileProvider,
                                    true);
                            private final boolean _isFile = data.getFile().isFile();

                            private void notifyFileDeleted() {
                                mFileAdapter.remove(data);
                                mFileAdapter.notifyDataSetChanged();

                                refreshHistories();
                                // TODO remove all duplicate history items

                                Dlg.toast(
                                        FileChooserActivity.this,
                                        getString(
                                                R.string.afc_pmsg_file_has_been_deleted,
                                                _isFile ? getString(R.string.afc_file) : getString(R.string.afc_folder),
                                                data.getFile().getName()), Dlg._LengthShort);
                            }// notifyFileDeleted()

                            @Override
                            protected void onPreExecute() {
                                super.onPreExecute();
                                mThread.start();
                            }// onPreExecute()

                            @Override
                            protected Object doInBackground(Void... arg0) {
                                while (mThread.isAlive()) {
                                    try {
                                        mThread.join(DisplayPrefs._DelayTimeWaitingThreads);
                                    } catch (InterruptedException e) {
                                        mThread.interrupt();
                                    }
                                }
                                return null;
                            }// doInBackground()

                            @Override
                            protected void onCancelled() {
                                mThread.interrupt();

                                if (data.getFile().exists()) {
                                    notifyDataModelNotDeleted(data);
                                    Dlg.toast(FileChooserActivity.this, R.string.afc_msg_cancelled, Dlg._LengthShort);
                                } else
                                    notifyFileDeleted();

                                super.onCancelled();
                            }// onCancelled()

                            @Override
                            protected void onPostExecute(Object result) {
                                super.onPostExecute(result);

                                if (data.getFile().exists()) {
                                    notifyDataModelNotDeleted(data);
                                    Dlg.toast(
                                            FileChooserActivity.this,
                                            getString(R.string.afc_pmsg_cannot_delete_file,
                                                    data.getFile().isFile() ? getString(R.string.afc_file)
                                                            : getString(R.string.afc_folder), data.getFile().getName()),
                                            Dlg._LengthShort);
                                } else
                                    notifyFileDeleted();
                            }// onPostExecute()
                        }.execute();// LoadingDialog
                    }// onClick()
                }, new DialogInterface.OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        notifyDataModelNotDeleted(data);
                    }// onCancel()
                });
    }// doDeleteFile()

    /**
     * As the name means.
     * 
     * @param filename
     * @since v1.91
     */
    private void doCheckSaveasFilenameAndFinish(String filename) {
        if (filename.length() == 0) {
            Dlg.toast(this, R.string.afc_msg_filename_is_empty, Dlg._LengthShort);
        } else {
            final IFile _file = mFileProvider.fromPath(getLocation().getAbsolutePath() + File.separator + filename);

            if (!FileUtils.isFilenameValid(filename)) {
                Dlg.toast(this, getString(R.string.afc_pmsg_filename_is_invalid, filename), Dlg._LengthShort);
            } else if (_file.isFile()) {
                Dlg.confirmYesno(FileChooserActivity.this,
                        getString(R.string.afc_pmsg_confirm_replace_file, _file.getName()),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                doFinish(_file);
                            }
                        });
            } else if (_file.isDirectory()) {
                Dlg.toast(this, getString(R.string.afc_pmsg_filename_is_directory, _file.getName()), Dlg._LengthShort);
            } else
                doFinish(_file);
        }
    }// doCheckSaveasFilenameAndFinish()

    /**
     * Gets current location.
     * 
     * @return current location, can be {@code null}.
     */
    private IFile getLocation() {
        return (IFile) mViewLocations.getTag();
    }// getLocation()

    /**
     * Sets current location.
     * 
     * @param path
     *            the path
     * @param listener
     *            {@link TaskListener}: the second parameter {@code any} in
     *            {@link TaskListener#onFinish(boolean, Object)} will be
     *            {@code path}.
     */
    private void setLocation(final IFile path, final TaskListener listener) {
        new LoadingDialog(this, R.string.afc_msg_loading, true) {

            // IFile[] files = new IFile[0];
            List<IFile> files;
            boolean hasMoreFiles = false;
            int shouldBeSelectedIdx = -1;
            /**
             * Used to focus last directory on list view.
             */
            String mLastPath = getLocation() != null ? getLocation().getAbsolutePath() : null;

            @Override
            protected Object doInBackground(Void... params) {
                try {
                    if (path.isDirectory() && path.canRead()) {
                        files = new ArrayList<IFile>();
                        mFileProvider.listAllFiles(path, new IFileFilter() {

                            @Override
                            public boolean accept(IFile pathname) {
                                if (mFileProvider.accept(pathname)) {
                                    if (files.size() < mFileProvider.getMaxFileCount())
                                        files.add(pathname);
                                    else
                                        hasMoreFiles = true;
                                }
                                return false;
                            }// accept()
                        });
                    } else
                        files = null;

                    if (files != null) {
                        Collections.sort(files,
                                new FileComparator(mFileProvider.getSortType(), mFileProvider.getSortOrder()));
                        if (mLastPath != null && mLastPath.length() >= path.getAbsolutePath().length()) {
                            for (int i = 0; i < files.size(); i++) {
                                IFile f = files.get(i);
                                if (f.isDirectory() && mLastPath.startsWith(f.getAbsolutePath())) {
                                    shouldBeSelectedIdx = i;
                                    break;
                                }
                            }
                        }
                    }// if files != null
                } catch (Throwable t) {
                    setLastException(t);
                    cancel(false);
                }
                return null;
            }// doInBackground()

            @Override
            protected void onCancelled() {
                super.onCancelled();
                Dlg.toast(FileChooserActivity.this, R.string.afc_msg_cancelled, Dlg._LengthShort);
            }// onCancelled()

            @Override
            protected void onPostExecute(Object result) {
                super.onPostExecute(result);

                if (files == null) {
                    Dlg.toast(FileChooserActivity.this, getString(R.string.afc_pmsg_cannot_access_dir, path.getName()),
                            Dlg._LengthShort);
                    if (listener != null)
                        listener.onFinish(false, path);
                    return;
                }

                // update list view

                createIFileAdapter();
                for (IFile f : files)
                    mFileAdapter.add(new IFileDataModel(f));
                mFileAdapter.notifyDataSetChanged();

                // update footers

                mFooterView.setVisibility(hasMoreFiles || mFileAdapter.isEmpty() ? View.VISIBLE : View.GONE);
                if (hasMoreFiles)
                    mFooterView.setText(getString(R.string.afc_pmsg_max_file_count_allowed,
                            mFileProvider.getMaxFileCount()));
                else if (mFileAdapter.isEmpty())
                    mFooterView.setText(R.string.afc_msg_empty);

                if (shouldBeSelectedIdx >= 0 && shouldBeSelectedIdx < mFileAdapter.getCount())
                    mViewFiles.setSelection(shouldBeSelectedIdx);
                else if (!mFileAdapter.isEmpty())
                    mViewFiles.setSelection(0);

                /*
                 * navigation buttons
                 */
                createLocationButtons(path);

                if (listener != null)
                    listener.onFinish(true, path);
            }// onPostExecute()
        }.execute();// new LoadingDialog()
    }// setLocation()

    /**
     * Goes to a specified location.
     * 
     * @param dir
     *            a directory, of course.
     * @return {@code true} if {@code dir} <b><i>can</i></b> be browsed to.
     * @since v4.3 beta
     */
    private boolean goTo(final IFile dir) {
        if (dir.equalsToPath(getLocation()))
            return false;

        setLocation(dir, new TaskListener() {

            IFile mLastPath = getLocation();

            @Override
            public void onFinish(boolean ok, Object any) {
                if (ok) {
                    mHistory.truncateAfter(mLastPath);
                    mHistory.push(dir);
                    mFullHistory.push(dir);
                }
            }
        });
        return true;
    }// goTo()

    private void createLocationButtons(IFile path) {
        mViewLocations.setTag(path);
        mViewLocations.removeAllViews();

        LinearLayout.LayoutParams lpBtnLoc = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lpBtnLoc.gravity = Gravity.CENTER;
        LinearLayout.LayoutParams lpDivider = null;
        LayoutInflater inflater = getLayoutInflater();
        final int _dim = getResources().getDimensionPixelSize(R.dimen.afc_5dp);
        int count = 0;
        while (path != null) {
            TextView btnLoc = (TextView) inflater.inflate(R.layout.afc_button_location, null);
            btnLoc.setText(path.parentFile() != null ? path.getName() : getString(R.string.afc_root));
            btnLoc.setTag(path);
            btnLoc.setOnClickListener(mBtnLocationOnClickListener);
            btnLoc.setOnLongClickListener(mBtnLocationOnLongClickListener);
            mViewLocations.addView(btnLoc, 0, lpBtnLoc);

            if (count++ == 0) {
                Rect r = new Rect();
                btnLoc.getPaint().getTextBounds(path.getName(), 0, path.getName().length(), r);
                if (r.width() >= getResources().getDimensionPixelSize(R.dimen.afc_button_location_max_width)
                        - btnLoc.getPaddingLeft() - btnLoc.getPaddingRight()) {
                    mTxtFullDirName.setText(path.getName());
                    mTxtFullDirName.setVisibility(View.VISIBLE);
                } else
                    mTxtFullDirName.setVisibility(View.GONE);
            }

            path = path.parentFile();
            if (path != null) {
                View divider = inflater.inflate(R.layout.afc_view_locations_divider, null);

                if (lpDivider == null) {
                    lpDivider = new LinearLayout.LayoutParams(_dim, _dim);
                    lpDivider.gravity = Gravity.CENTER;
                    lpDivider.setMargins(_dim, _dim, _dim, _dim);
                }
                mViewLocations.addView(divider, 0, lpDivider);
            }
        }

        mViewLocationsContainer.postDelayed(new Runnable() {

            public void run() {
                mViewLocationsContainer.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        }, 100);
    }// createLocationButtons()

    /**
     * Refreshes all the histories. This removes invalid items (which are not
     * existed anymore).
     */
    private void refreshHistories() {
        HistoryFilter<IFile> historyFilter = new HistoryFilter<IFile>() {

            @Override
            public boolean accept(IFile item) {
                return !item.isDirectory();
            }
        };

        mHistory.removeAll(historyFilter);
        mFullHistory.removeAll(historyFilter);
    }// refreshHistories()

    /**
     * Finishes this activity.
     * 
     * @param files
     *            list of {@link IFile}
     */
    private void doFinish(IFile... files) {
        List<IFile> list = new ArrayList<IFile>();
        for (IFile f : files)
            list.add(f);
        doFinish((ArrayList<IFile>) list);
    }

    /**
     * Finishes this activity.
     * 
     * @param files
     *            list of {@link IFile}
     */
    private void doFinish(ArrayList<IFile> files) {
        if (files == null || files.isEmpty()) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        Intent intent = new Intent();

        // set results
        intent.putExtra(_Results, files);

        // return flags for further use (in case the caller needs)
        intent.putExtra(_FilterMode, mFileProvider.getFilterMode());
        intent.putExtra(_SaveDialog, mIsSaveDialog);

        setResult(RESULT_OK, intent);
        finish();
    }// doFinish()

    /**********************************************************
     * BUTTON LISTENERS
     */

    private final View.OnClickListener mBtnGoBackOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            /*
             * if user deleted a dir which was one in history, then maybe there
             * are duplicates, so we check and remove them here
             */
            IFile currentLoc = getLocation();
            IFile preLoc = null;
            while (currentLoc.equalsToPath(preLoc = mHistory.prevOf(currentLoc)))
                mHistory.remove(preLoc);

            if (preLoc != null) {
                setLocation(preLoc, new TaskListener() {

                    @Override
                    public void onFinish(boolean ok, Object any) {
                        if (ok) {
                            mViewGoBack.setEnabled(mHistory.prevOf(getLocation()) != null);
                            mViewGoForward.setEnabled(true);
                            mFullHistory.push((IFile) any);
                        }
                    }
                });
            } else {
                mViewGoBack.setEnabled(false);
            }
        }
    };// mBtnGoBackOnClickListener

    private final View.OnClickListener mBtnLocationOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (v.getTag() instanceof IFile)
                goTo((IFile) v.getTag());
        }
    };// mBtnLocationOnClickListener

    private final View.OnLongClickListener mBtnLocationOnLongClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            if (IFileProvider.FilterMode.FilesOnly.equals(mFileProvider.getFilterMode()) || mIsSaveDialog)
                return false;

            doFinish((IFile) v.getTag());

            return false;
        }

    };// mBtnLocationOnLongClickListener

    private final View.OnClickListener mBtnGoForwardOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            /*
             * if user deleted a dir which was one in history, then maybe there
             * are duplicates, so we check and remove them here
             */
            IFile currentLoc = getLocation();
            IFile nextLoc = null;
            while (currentLoc.equalsToPath(nextLoc = mHistory.nextOf(currentLoc)))
                mHistory.remove(nextLoc);

            if (nextLoc != null) {
                setLocation(nextLoc, new TaskListener() {

                    @Override
                    public void onFinish(boolean ok, Object any) {
                        if (ok) {
                            mViewGoBack.setEnabled(true);
                            mViewGoForward.setEnabled(mHistory.nextOf(getLocation()) != null);
                            mFullHistory.push((IFile) any);
                        }
                    }
                });
            } else {
                mViewGoForward.setEnabled(false);
            }
        }
    };// mBtnGoForwardOnClickListener

    private final View.OnLongClickListener mBtnGoBackForwardOnLongClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            ViewFilesContextMenuUtils.doShowHistoryContents(FileChooserActivity.this, mFileProvider, mFullHistory,
                    getLocation(), new TaskListener() {

                        @Override
                        public void onFinish(boolean ok, Object any) {
                            mHistory.removeAll(new HistoryFilter<IFile>() {

                                @Override
                                public boolean accept(IFile item) {
                                    return mFullHistory.indexOf(item) < 0;
                                }
                            });

                            if (any instanceof IFile) {
                                setLocation((IFile) any, new TaskListener() {

                                    @Override
                                    public void onFinish(boolean ok, Object any) {
                                        if (ok)
                                            mHistory.notifyHistoryChanged();
                                    }
                                });
                            } else if (mHistory.isEmpty()) {
                                mHistory.push(getLocation());
                                mFullHistory.push(getLocation());
                            }
                        }// onFinish()
                    });
            return false;
        }// onLongClick()
    };// mBtnGoBackForwardOnLongClickListener

    private final TextView.OnEditorActionListener mTxtFilenameOnEditorActionListener = new TextView.OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Ui.hideSoftKeyboard(FileChooserActivity.this, mTxtSaveas.getWindowToken());
                mBtnOk.performClick();
                return true;
            }
            return false;
        }
    };// mTxtFilenameOnEditorActionListener

    private final View.OnClickListener mBtnOk_SaveDialog_OnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Ui.hideSoftKeyboard(FileChooserActivity.this, mTxtSaveas.getWindowToken());
            String filename = mTxtSaveas.getText().toString().trim();
            doCheckSaveasFilenameAndFinish(filename);
        }
    };// mBtnOk_SaveDialog_OnClickListener

    private final View.OnClickListener mBtnOk_OpenDialog_OnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            List<IFile> list = new ArrayList<IFile>();
            for (int i = 0; i < mViewFiles.getAdapter().getCount(); i++) {
                // NOTE: header and footer don't have data
                Object obj = mViewFiles.getAdapter().getItem(i);
                if (obj instanceof IFileDataModel) {
                    IFileDataModel dm = (IFileDataModel) obj;
                    if (dm.isSelected())
                        list.add(dm.getFile());
                }
            }
            doFinish((ArrayList<IFile>) list);
        }
    };// mBtnOk_OpenDialog_OnClickListener

    private GestureDetector mListviewFilesGestureDetector;

    private void initGestureDetector() {
        mListviewFilesGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {

            private Object getData(float x, float y) {
                int i = getSubViewId(x, y);
                if (i >= 0)
                    return mViewFiles.getItemAtPosition(mViewFiles.getFirstVisiblePosition() + i);
                return null;
            }// getSubView()

            /**
             * Gets {@link IFileDataModel} from {@code e}.
             * 
             * @param e
             *            {@link MotionEvent}.
             * @return the data model, or {@code null} if not available.
             */
            private IFileDataModel getDataModel(MotionEvent e) {
                Object o = getData(e.getX(), e.getY());
                return o instanceof IFileDataModel ? (IFileDataModel) o : null;
            }// getDataModel()

            private int getSubViewId(float x, float y) {
                Rect r = new Rect();
                for (int i = 0; i < mViewFiles.getChildCount(); i++) {
                    mViewFiles.getChildAt(i).getHitRect(r);
                    if (r.contains((int) x, (int) y))
                        return i;
                }

                return -1;
            }// getSubViewId()

            @Override
            public void onLongPress(MotionEvent e) {
                IFileDataModel data = getDataModel(e);
                if (data == null)
                    return;

                if (mDoubleTapToChooseFiles) {
                    // do nothing
                }// double tap to choose files
                else {
                    if (!mIsSaveDialog
                            && !mIsMultiSelection
                            && data.getFile().isDirectory()
                            && (IFileProvider.FilterMode.DirectoriesOnly.equals(mFileProvider.getFilterMode()) || IFileProvider.FilterMode.FilesAndDirectories
                                    .equals(mFileProvider.getFilterMode())))
                        doFinish(data.getFile());
                }// single tap to choose files
            }// onLongPress()

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                IFileDataModel data = getDataModel(e);
                if (data == null)
                    return false;

                if (data.getFile().isDirectory()) {
                    goTo(data.getFile());
                    return true;
                }

                if (mIsSaveDialog)
                    mTxtSaveas.setText(data.getFile().getName());

                if (mDoubleTapToChooseFiles) {
                    // do nothing
                    return false;
                }// double tap to choose files
                else {
                    if (mIsMultiSelection)
                        return false;

                    if (mIsSaveDialog)
                        doCheckSaveasFilenameAndFinish(data.getFile().getName());
                    else
                        doFinish(data.getFile());

                    return true;
                }// single tap to choose files
            }// onSingleTapConfirmed()

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (mDoubleTapToChooseFiles) {
                    if (mIsMultiSelection)
                        return false;

                    IFileDataModel data = getDataModel(e);
                    if (data == null)
                        return false;

                    if (data.getFile().isDirectory()
                            && IFileProvider.FilterMode.FilesOnly.equals(mFileProvider.getFilterMode()))
                        return false;

                    // if mFilterMode == DirectoriesOnly, files won't be
                    // shown

                    if (mIsSaveDialog) {
                        if (data.getFile().isFile()) {
                            mTxtSaveas.setText(data.getFile().getName());
                            doCheckSaveasFilenameAndFinish(data.getFile().getName());
                        } else
                            return false;
                    } else
                        doFinish(data.getFile());
                }// double tap to choose files
                else {
                    // do nothing
                    return false;
                }// single tap to choose files

                return true;
            }// onDoubleTap()

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                final int _max_y_distance = 19;// 10 is too short :-D
                final int _min_x_distance = 80;
                final int _min_x_velocity = 200;
                if (Math.abs(e1.getY() - e2.getY()) < _max_y_distance
                        && Math.abs(e1.getX() - e2.getX()) > _min_x_distance && Math.abs(velocityX) > _min_x_velocity) {
                    Object o = getData(e1.getX(), e1.getY());
                    if (o instanceof IFileDataModel) {
                        ((IFileDataModel) o).setTobeDeleted(true);
                        mFileAdapter.notifyDataSetChanged();
                        doDeleteFile((IFileDataModel) o);
                        return true;
                    }
                }

                return false;
            }// onFling()
        });// mListviewFilesGestureDetector
    }// initGestureDetector()
}