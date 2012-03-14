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

import group.pals.android.lib.ui.filechooser.bean.FileContainer;
import group.pals.android.lib.ui.filechooser.utils.E;
import group.pals.android.lib.ui.filechooser.utils.FileComparator;
import group.pals.android.lib.ui.filechooser.utils.History;
import group.pals.android.lib.ui.filechooser.utils.HistoryStore;
import group.pals.android.lib.ui.filechooser.utils.UI;
import group.pals.android.lib.ui.filechooser.utils.Utils;
import group.pals.android.lib.ui.filechooser.utils.ui.LoadingDialog;
import group.pals.android.lib.ui.filechooser.utils.ui.TaskListener;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Main activity for this library.
 * 
 * @author Hai Bison
 * 
 */
public class FileChooserActivity extends Activity {

    /*---------------------------------------------
     * KEYS
     */

    /**
     * Key to hold the root path, default is sdcard, if sdcard is not available,
     * "/" will be used
     */
    public static final String Rootpath = "rootpath";

    // ---------------------------------------------------------

    /**
     * Key to hold selection mode, default = {@link #FilesOnly}.<br>
     * Acceptable values:<br>
     * - {@link #FilesOnly}<br>
     * - {@link #DirectoriesOnly}<br>
     * - {@link #FilesAndDirectories}
     */
    public static final String SelectionMode = "selection_mode";

    // flags

    /**
     * User can choose files only
     */
    public static final int FilesOnly = 0;
    /**
     * User can choose directories only
     */
    public static final int DirectoriesOnly = 1;
    /**
     * User can choose files or directories
     */
    public static final int FilesAndDirectories = 2;

    // ---------------------------------------------------------

    /**
     * Key to hold max file count that's allowed to be listed, default =
     * {@code 1024}
     */
    public static final String MaxFileCount = "max-file-count";
    /**
     * Key to hold multi-selection mode, default = {@code false}
     */
    public static final String MultiSelection = "multi_selection";
    /**
     * Key to hold regex filename filter, default = {@code null}
     */
    public static final String RegexFilenameFilter = "regex_filename_filter";
    /**
     * Key to hold display-hidden-files, default = {@code false}
     */
    public static final String DisplayHiddenFiles = "display_hidden_files";

    // ---------------------------------------------------------

    /**
     * Key to hold sort type, default = {@link #SortByName}.<br>
     * Acceptable values:<br>
     * - {@link #SortByName}<br>
     * - {@link #SortBySize}<br>
     * - {@link #SortByDate}
     */
    public static final String SortType = "sort-type";

    // flags

    /**
     * Sort by name, (directories first, case-insensitive)
     */
    public static final int SortByName = 0;
    /**
     * Sort by size (directories first)
     */
    public static final int SortBySize = 1;
    /**
     * Sort by date (directories first)
     */
    public static final int SortByDate = 2;

    // ---------------------------------------------------------

    /**
     * Key to hold sort order, default = {@link #Ascending}.<br>
     * Acceptable values:<br>
     * - {@link #Ascending}<br>
     * - {@link #Descending}
     */
    public static final String SortOrder = "sort-order";

    // flags

    /**
     * Sort ascending.
     */
    public static final int Ascending = 0;
    /**
     * Sort descending.
     */
    public static final int Descending = 1;

    // ---------------------------------------------------------

    /**
     * Key to hold property save-dialog, default = {@code false}
     */
    public static final String SaveDialog = "save_dialog";
    /**
     * Key to hold default filename, default = {@code null}
     */
    public static final String DefaultFilename = "default_filename";
    /**
     * Key to hold results (can be one or multiple files)
     */
    public static final String Results = "results";

    /*
     * "constant" variables
     */

    /**
     * Used to store preferences. Currently it just stores {@link #SortType} and
     * {@link #SortOrder}
     * 
     * @since v2.0 alpha
     */
    private SharedPreferences fPrefs;
    private File fRoot;
    private int fSelectionMode;
    private int fMaxFileCount;
    private boolean fMultiSelection;
    private String fRegexFilenameFilter;
    private boolean fDisplayHiddenFiles;
    private boolean fSaveDialog;
    private History<FileContainer> fHistory;

