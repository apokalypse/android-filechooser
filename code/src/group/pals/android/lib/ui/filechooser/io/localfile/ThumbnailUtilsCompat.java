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

package group.pals.android.lib.ui.filechooser.io.localfile;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

/**
 * Helper class for {@link ThumbnailUtils} for use in API 8+.
 * 
 * @author Hai Bison
 * @since v4.6 beta
 * 
 */
public class ThumbnailUtilsCompat {

    /**
     * Creates a thumbnail for video file {@code filePath}.
     * 
     * @param filePath
     *            the full path to file.
     * @param kind
     *            can be one of {@link MediaStore.Images.Thumbnails#MICRO_KIND},
     *            {@link MediaStore.Images.Thumbnails#MINI_KIND}.
     * @return the video thumbnail, can be {@code null}.
     */
    public static Bitmap createVideoThumbnail(String filePath, int kind) {
        return ThumbnailUtils.createVideoThumbnail(filePath, kind);
    }// createVideoThumbnail()
}
