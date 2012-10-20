package group.pals.android.lib.ui.filechooser.demo;

import java.io.File;

import android.content.Context;

/**
 * This supports some methods which are not available for older APIs.
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
