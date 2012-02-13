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

import java.io.File;
import java.util.Comparator;

/**
 * {@link File} comparator.<br>
 * Rules:<br>
 * - directories first;<br>
 * - names in alphabetical order (case insensitive);
 * 
 * @author Hai Bison
 * @since v1.91
 */
public class FileComparator implements Comparator<File> {

    @Override
    public int compare(File lhs, File rhs) {
        if ((lhs.isDirectory() && rhs.isDirectory())
                || (lhs.isFile() && rhs.isFile()))
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        return lhs.isDirectory() ? -1 : 1;
    }
}
