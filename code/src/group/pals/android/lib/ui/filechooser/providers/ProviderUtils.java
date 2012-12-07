/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.providers;

import android.net.Uri;

/**
 * Utilities for providers.
 * 
 * @since v5.1 beta
 * @author Hai Bison
 * 
 */
public class ProviderUtils {

    /**
     * The scheme part for default provider's URI.
     */
    public static final String _Scheme = "content://";

    /**
     * Gets integer parameter.
     * 
     * @param uri
     *            the original URI.
     * @param key
     *            the key of query parameter.
     * @param defaultValue
     *            will be returned if nothing found or parsing value failed.
     * @return the integer value.
     */
    public static int getIntQueryParam(Uri uri, String key, int defaultValue) {
        try {
            return Integer.parseInt(uri.getQueryParameter(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }// getIntQueryParam()

    /**
     * Gets long parameter.
     * 
     * @param uri
     *            the original URI.
     * @param key
     *            the key of query parameter.
     * @param defaultValue
     *            will be returned if nothing found or parsing value failed.
     * @return the long value.
     */
    public static long getLongQueryParam(Uri uri, String key, long defaultValue) {
        try {
            return Long.parseLong(uri.getQueryParameter(key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }// getLongQueryParam()
}
