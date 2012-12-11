/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.providers;

import java.util.HashMap;
import java.util.Map;

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

    /**
     * Gets boolean parameter.
     * 
     * @param uri
     *            the original URI.
     * @param key
     *            the key of query parameter.
     * @return {@code false} if the parameter does not exist, or it is either
     *         {@code "false"} or {@code "0"}. {@code true} otherwise.
     */
    public static boolean getBooleanQueryParam(Uri uri, String key) {
        String param = uri.getQueryParameter(key);
        if (param == null || "false".equalsIgnoreCase(param) || "0".equalsIgnoreCase(param))
            return false;
        return true;
    }// getLongQueryParam()

    /**
     * Gets boolean parameter.
     * 
     * @param uri
     *            the original URI.
     * @param key
     *            the key of query parameter.
     * @param defaultValue
     *            the default value if the parameter dies not exist.
     * @return {@code defaultValue} if the parameter does not exist, or it is
     *         either {@code "false"} or {@code "0"}. {@code true} otherwise.
     */
    public static boolean getBooleanQueryParam(Uri uri, String key, boolean defaultValue) {
        String param = uri.getQueryParameter(key);
        if (param == null)
            return defaultValue;
        if ("false".equalsIgnoreCase(param) || "0".equalsIgnoreCase(param))
            return false;
        return true;
    }// getLongQueryParam()

    /**
     * Map of provider IDs to their names, to avoid of querying multiple times
     * for a same ID.
     */
    private static final Map<String, String> _MapProviderName = new HashMap<String, String>();

    /**
     * Gets provider name from its ID.
     * 
     * @param providerId
     *            the provider ID.
     * @return the provider name, or {@code null} if not available.
     */
    public static String getProviderName(String providerId) {
        return _MapProviderName.get(providerId);
    }// getProviderName()

    /**
     * Sets provider name and ID.
     * 
     * @param providerId
     *            the provider ID.
     * @param providerName
     *            the provider name.
     */
    public static void setProviderName(String providerId, String providerName) {
        _MapProviderName.put(providerId, providerName);
    }// setProviderName()
}
