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

import java.io.File;

import android.content.Context;

/**
 * This supports some methods which is not available for older APIs.
 * 
 * @author Hai Bison
 * 
 */
public class ContextCompat {

    /**
     * Gets external cache directory. If the directory does not exist, creates
     * it.
     * 
     * @param context
     *            {@link Context}
     * @return the external cache directory, will be {@code null} if external
     *         storage is not available.
     * @see Context#getExternalCacheDir()
     * @since Android API Level 8
     */
    public static File getExternalCacheDir(Context context) {
        return context.getExternalCacheDir();
    }// getExternalCacheDir()
}
