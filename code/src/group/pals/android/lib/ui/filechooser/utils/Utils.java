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

package group.pals.android.lib.ui.filechooser.utils;

import group.pals.android.lib.ui.filechooser.io.IFile;
import group.pals.android.lib.ui.filechooser.services.IFileProvider;

import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Utilities.
 */
public class Utils {

    /**
     * Deletes a file or directory.
     * 
     * @param file
     *            {@link IFile}
     * @param fileProvider
     *            {@link IFileProvider}
     * @param recursive
     *            if {@code true} and {@code file} is a directory, browses the
     *            directory and deletes all of its sub files
     * @return the thread which is deleting files
     */
    public static Thread createDeleteFileThread(final IFile file, final IFileProvider fileProvider,
            final boolean recursive) {
        return new Thread() {

            @Override
            public void run() {
                deleteFile(file);
            }// run()

            private void deleteFile(IFile file) {
                if (isInterrupted())
                    return;

                if (file.isFile()) {
                    file.delete();
                    return;
                } else if (!file.isDirectory())
                    return;

                if (!recursive) {
                    file.delete();
                    return;
                }

                try {
                    List<IFile> files = fileProvider.listAllFiles(file);
                    if (files == null) {
                        file.delete();
                        return;
                    }

                    for (IFile f : files) {
                        if (isInterrupted())
                            return;

                        if (f.isFile())
                            f.delete();
                        else if (f.isDirectory()) {
                            if (recursive)
                                deleteFile(f);
                            else
                                f.delete();
                        }
                    }

                    file.delete();
                } catch (Throwable t) {
                    // TODO
                }
            }// deleteFile()
        };
    }// deleteFile()

    /**
     * Checks if the app has <b>all</b> {@code permissions} granted
     * 
     * @param context
     *            {@link Context}
     * @param permissions
     *            array of permission names
     * @return {@code true} if the app has all {@code permissions} asked
     */
    public static boolean havePermissions(Context context, String... permissions) {
        for (String p : permissions)
            if (context.checkCallingOrSelfPermission(p) == PackageManager.PERMISSION_DENIED)
                return false;
        return true;
    }// havePermissions()
}
