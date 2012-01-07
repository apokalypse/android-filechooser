package group.pals.android.lib.ui.filechooser;

import group.pals.android.lib.ui.filechooser.bean.FileContainer;
import group.pals.android.lib.ui.filechooser.utils.HistoryPath;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class FileChooserActivity extends Activity {

  public static final String Rootpath = "rootpath";
  public static final String SelectionMode = "selection_mode";
  public static final String MultiSelection = "multi_selection";
  public static final String RegexFilenameFilter = "regex_filename_filter";
  public static final String DisplayHiddenFiles = "display_hidden_files";

  public static final int FilesOnly = 0;
  public static final int DirectoriesOnly = 1;
  public static final int FilesAndDirectories = 2;

  public static final String Results = "results";

  /*
   * "constant" variables
   */
  private File root;
  private int selectionMode;
  private boolean multiSelection;
  private String regexFilenameFilter;
  private boolean displayHiddenFiles;

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

    btnLocation = (Button) findViewById(R.id.button_location);
    listviewFiles = (ListView) findViewById(R.id.listview_files);
    btnOk = (Button) findViewById(R.id.button_ok);
    btnGoBack = (ImageButton) findViewById(R.id.button_go_back);
    btnGoForward = (ImageButton) findViewById(R.id.button_go_forward);

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
    if (!multiSelection)
      Toast.makeText(this, R.string.hint_long_click_to_select_files, Toast.LENGTH_SHORT).show();
  }

  private void setupHeader() {
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

    //long click to select results
    btnLocation.setOnLongClickListener(new View.OnLongClickListener() {

      @Override
      public boolean onLongClick(View v) {
        if (multiSelection || selectionMode == FilesOnly)
          return false;

        List<File> list = new ArrayList<File>();
        list.add(getLocation().getFile());

        Intent intent = new Intent();
        intent.putExtra(Results, (ArrayList<File>) list);
        setResult(RESULT_OK, intent);

        finish();

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
    if (!multiSelection) {
      btnOk.setVisibility(View.GONE);
      return;
    }

    btnOk.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        List<File> list = new ArrayList<File>();
        for (int i = 0; i < listviewFiles.getAdapter().getCount(); i++) {
          DataModel dm = (DataModel) listviewFiles.getAdapter().getItem(i);
          if (dm.isSelected())
            list.add(dm.getFile());
        }

        Intent intent = new Intent();
        intent.putExtra(Results, (ArrayList<File>) list);
        setResult(RESULT_OK, intent);

        finish();
      }
    });
  }

  private FileContainer getLocation() {
    return (FileContainer) btnLocation.getTag();
  }

  private boolean setLocation(File path) {
    return setLocation(new FileContainer(path));
  }

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

    List<DataModel> list = new ArrayList<DataModel>();
    for (File f : files)
      list.add(new DataModel(f));
    listviewFiles.setAdapter(new FilesAdapter(
        this, R.layout.file_item, list, selectionMode, multiSelection));

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
        if (data != null && data.getFile().isDirectory()) {
          FileContainer lastPath = getLocation();
          if (setLocation(data.getFile())) {
            history.push(lastPath, getLocation());
            btnGoBack.setEnabled(true);
            btnGoForward.setEnabled(false);
          }
        }
      }
    });

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

        List<File> list = new ArrayList<File>();
        list.add(data.getFile());

        Intent intent = new Intent();
        intent.putExtra(Results, (ArrayList<File>) list);
        setResult(RESULT_OK, intent);

        finish();

        return false;
      }
    });
  }
}