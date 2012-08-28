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
import group.pals.android.lib.ui.filechooser.services.IFileProvider;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

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
    private Button mBtnChooseFiles;
    private Button mBtnChooseDirs;
    private Button mBtnChooseFilesAndDirs;
    private Button mBtnSaveAs;
    private Button mBtnCreateHugeDir;

    private static final int _ReqChooseFile = 0;
    private static final int _ReqSaveAs = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mChkDialogTheme = (CheckBox) findViewById(R.id.activity_main_checkBox_use_dialog_theme);
        mChkMultiSelection = (CheckBox) findViewById(R.id.activity_main_checkBox_multi_selection);
        mBtnChooseFiles = (Button) findViewById(R.id.activity_main_button_choose_files);
        mBtnChooseDirs = (Button) findViewById(R.id.activity_main_button_choose_dirs);
        mBtnChooseFilesAndDirs = (Button) findViewById(R.id.activity_main_button_choose_files_and_dirs);
        mBtnSaveAs = (Button) findViewById(R.id.activity_main_button_save_as);
        mBtnCreateHugeDir = (Button) findViewById(R.id.activity_main_button_create_hugedir);

        // init listeners

        for (Button b : new Button[] { mBtnChooseFiles, mBtnChooseDirs, mBtnChooseFilesAndDirs, mBtnSaveAs })
            b.setOnClickListener(mBtnFileChooserHandlers);

        mBtnCreateHugeDir.setOnClickListener(mBtnCreateHugeDirOnClickListener);
    }// onCreate()

    // =========
    // LISTENERS

    private static final int _DialogTheme = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH ? android.R.style.Theme_DeviceDefault_Dialog
            : (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? android.R.style.Theme_Holo_Dialog
                    : android.R.style.Theme_Dialog);

    private final View.OnClickListener mBtnFileChooserHandlers = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, FileChooserActivity.class);

            // multi-selection
            intent.putExtra(FileChooserActivity._MultiSelection, mChkMultiSelection.isChecked());
            // theme
            if (mChkDialogTheme.isChecked())
                intent.putExtra(FileChooserActivity._Theme, _DialogTheme);

            // filter-mode
            if (v.getId() == R.id.activity_main_button_choose_files)
                intent.putExtra(FileChooserActivity._FilterMode, IFileProvider.FilterMode.FilesOnly);
            else if (v.getId() == R.id.activity_main_button_choose_dirs)
                intent.putExtra(FileChooserActivity._FilterMode, IFileProvider.FilterMode.DirectoriesOnly);
            else if (v.getId() == R.id.activity_main_button_choose_files_and_dirs)
                intent.putExtra(FileChooserActivity._FilterMode, IFileProvider.FilterMode.FilesAndDirectories);
            else if (v.getId() == R.id.activity_main_button_save_as) {
                intent.putExtra(FileChooserActivity._SaveDialog, true);
                intent.putExtra(FileChooserActivity._DefaultFilename, "filename  :-)");
                startActivityForResult(intent, _ReqSaveAs);
                return;
            }

            startActivityForResult(intent, _ReqChooseFile);
        }// onClick()
    };// mBtnFileChooserHandlers

    private final View.OnClickListener mBtnCreateHugeDirOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            //
        }// onClick()
    };// mBtnCreateHugeDirOnClickListener
}
