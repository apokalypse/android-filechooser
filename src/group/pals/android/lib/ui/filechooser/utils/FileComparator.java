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

import group.pals.android.lib.ui.filechooser.FileChooserActivity;

import java.io.File;
import java.util.Comparator;

/**
 * {@link File} comparator.<br>
 * Rules:<br>
 * - directories first;<br>
 * - other properties are based on parameters given in constructor, see
 * {@link #FileComparator(int, int)};
 * 
 * @author Hai Bison
 * @since v1.91
 */
public class FileComparator implements Comparator<File> {

    private final int SortType;
    private final int SortOrder;

    /**
     * Creates new {@link FileComparator}
     * 
     * @param sortType
     *            see {@link FileChooserActivity#SortType}
     * @param sortOrder
     *            see {@link FileChooserActivity#SortOrder}
     */
    public FileComparator(int sortType, int sortOrder) {
        SortType = sortType;
        SortOrder = sortOrder;
    }

    @Override
    public int compare(File lhs, File rhs) {
        if ((lhs.isDirectory() && rhs.isDirectory())
                || (lhs.isFile() && rhs.isFile())) {
            int res = 0;
            switch (SortType) {
            case FileChooserActivity.SortByName:
                res = lhs.getName().compareToIgnoreCase(rhs.getName());
                break;// SortByName

            case FileChooserActivity.SortBySize:
                if (lhs.length() > rhs.length())
                    res = 1;
                else if (lhs.length() == rhs.length())
                    res = lhs.getName().compareToIgnoreCase(rhs.getName());
                else
                    res = -1;
                break;// SortBySize

            case FileChooserActivity.SortByDate:
                if (lhs.lastModified() > rhs.lastModified())
                    res = 1;
                else if (lhs.lastModified() == rhs.lastModified())
                    res = lhs.getName().compareToIgnoreCase(rhs.getName());
                else
                    res = -1;
                break;// SortByDate

            default:
                res = lhs.getName().compareToIgnoreCase(rhs.getName());
                break;
            }

            return SortOrder == FileChooserActivity.Ascending ? res : -res;
        }

        return lhs.isDirectory() ? -1 : 1;
    }// compare
}
