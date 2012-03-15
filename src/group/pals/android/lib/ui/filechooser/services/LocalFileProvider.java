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

package group.pals.android.lib.ui.filechooser.services;

import group.pals.android.lib.ui.filechooser.utils.FileComparator;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

/**
 * This is simple local file provider - as its name means.<br>
 * It handles file request on local device.
 * 
 * @author Hai Bison
 * @since v2.1 alpha
 */
public class LocalFileProvider extends FileProviderService {

    /**
     * The action name for this service.
     */
    public static final String ServiceActionName = LocalFileProvider.class
            .getName() + ".60cf767c.6fe6.41ad.90ce.e94657bde773";

    /*-------------------------------------------------------------------
     * Service
     */

    @Override
    public void onCreate() {
        // TODO
    }// onCreate()

    /*-------------------------------------------------------------------
     * IFileProvider
     */

    @Override
    public File[] listFiles(File dir, final boolean[] fHasMoreFiles) {
        if (fHasMoreFiles != null && fHasMoreFiles.length > 0)
            fHasMoreFiles[0] = false;

        try {
            File[] files = dir.listFiles(new FileFilter() {

                int fileCount = 0;

                @Override
                public boolean accept(File pathname) {
                    if (!isDisplayHiddenFiles()
                            && pathname.getName().startsWith("."))
                        return false;
                    if (fileCount >= getMaxFileCount()) {
                        if (fHasMoreFiles != null && fHasMoreFiles.length > 0)
                            fHasMoreFiles[0] = true;
                        return false;
                    }

                    switch (getFilterMode()) {
                    case FilesOnly:
                        if (getRegexFilenameFilter() != null && pathname.isFile())
                            return pathname.getName().matches(
                                    getRegexFilenameFilter());

                        fileCount++;
                        return true;
                    case DirectoriesOnly:
                        boolean ok = pathname.isDirectory();
                        if (ok)
                            fileCount++;
                        return ok;
                    default:
                        if (getRegexFilenameFilter() != null && pathname.isFile())
                            return pathname.getName().matches(
                                    getRegexFilenameFilter());

                        fileCount++;
                        return true;
                    }
                }
            });// dir.listFiles()

            if (files != null)
                Arrays.sort(files, new FileComparator(getSortType(), getSortOrder()));

            return files;
        } catch (Exception e) {
            return null;
        }
    }// listFiles()
}
