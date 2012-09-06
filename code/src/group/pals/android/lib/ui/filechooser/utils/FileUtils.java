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

import group.pals.android.lib.ui.filechooser.R;
import group.pals.android.lib.ui.filechooser.io.IFile;

/**
 * Utilities for files.
 * 
 * @author Hai Bison
 * @since v4.3 beta
 * 
 */
public class FileUtils {

    /**
     * Gets resource icon ID of an {@link IFile}.
     * 
     * @param file
     *            {@link IFile}
     * @return the resource icon ID
     */
    public static int getResIcon(IFile file) {
        if (file == null || !file.exists())
            return android.R.drawable.ic_delete;

        if (file.isFile()) {
            if (file.getName().matches(MimeTypes._RegexFileTypeAudios))
                return R.drawable.afc_file_audio;
            if (file.getName().matches(MimeTypes._RegexFileTypeVideos))
                return R.drawable.afc_file_video;
            if (file.getName().matches(MimeTypes._RegexFileTypeImages))
                return R.drawable.afc_file_image;
            return R.drawable.afc_file;
        } else if (file.isDirectory())
            return R.drawable.afc_folder;
        return android.R.drawable.ic_delete;
    }// getResIcon()
}
