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
import group.pals.android.lib.ui.filechooser.utils.HistoryPath;
import group.pals.android.lib.ui.filechooser.utils.UI;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
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
 * @author Hai Bison
 *
 */
public class FileChooserActivity extends Activity {

  /**
   * Key to hold the root path
   */
  public static final String Rootpath = "rootpath";
  /**
   * Key to hold selection mode
   */
  public static final String SelectionMode = "selection_mode";
  /**
   * Key to hold multi-selection mode
   */
  public static final String MultiSelection = "multi_selection";
  /**
   * Key to hole regex filename filter
   */
  public static final String RegexFilenameFilter = "regex_filename_filter";
  /**
   * Key to hold display-hidden-files
   */
  public static final String DisplayHiddenFiles = "display_hidden_files";
  /**
   * Key to hold property save-dialog
   */
  public static final String SaveDialog = "save_dialog";
  /**
   * Key to hold default filename
   */
  public static final String DefaultFilename = "default_filename";

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

  /**
   * Key to hold results (can be one or multiple files)
   */
  public static final String Results = "results";

  /*
   * "constant" variables
   */
  private File root;
  private int selectionMode;
  private boolean multiSelection;
  private String regexFilenameFilter;
  private boolean displayHiddenFiles;
  private boolean saveDialog;

  /*
   * variables
   */
  private HistoryPath history;

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

    selectionMode = getIntent().getIntExtra(SelectionMode, FilesOnly);
    root = new File(getIntent().getStringExtra(Rootpath) != null ?
        getIntent().getStringExtra(Rootpath) : "/");
    if (!root.isDirectory())
      root = new File("/");

    multiSelection = getIntent().getBooleanExtra(MultiSelection, false);
    regexFilenameFilter = getIntent().getStringExtra(RegexFilenameFilter);
    displayHiddenFiles = getIntent().getBooleanExtra(DisplayHiddenFiles, false);
    saveDialog = getIntent().getBooleanExtra(SaveDialog, false);
    if (saveDialog) {
      selectionMode = FilesOnly;
      multiSelection = false;
      regexFilenameFilter = null;
    }

    btnGoBack = (ImageButton) findViewById(R.id.button_go_back);
    btnGoForward = (ImageButton) findViewById(R.id.button_go_forward);
    btnLocation = (Button) findViewById(R.id.button_location);
    listviewFiles = (ListView) findViewById(R.id.listview_files);
    txtSaveasFilename = (EditText) findViewById(R.id.text_view_saveas_filename);
    btnOk = (Button) findViewById(R.id.button_ok);
    btnCancel = (Button) findViewById(R.id.button_cancel);

    history = new HistoryPath(0);

