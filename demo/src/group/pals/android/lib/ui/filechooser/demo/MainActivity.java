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

package group.pals.android.lib.ui.filechooser.demo;

import group.pals.android.lib.ui.filechooser.FileChooserActivity;
import group.pals.android.lib.ui.filechooser.io.localfile.LocalFile;
import group.pals.android.lib.ui.filechooser.services.IFileProvider;
import group.pals.android.lib.ui.filechooser.utils.ui.Dlg;

import java.io.File;
import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

/**
 * Main activity of this demo.
 * 
 * @author Hai Bison
 * @since v1.0
 * 
 */
public class MainActivity extends Activity {

    private CheckBox mChkDialogTheme;
    private CheckBox mChkMultiSelection;
    private Button mBtnCreateHugeDir;

    private static final int[] _FileChooserButtonIds = { R.id.activity_main_button_choose_files,
            R.id.activity_main_button_choose_dirs, R.id.activity_main_button_choose_files_and_dirs,
            R.id.activity_main_button_save_as };

    private static final int _ReqChooseFile = 0;
    private static final int _ReqSaveAs = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mChkDialogTheme = (CheckBox) findViewById(R.id.activity_main_checkBox_use_dialog_theme);
        mChkMultiSelection = (CheckBox) findViewById(R.id.activity_main_checkBox_multi_selection);
        mBtnCreateHugeDir = (Button) findViewById(R.id.activity_main_button_create_hugedir);

        // init listeners

        for (int id : _FileChooserButtonIds)
            findViewById(id).setOnClickListener(mBtnFileChooserHandlers);