    /*
     * controls
     */
    private Button btnLocation;
    private ListView listviewFiles;
    private Button btnOk;
    private Button btnCancel;
    private EditText txtSaveasFilename;
    private ImageButton btnGoBack;
    private ImageButton btnGoForward;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_chooser);

        loadPreferences();

        fSelectionMode = getIntent().getIntExtra(SelectionMode, FilesOnly);
        fMaxFileCount = getIntent().getIntExtra(MaxFileCount, 1024);

        /*
         * set root path, if not specified, try using sdcard, if sdcard is not
         * available, use "/"
         */
        if (getIntent().getStringExtra(Rootpath) == null)
            fRoot = Environment.getExternalStorageDirectory();
        else
            fRoot = new File(getIntent().getStringExtra(Rootpath));
        if (fRoot == null || !fRoot.isDirectory())
            fRoot = new File("/");

        fMultiSelection = getIntent().getBooleanExtra(MultiSelection, false);
        fRegexFilenameFilter = getIntent().getStringExtra(RegexFilenameFilter);
        fDisplayHiddenFiles = getIntent().getBooleanExtra(DisplayHiddenFiles,
                false);

        fSaveDialog = getIntent().getBooleanExtra(SaveDialog, false);
        if (fSaveDialog) {
            fSelectionMode = FilesOnly;
            fMultiSelection = false;
            fRegexFilenameFilter = null;
        }

        btnGoBack = (ImageButton) findViewById(R.id.button_go_back);
        btnGoForward = (ImageButton) findViewById(R.id.button_go_forward);
        btnLocation = (Button) findViewById(R.id.button_location);
        listviewFiles = (ListView) findViewById(R.id.listview_files);
        txtSaveasFilename = (EditText) findViewById(R.id.text_view_saveas_filename);
        btnOk = (Button) findViewById(R.id.button_ok);
        btnCancel = (Button) findViewById(R.id.button_cancel);

        fHistory = new HistoryStore<FileContainer>(0);

        setupHeader();
        setupListviewFiles();
        setupFooter();
        setLocation(fRoot, new TaskListener() {

            @Override
            public void onFinish(boolean ok, Object any) {
                fHistory.push(getLocation(), getLocation());
            }
        });
    }// onCreate

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.file_chooser_activity, menu);
        return true;
    }// onCreateOptionsMenu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO: split into multiple methods
        if (item.getGroupId() == R.id.menugroup_sorter) {
            final int fLastSortType = fPrefs.getInt(
                    FileChooserActivity.SortType, SortByName);
            final boolean fLastSortAscending = fPrefs.getInt(
                    FileChooserActivity.SortOrder, Ascending) == Ascending;
            Editor editor = fPrefs.edit();

            if (item.getItemId() == R.id.menuitem_sort_by_name) {
                if (fLastSortType == SortByName)
                    editor.putInt(SortOrder, fLastSortAscending ? Descending
                            : Ascending);
                else {
                    editor.putInt(FileChooserActivity.SortType, SortByName);
                    editor.putInt(SortOrder, Ascending);
                }
            } else if (item.getItemId() == R.id.menuitem_sort_by_size) {
                if (fLastSortType == SortBySize)
                    editor.putInt(SortOrder, fLastSortAscending ? Descending
                            : Ascending);
                else {
                    editor.putInt(FileChooserActivity.SortType, SortBySize);
                    editor.putInt(SortOrder, Ascending);
                }
            } else if (item.getItemId() == R.id.menuitem_sort_by_date) {
                if (fLastSortType == SortByDate)
                    editor.putInt(SortOrder, fLastSortAscending ? Descending
                            : Ascending);
                else {
                    editor.putInt(FileChooserActivity.SortType, SortByDate);
                    editor.putInt(SortOrder, Ascending);
                }
            }

            editor.commit();

            /*
             * Re-sort the listview by re-loading current location; NOTE:
             * re-sort the adapter does not repaint the listview, even if we
             * call notifyDataSetChanged(), invalidateViews()...
             */
            setLocation(getLocation(), null);
        }// group_sorter

        return true;
    }// onOptionsItemSelected

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        /*
         * sorting
         */

        // clear all icons
        final int[] fSorterIds = { R.id.menuitem_sort_by_name,
                R.id.menuitem_sort_by_size, R.id.menuitem_sort_by_date };
        for (int id : fSorterIds)
            menu.findItem(id).setIcon(0);

        final int fSortType = fPrefs.getInt(FileChooserActivity.SortType,
                SortByName);
        final boolean fSortAscending = fPrefs.getInt(
                FileChooserActivity.SortOrder, Ascending) == Ascending;

        switch (fSortType) {
        case SortByName:
            menu.findItem(R.id.menuitem_sort_by_name).setIcon(
                    fSortAscending ? R.drawable.ic_menu_sort_up
                            : R.drawable.ic_menu_sort_down);
            break;
        case SortBySize:
            menu.findItem(R.id.menuitem_sort_by_size).setIcon(
                    fSortAscending ? R.drawable.ic_menu_sort_up
                            : R.drawable.ic_menu_sort_down);
            break;
        case SortByDate:
            menu.findItem(R.id.menuitem_sort_by_date).setIcon(
                    fSortAscending ? R.drawable.ic_menu_sort_up
                            : R.drawable.ic_menu_sort_down);
            break;
        }

        return true;
    }// onPrepareOptionsMenu

    @Override
    protected void onStart() {
        super.onStart();
        if (!fMultiSelection && !fSaveDialog)
            Toast.makeText(this, R.string.hint_long_click_to_select_files,
                    Toast.LENGTH_SHORT).show();
    }// onStart

    /**
     * Loads preferences.
     */
    private void loadPreferences() {
        fPrefs = getSharedPreferences(
                FileChooserActivity.class.getName(), 0);

        Editor editor = fPrefs.edit();

        /*
         * sort
         */

        if (getIntent().hasExtra(SortType))
            editor.putInt(SortType,
                    getIntent().getIntExtra(SortType, SortByName));
        else if (!fPrefs.contains(SortType))
            editor.putInt(SortType, SortByName);

        if (getIntent().hasExtra(SortOrder))
            editor.putInt(SortOrder,
                    getIntent().getIntExtra(SortOrder, Ascending));
        else if (!fPrefs.contains(SortOrder))
            editor.putInt(SortOrder, Ascending);

        editor.commit();
    }// loadPreferences

    /**
     * Setup:<br>
     * - title of activity;<br>
     * - button go back;<br>
     * - button location;<br>
     * - button go forward;
     */
    private void setupHeader() {
        if (fSaveDialog) {
            setTitle(R.string.title_save_as);
        } else {
            switch (fSelectionMode) {
            case FilesOnly:
                setTitle(R.string.title_choose_files);
                break;
            case FilesAndDirectories:
                setTitle(R.string.title_choose_files_and_directories);
                break;
            case DirectoriesOnly:
                setTitle(R.string.title_choose_directories);
                break;
            }
        }// title of activity

        // single click to change path
        btnLocation.setOnClickListener(fBtnLocationOnClickListener);
        // long click to select current directory
        btnLocation.setOnLongClickListener(fBtnLocationOnLongClickListener);

        btnGoBack.setEnabled(false);
        btnGoBack.setOnClickListener(fBtnGoBackOnClickListener);

        btnGoForward.setEnabled(false);
        btnGoForward.setOnClickListener(fBtnGoForwardOnClickListener);
    }// setupHeader()

    /**
     * As the name means :-)
     */
    private void setupListviewFiles() {
        listviewFiles.setFooterDividersEnabled(true);
        // single click to open directory (if the item is directory)
        listviewFiles.setOnItemClickListener(fListviewFilesOnItemClickListener);
        // long click to select item (if this is single mode)
        listviewFiles
                .setOnItemLongClickListener(fListviewFilesOnItemLongClickListener);
    }// setupListviewFiles()

    /**
     * Setup:<br>
     * - button Cancel;<br>
     * - text field "save as" filename;<br>
     * - button Ok;
     */
    private void setupFooter() {
        btnCancel.setOnClickListener(fBtnCancelOnClickListener);

        if (fSaveDialog) {
            txtSaveasFilename.setText(getIntent().getStringExtra(
                    DefaultFilename));
            txtSaveasFilename
                    .setOnEditorActionListener(fTxtFilenameOnEditorActionListener);
            btnOk.setOnClickListener(fBtnOk_SaveDialog_OnClickListener);
        } else {// this is in open mode
            txtSaveasFilename.setVisibility(View.GONE);

            if (fMultiSelection)
                btnOk.setOnClickListener(fBtnOk_OpenDialog_OnClickListener);
            else
                btnOk.setVisibility(View.GONE);
        }// if fSaveDialog...
    }// setupFooter()

    /**
     * As the name means.
     * 
     * @param filename
     * @since v1.91
     */
    private void checkSaveasFilenameAndFinish(String filename) {
        if (filename.length() == 0) {
            Toast.makeText(FileChooserActivity.this,
                    R.string.msg_filename_is_empty, Toast.LENGTH_SHORT).show();
        } else {
            final File fFile = new File(getLocation().getFile()
                    .getAbsolutePath() + File.separator + filename);

            if (!Utils.isFilenameValid(filename)) {
                Toast.makeText(
                        FileChooserActivity.this,
                        String.format(
                                getString(R.string.pmsg_filename_is_invalid),
                                filename), Toast.LENGTH_SHORT).show();
            } else if (fFile.isFile()) {
                new AlertDialog.Builder(FileChooserActivity.this)
                        .setMessage(
                                String.format(
                                        getString(R.string.pmsg_confirm_replace_file),
                                        fFile.getName()))
                        .setPositiveButton(R.string.cmd_cancel, null)
                        .setNeutralButton(R.string.cmd_ok,
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                            int which) {
                                        doFinish(fFile);
                                    }
                                }).show();
            } else if (fFile.isDirectory()) {
                Toast.makeText(
                        FileChooserActivity.this,
                        String.format(
                                getString(R.string.pmsg_filename_is_directory),
                                fFile.getName()), Toast.LENGTH_SHORT).show();
            } else
                doFinish(fFile);
        }
    }// checkSaveasFilenameAndFinish()

    /**
     * Gets current location.
     * 
     * @return current location.
     */
    private FileContainer getLocation() {
        return (FileContainer) btnLocation.getTag();
    }// getLocation()

    /**
     * Sets current location
     * 
     * @param path
     *            the path
     * @param listener
     *            {@link TaskListener}
     */
    private void setLocation(File path, TaskListener listener) {
        setLocation(new FileContainer(path), listener);
    }// setLocation()

    /**
     * Sets current location
     * 
     * @param fPath
     *            the path
     * @param fListener
     *            {@link TaskListener}
     */
    private void setLocation(final FileContainer fPath,
            final TaskListener fListener) {
        // TODO: let the user to be able to cancel the task
        new LoadingDialog(this, R.string.msg_loading, false) {

            File[] files = new File[0];
            boolean hasMoreFiles = false;

            @Override
            protected Object doInBackground(Void... params) {
                files = listFiles(fPath.getFile(), new TaskListener() {

                    @Override
                    public void onFinish(boolean ok, Object any) {
                        hasMoreFiles = ok;
                    }
                });
                
                return null;
            }// doInBackground()

            @Override
            protected void onPostExecute(Object result) {
                super.onPostExecute(result);

                if (files == null) {
                    Toast.makeText(
                            FileChooserActivity.this,
                            String.format(
                                    getString(R.string.pmsg_cannot_access_dir),
                                    fPath.getFile().getName()),
                            Toast.LENGTH_SHORT).show();
                    if (fListener != null)
                        fListener.onFinish(false, null);
                    return;
                }

                /*
                 * add footer if has more files, NOTE: do this before setting
                 * adapter to list view
                 */
                updateListviewFilesFooter(hasMoreFiles);

                /*
                 * add files to list view
                 */
                List<DataModel> list = new ArrayList<DataModel>();
                for (File f : files)
                    list.add(new DataModel(f));
                listviewFiles.setAdapter(new FileAdapter(
                        FileChooserActivity.this, list, fSelectionMode,
                        fMultiSelection));

                /*
                 * navigation buttons
                 */

                if (fPath.getFile().getParentFile() != null
                        && fPath.getFile().getParentFile().getParentFile() != null)
                    btnLocation.setText("../" + fPath.getFile().getName());
                else
                    btnLocation.setText(fPath.getFile().getAbsolutePath());
                btnLocation.setTag(fPath);

                int idx = fHistory.indexOf(fPath);
                btnGoBack.setEnabled(idx > 0);
                btnGoForward.setEnabled(idx >= 0 && idx < fHistory.size() - 2);

                if (fListener != null)
                    fListener.onFinish(true, null);
            }// onPostExecute()
        }.execute((Void[]) null);// new LoadingDialog()
    }// setLocation()

    /**
     * As the name means.<br>
     * <b>Note:</b> Do this before changing listview's adapter, or error will
     * occur. See {@link ListView#addFooterView(View)}.
     * 
     * @param hasMoreFiles
     *            - if {@code true}, add a footer showing that there are more
     *            files but can not be shown;<br>
     *            - if {@code false}, remove any footer;
     */
    private void updateListviewFilesFooter(boolean hasMoreFiles) {
        if (hasMoreFiles) {
            View footer = null;
            if (listviewFiles.getTag() instanceof View) {
                footer = (View) listviewFiles.getTag();
            } else {
                LayoutInflater layoutInflater = FileChooserActivity.this
                        .getLayoutInflater();
                footer = layoutInflater.inflate(R.layout.listview_files_footer,
                        null);
                listviewFiles.setTag(footer);
                listviewFiles.addFooterView(footer);
            }

            footer.setEnabled(false);
            TextView txt = (TextView) footer
                    .findViewById(R.id.text_view_msg_hasmorefiles);
            txt.setText(String.format(
                    getString(R.string.pmsg_max_file_count_allowed),
                    fMaxFileCount));
        } else {
            listviewFiles.removeFooterView((View) listviewFiles.getTag());
            listviewFiles.setTag(null);
        }
    }// updateListviewFilesFooter

    /**
     * Lists files inside {@code dir}
     * 
     * @param dir
     *            the root directory which needs to list files
     * @param listener
     *            {@link TaskListener} used to notify caller:<br>
     *            - {@link TaskListener#onFinish(boolean, Object)} ->
     *            {@code boolean} will be {@code true} if file count exceeds max
     *            file count allowed, {@code false} otherwise
     * @return list of files, or {@code null} if an exception occurs, see
     *         {@link File#listFiles()}
     * @since v1.8
     */
    private File[] listFiles(File dir, TaskListener listener) {
        /*
         * if total files exceeds max file count allowed, fHasMoreFiles[0] is
         * true, otherwise false
         */
        final boolean[] fHasMoreFiles = { false };

        try {
            File[] files = dir.listFiles(new FileFilter() {

                int fileCount = 0;

                @Override
                public boolean accept(File pathname) {
                    if (!fDisplayHiddenFiles
                            && pathname.getName().startsWith("."))
                        return false;
                    if (fileCount >= fMaxFileCount) {
                        fHasMoreFiles[0] = true;
                        return false;
                    }

                    switch (fSelectionMode) {
                    case FilesOnly:
                        if (fRegexFilenameFilter != null && pathname.isFile())
                            return pathname.getName().matches(
                                    fRegexFilenameFilter);

                        fileCount++;
                        return true;
                    case DirectoriesOnly:
                        boolean ok = pathname.isDirectory();
                        if (ok)
                            fileCount++;
                        return ok;
                    default:
                        if (fRegexFilenameFilter != null && pathname.isFile())
                            return pathname.getName().matches(
                                    fRegexFilenameFilter);

                        fileCount++;
                        return true;
                    }
                }
            });// dir.listFiles()

            if (files != null)
                Arrays.sort(files,
                        new FileComparator(fPrefs.getInt(SortType, SortByName),
                                fPrefs.getInt(SortOrder, Ascending)));

            if (listener != null)
                listener.onFinish(fHasMoreFiles[0], null);

            return files;
        } catch (Exception e) {
            return null;
        }
    }// listFiles()

    /**
     * Finishes this activity.
     * 
     * @param files
     *            list of {@link File}
     */
    private void doFinish(File... files) {
        List<File> list = new ArrayList<File>();
        for (File f : files)
            list.add(f);
        doFinish((ArrayList<File>) list);
    }

    /**
     * Finishes this activity.
     * 
     * @param files
     *            list of {@link File}
     */
    private void doFinish(ArrayList<File> files) {
        Intent intent = new Intent();

        // set results
        intent.putExtra(Results, files);

        // return flags for further use (in case the caller needs)
        intent.putExtra(SelectionMode, fSelectionMode);
        intent.putExtra(SaveDialog, fSaveDialog);

        setResult(RESULT_OK, intent);

        finish();
    }

    /**********************************************************
     * BUTTON LISTENERS
     */

    private final View.OnClickListener fBtnGoBackOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            FileContainer path = fHistory.prevOf(getLocation());
            if (path != null) {
                setLocation(path, new TaskListener() {

                    @Override
                    public void onFinish(boolean ok, Object any) {
                        if (ok) {
                            btnGoBack.setEnabled(fHistory.prevOf(getLocation()) != null);
                            btnGoForward.setEnabled(true);
                        }
                    }
                });
            } else {
                btnGoBack.setEnabled(false);
            }
        }
    };// fBtnGoBackOnClickListener

    private final View.OnClickListener fBtnLocationOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (getLocation().getFile().getParentFile() != null) {
                final FileContainer fLastPath = getLocation();
                setLocation(getLocation().getFile().getParentFile(),
                        new TaskListener() {

                            @Override
                            public void onFinish(boolean ok, Object any) {
                                if (ok) {
                                    fHistory.push(fLastPath, getLocation());
                                    btnGoBack.setEnabled(true);
                                    btnGoForward.setEnabled(false);
                                }
                            }
                        });// setLocation()
            }
        }
    };// fBtnLocationOnClickListener

    private final View.OnLongClickListener fBtnLocationOnLongClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
            if (fMultiSelection || fSelectionMode == FilesOnly || fSaveDialog)
                return false;

            doFinish(getLocation().getFile());

            return false;
        }

    };// fBtnLocationOnLongClickListener

    private final View.OnClickListener fBtnGoForwardOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            FileContainer path = fHistory.nextOf(getLocation());
            if (path != null) {
                setLocation(path, new TaskListener() {

                    @Override
                    public void onFinish(boolean ok, Object any) {
                        if (ok) {
                            btnGoBack.setEnabled(true);
                            btnGoForward.setEnabled(fHistory
                                    .nextOf(getLocation()) != null);
                        }
                    }
                });
            } else {
                btnGoForward.setEnabled(false);
            }
        }
    };// fBtnGoForwardOnClickListener

    private final View.OnClickListener fBtnCancelOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // make sure RESULT_CANCELED is returned
            setResult(RESULT_CANCELED);
            finish();
        }
    };// fBtnCancelOnClickListener

    private final TextView.OnEditorActionListener fTxtFilenameOnEditorActionListener = new TextView.OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                UI.hideSoftKeyboard(FileChooserActivity.this,
                        txtSaveasFilename.getWindowToken());
                btnOk.performClick();
                return true;
            }
            return false;
        }
    };// fTxtFilenameOnEditorActionListener

    private final View.OnClickListener fBtnOk_SaveDialog_OnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            UI.hideSoftKeyboard(FileChooserActivity.this,
                    txtSaveasFilename.getWindowToken());
            String filename = txtSaveasFilename.getText().toString().trim();
            checkSaveasFilenameAndFinish(filename);
        }
    };// fBtnOk_SaveDialog_OnClickListener

    private final View.OnClickListener fBtnOk_OpenDialog_OnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            List<File> list = new ArrayList<File>();
            for (int i = 0; i < listviewFiles.getAdapter().getCount(); i++) {
                // NOTE: header and footer don't have data
                Object obj = listviewFiles.getAdapter().getItem(i);
                if (obj instanceof DataModel) {
                    DataModel dm = (DataModel) obj;
                    if (dm.isSelected())
                        list.add(dm.getFile());
                }
            }
            doFinish((ArrayList<File>) list);
        }
    };// fBtnOk_OpenDialog_OnClickListener

    private final AdapterView.OnItemClickListener fListviewFilesOnItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> av, View v, int position, long id) {
            if (!(av.getItemAtPosition(position) instanceof DataModel))
                return;

            DataModel data = (DataModel) av.getItemAtPosition(position);
            if (data.getFile().isDirectory()) {
                final FileContainer fLastPath = getLocation();
                setLocation(data.getFile(), new TaskListener() {

                    @Override
                    public void onFinish(boolean ok, Object any) {
                        if (ok) {
                            fHistory.push(fLastPath, getLocation());
                            btnGoBack.setEnabled(true);
                            btnGoForward.setEnabled(false);
                        }
                    }
                });
            } else {
                if (fSaveDialog)
                    txtSaveasFilename.setText(data.getFile().getName());
            }
        }
    };// fListviewFilesOnItemClickListener

    private final AdapterView.OnItemLongClickListener fListviewFilesOnItemLongClickListener = new AdapterView.OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> av, View v, int position,
                long id) {
            if (fMultiSelection)
                return false;
            if (!(av.getItemAtPosition(position) instanceof DataModel)) {
                // no comments :-D
                E.show(FileChooserActivity.this);
                return false;
            }

            DataModel data = (DataModel) av.getItemAtPosition(position);

            if (data.getFile().isDirectory() && fSelectionMode == FilesOnly)
                return false;

            // if fSelectionMode == DirectoriesOnly, files won't be
            // shown

            doFinish(data.getFile());
            return false;
        }
    };// fListviewFilesOnItemLongClickListener
}