    setupHeader();
    setupListviewFiles();
    setupFooter();
    setLocation(root);
    history.push(getLocation(), getLocation());
  }

  @Override
  protected void onStart () {
    super.onStart();
    if (!multiSelection && !saveDialog)
      Toast.makeText(this, R.string.hint_long_click_to_select_files, Toast.LENGTH_SHORT).show();
  }

  private void setupHeader() {
    if (saveDialog) {
      setTitle(R.string.title_save_as);
    } else {
      switch (selectionMode) {
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
    }

    //single click to change path
    btnLocation.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        if (getLocation().getFile().getParentFile() != null) {
          FileContainer lastPath = getLocation();
          if (setLocation(getLocation().getFile().getParentFile())) {
            history.push(lastPath, getLocation());
            btnGoBack.setEnabled(true);
            btnGoForward.setEnabled(false);
          }
        }
      }
    });//click

    //long click to select current directory
    btnLocation.setOnLongClickListener(new View.OnLongClickListener() {

      @Override
      public boolean onLongClick(View v) {
        if (multiSelection || selectionMode == FilesOnly || saveDialog)
          return false;

        doFinish(getLocation().getFile());

        return false;
      }
    });//longClick

    btnGoBack.setEnabled(false);
    btnGoBack.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        FileContainer path = history.getPrev(getLocation());
        if (path != null) {
          if (setLocation(path)) {
            btnGoBack.setEnabled(history.getPrev(getLocation()) != null);
            btnGoForward.setEnabled(true);
          }
        } else {
          btnGoBack.setEnabled(false);
        }
      }
    });

    btnGoForward.setEnabled(false);
    btnGoForward.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        FileContainer path = history.getNext(getLocation());
        if (path != null) {
          if (setLocation(path)) {
            btnGoBack.setEnabled(true);
            btnGoForward.setEnabled(history.getNext(getLocation()) != null);
          }
        } else {
          btnGoForward.setEnabled(false);
        }
      }
    });
  }

  private void setupFooter() {
    btnCancel.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        //make sure RESULT_CANCELED is returned
        setResult(RESULT_CANCELED);
        finish();
      }
    });

    if (saveDialog) {
      txtSaveasFilename.setText(getIntent().getStringExtra(DefaultFilename));
      txtSaveasFilename.setOnEditorActionListener(new TextView.OnEditorActionListener() {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
          if (actionId == EditorInfo.IME_ACTION_DONE) {
            UI.hideSoftKeyboard(FileChooserActivity.this, txtSaveasFilename.getWindowToken());
            btnOk.performClick();
            return true;
          }
          return false;
        }
      });

      btnOk.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View v) {
          UI.hideSoftKeyboard(FileChooserActivity.this, txtSaveasFilename.getWindowToken());

          String filename = txtSaveasFilename.getText().toString().trim();
          if (filename.length() == 0) {
            Toast.makeText(FileChooserActivity.this, R.string.msg_filename_is_empty,
                Toast.LENGTH_SHORT).show();
          } else {
            final File F = new File(getLocation().getFile().getAbsolutePath() + "/" + filename);
            if (F.isFile()) {
              new AlertDialog.Builder(FileChooserActivity.this)
                .setMessage(String.format(
                    getString(R.string.pmsg_confirm_replace_file), F.getName()))
                .setPositiveButton(R.string.cmd_cancel, null)
                .setNeutralButton(R.string.cmd_ok, new DialogInterface.OnClickListener(){

                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                    doFinish(F);
                  }})
                .show();
            } else if (F.isDirectory()) {
              Toast.makeText(FileChooserActivity.this,
                  String.format(getString(R.string.pmsg_filename_is_directory), F.getName()),
                  Toast.LENGTH_SHORT).show();
            } else
              doFinish(F);
          }
        }
      });
    } else {
      txtSaveasFilename.setVisibility(View.GONE);

      if (multiSelection)
        btnOk.setOnClickListener(new View.OnClickListener() {
          
          @Override
          public void onClick(View v) {
            List<File> list = new ArrayList<File>();
            for (int i = 0; i < listviewFiles.getAdapter().getCount(); i++) {
              DataModel dm = (DataModel) listviewFiles.getAdapter().getItem(i);
              if (dm.isSelected())
                list.add(dm.getFile());
            }
            doFinish((ArrayList<File>) list);
          }
        });
      else
        btnOk.setVisibility(View.GONE);
    }
  }

  private FileContainer getLocation() {
    return (FileContainer) btnLocation.getTag();
  }

  private boolean setLocation(File path) {
    return setLocation(new FileContainer(path));
  }

  /**
   * Sets current location
   * @param path the path
   * @return {@code true} if we can access the path given, {@code false} otherwise
   */
  private boolean setLocation(FileContainer path) {
    File[] files;
    try {
      files = path.getFile().listFiles(new FileFilter() {

        @Override
        public boolean accept(File pathname) {
          if (!displayHiddenFiles && pathname.getName().startsWith("."))
            return false;

          switch (selectionMode) {
            case FilesOnly:
              if (regexFilenameFilter != null && pathname.isFile())
                return pathname.getName().matches(regexFilenameFilter);
              return true;
            case DirectoriesOnly:
              return pathname.isDirectory();
            default:
              if (regexFilenameFilter != null && pathname.isFile())
                return pathname.getName().matches(regexFilenameFilter);
              return true;
          }
        }
      });
      if (files == null)
        throw new Exception();
    } catch (Exception e) {
      Toast.makeText(this,
          String.format(getString(R.string.pmsg_cannot_access_dir), path.getFile().getName()),
          Toast.LENGTH_SHORT).show();
      return false;
    }

    //sort file list
    Arrays.sort(files, new Comparator<File>() {

      @Override
      public int compare(File lhs, File rhs) {
        if ((lhs.isDirectory() && rhs.isDirectory()) ||
            (lhs.isFile() && rhs.isFile()))
          return lhs.getName().compareToIgnoreCase(rhs.getName());

        if (lhs.isDirectory())
          return -1;
        else
          return 1;
      }
    });

    //add files to list view
    List<DataModel> list = new ArrayList<DataModel>();
    for (File f : files)
      list.add(new DataModel(f));
    listviewFiles.setAdapter(new FilesAdapter(this, list, selectionMode, multiSelection));

    if (path.getFile().getParentFile() != null &&
        path.getFile().getParentFile().getParentFile() != null)
      btnLocation.setText("../" + path.getFile().getName());
    else
      btnLocation.setText(path.getFile().getAbsolutePath());
    btnLocation.setTag(path);

    int idx = history.indexOf(path);
    btnGoBack.setEnabled(idx > 0);
    btnGoForward.setEnabled(idx >= 0 && idx < history.size() - 2);

    return true;
  }

  private void setupListviewFiles() {
    listviewFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> av, View v, int position, long id) {
        DataModel data = (DataModel) av.getItemAtPosition(position);
        if (data.getFile().isDirectory()) {
          FileContainer lastPath = getLocation();
          if (setLocation(data.getFile())) {
            history.push(lastPath, getLocation());
            btnGoBack.setEnabled(true);
            btnGoForward.setEnabled(false);
          }
        } else {
          if (saveDialog)
            txtSaveasFilename.setText(data.getFile().getName());
        }
      }
    });//single click

    listviewFiles.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

      @Override
      public boolean onItemLongClick(AdapterView<?> av, View v, int position,
          long id) {
        if (multiSelection)
          return false;

        DataModel data = (DataModel) av.getItemAtPosition(position);

        if (data.getFile().isDirectory() && selectionMode == FilesOnly)
          return false;

        //if selectionMode == DirectoriesOnly, files won't be shown

        doFinish(data.getFile());

        return false;
      }
    });//long click
  }

  private void doFinish(File... files) {
    List<File> list = new ArrayList<File>();
    for (File f : files)
      list.add(f);
    doFinish((ArrayList<File>) list);
  }

  private void doFinish(ArrayList<File> files) {
    Intent intent = new Intent();

    //set results
    intent.putExtra(Results, files);

    //return flags for further use (in case the caller needs)
    intent.putExtra(SelectionMode, selectionMode);
    intent.putExtra(SaveDialog, saveDialog);

    setResult(RESULT_OK, intent);

    finish();
  }
}