        mBtnCreateHugeDir.setOnClickListener(mBtnCreateHugeDirOnClickListener);
    }// onCreate()

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case _ReqChooseFile:
            if (resultCode == RESULT_OK) {
                /*
                 * a list of files will always return, if selection mode is
                 * single, the list contains one file
                 */
                List<LocalFile> files = (List<LocalFile>) data.getSerializableExtra(FileChooserActivity._Results);
                StringBuffer msg = new StringBuffer("You chose:\n");
                for (File f : files) {
                    if (f.isDirectory())
                        msg.append(String.format(" — [Dir] %s\n", f.getName()));
                    else
                        msg.append(String.format(" — %s\n", f.getName()));
                }

                Dlg.showInfo(MainActivity.this, msg);
            }
            break;// _ReqChooseFile

        case _ReqSaveAs:
            if (resultCode == RESULT_OK) {
                List<LocalFile> files = (List<LocalFile>) data.getSerializableExtra(FileChooserActivity._Results);
                Dlg.showInfo(MainActivity.this,
                        String.format("Data will be saved to \"%s\"", files.get(0).getAbsolutePath()));
            }
            break;// _ReqSaveAs
        }
    }// onActivityResult()

    // =========
    // LISTENERS

    private final View.OnClickListener mBtnFileChooserHandlers = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, FileChooserActivity.class);

            // theme
            if (mChkDialogTheme.isChecked())
                intent.putExtra(FileChooserActivity._Theme, R.style.AppTheme_Dialog);
            else
                intent.putExtra(FileChooserActivity._Theme, R.style.AppTheme);

            // save as...
            if (v.getId() == R.id.activity_main_button_save_as) {
                intent.putExtra(FileChooserActivity._SaveDialog, true);
                intent.putExtra(FileChooserActivity._DefaultFilename, "hi there  :-)");
                startActivityForResult(intent, _ReqSaveAs);
                return;
            }

            // multi-selection
            intent.putExtra(FileChooserActivity._MultiSelection, mChkMultiSelection.isChecked());

            // filter-mode
            if (v.getId() == R.id.activity_main_button_choose_files)
                intent.putExtra(FileChooserActivity._FilterMode, IFileProvider.FilterMode.FilesOnly);
            else if (v.getId() == R.id.activity_main_button_choose_dirs)
                intent.putExtra(FileChooserActivity._FilterMode, IFileProvider.FilterMode.DirectoriesOnly);
            else if (v.getId() == R.id.activity_main_button_choose_files_and_dirs)
                intent.putExtra(FileChooserActivity._FilterMode, IFileProvider.FilterMode.FilesAndDirectories);

            startActivityForResult(intent, _ReqChooseFile);
        }// onClick()
    };// mBtnFileChooserHandlers

    private final View.OnClickListener mBtnCreateHugeDirOnClickListener = new View.OnClickListener() {

        private static final int _MsgDone = 0;
        private static final int _MsgProgress = 1;

        private File mDir;
        private Handler mHandler;
        private Thread mThread;
        private ProgressDialog mProgressDlg;

        @Override
        public void onClick(View v) {
            mDir = createOrRetrieveExternalTempDir();

            if (mDir == null || !mDir.isDirectory()) {
                Toast.makeText(MainActivity.this, "Can't access external SD card", Toast.LENGTH_SHORT).show();
                return;
            }

            initProgressDialog();
            initHandler();
            createAndStartThread();
        }// onClick()

        private void initProgressDialog() {
            mProgressDlg = ProgressDialog.show(MainActivity.this, null, "Processing…", true, true,
                    new DialogInterface.OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            if (mThread != null)
                                mThread.interrupt();
                        }
                    });// ProgressDialog
            mProgressDlg.setCanceledOnTouchOutside(true);
        }// initProgressDialog()

        private void initHandler() {
            mHandler = new Handler() {

                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                    case _MsgProgress:
                        if (mProgressDlg != null) {
                            mProgressDlg.setMessage(String.format("Loading… %.02f%% done", msg.obj));
                        }
                        break;// _MsgProgress

                    case _MsgDone:
                        Toast.makeText(MainActivity.this,
                                String.format("Huge-dir has been created at \"%s\"", mDir.getAbsolutePath()),
                                Toast.LENGTH_LONG).show();
                        break;// _MsgDone
                    }
                }
            };// mHandler
        }// initHandler()

        private void createAndStartThread() {
            mThread = new Thread() {

                private static final int _Count = (int) 2e3;

                @Override
                public void run() {
                    int step = 5;

                    final String _dirPath = mDir.getAbsolutePath();

                    for (int i = 0; i < _Count && !isInterrupted(); i++) {
                        try {
                            File file = new File(String.format("%s/%,09d", _dirPath, i));
                            if (!file.exists())
                                file.createNewFile();
                        } catch (IOException e) {
                            interrupt();
                        }

                        if ((i + 1.0) / _Count * 100 >= step) {
                            Message msg = new Message();
                            msg.what = _MsgProgress;
                            msg.obj = (i + 1.0) / _Count * 100;
                            mHandler.sendMessage(msg);

                            step += 5;
                        }
                    }// for i

                    mHandler.sendEmptyMessage(_MsgDone);
                    if (mProgressDlg != null)
                        mProgressDlg.dismiss();
                }
            };// mThread

            mThread.start();
        }// createAndStartThread()

        /**
         * Generates an external temporary directory name {@code "huge-dir"}. It
         * will be:<br>
         * <li>{@code /mnt/sdcard/Android/data/<package_name>/cache/huge-dir}</li>
         * <br>
         * 
         * @see <a href=
         *      "http://developer.android.com/guide/topics/data/data-storage.html#filesExternal"
         *      >Guide</a> for more information.<br>
         * 
         * @return {@link File} or {@code null} if an error occurred.
         */
        private File createOrRetrieveExternalTempDir() {
            final String _hugeDir = "huge-dir";

            File dir;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                dir = ContextCompat.getExternalCacheDir(MainActivity.this);
                if (dir != null)
                    dir = new File(String.format("%s/%s", dir.getAbsolutePath(), _hugeDir));
            } else {
                dir = Environment.getExternalStorageDirectory();
                if (dir != null)
                    dir = new File(String.format("%s/Android/data/%s/cache/%s", dir.getAbsolutePath(),
                            getApplicationInfo().packageName, _hugeDir));
            }

            if (dir == null)
                return null;

            if (dir.isDirectory())
                return dir;
            return dir.mkdirs() ? dir : null;
        }// createOrRetrieveExternalTempDir()
    };// mBtnCreateHugeDirOnClickListener
}
