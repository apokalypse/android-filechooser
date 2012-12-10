/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.providers;

import group.pals.android.lib.ui.filechooser.providers.basefile.BaseFileContract.BaseFile;
import group.pals.android.lib.ui.filechooser.providers.localfile.LocalFileContract;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * Utilities for base file provider.
 * 
 * @since v5.1 beta
 * @author Hai Bison
 * 
 */
public class BaseFileProviderUtils {

    /**
     * Map of provider ID to its authority.<br>
     * <b>Note for developers:</b> If you provide your own provider, add its ID
     * and authority to this map, in the {@code static} block below.
     */
    private static final Map<String, String> _MapProviderInfo = new HashMap<String, String>();

    static {
        _MapProviderInfo.put(LocalFileContract._ID, LocalFileContract._Authority);
    }// static

    /**
     * Gets provider authority from its ID.
     * 
     * @param providerId
     *            the provider ID.
     * @return the provider authority, or {@code null} if not available.
     */
    public static String getProviderAuthority(String providerId) {
        return _MapProviderInfo.get(providerId);
    }// getProviderAuthority()

    /**
     * Gets provider ID from its authority.
     * 
     * @param authority
     *            the provider authority.
     * @return the provider ID, or {@code null} if not available.
     */
    public static String getProviderId(String authority) {
        for (Entry<String, String> entry : _MapProviderInfo.entrySet())
            if (entry.getValue().equals(authority))
                return entry.getKey();
        return null;
    }// getProviderId()

    /**
     * Gets provider name from its ID.
     * 
     * @param context
     *            {@link Context}.
     * @param providerId
     *            the provider ID.
     * @return the provider name, can be {@code null} if not provided.
     */
    public static String getProviderName(Context context, String providerId) {
        String authority = getProviderAuthority(providerId);
        if (authority == null)
            return null;

        Cursor cursor = context.getContentResolver().query(BaseFile.genContentUriInfo(authority), null, null, null,
                null);
        if (cursor == null)
            return null;
        try {
            if (cursor.moveToFirst())
                return cursor.getString(cursor.getColumnIndex(BaseFile._ColumnProviderName));
            return null;
        } finally {
            cursor.close();
        }
    }// getProviderName()

    /**
     * Checks if {@code uri} is a directory.
     * 
     * @param context
     *            {@link Context}.
     * @param authority
     *            the file provider authority.
     * @param uri
     *            the URI you want to check.
     * @return {@code true} if {@code uri} is a directory, {@code false}
     *         otherwise.
     */
    public static boolean isDirectory(Context context, String authority, Uri uri) {
        Cursor cursor = context.getContentResolver().query(
                BaseFile.genContentIdUriBase(authority).buildUpon().appendPath(uri.toString()).build(), null, null,
                null, null);
        if (cursor == null)
            return false;

        try {
            if (cursor.moveToFirst())
                return isDirectory(cursor);
            return false;
        } finally {
            cursor.close();
        }
    }// isDirectory()

    /**
     * Checks if {@code cursor} is a directory.
     * 
     * @param cursor
     *            the cursor points to a file.
     * @return {@code true} if {@code uri} is a directory, {@code false}
     *         otherwise.
     */
    public static boolean isDirectory(Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndex(BaseFile._ColumnType)) == BaseFile._FileTypeDirectory;
    }// isDirectory()

    /**
     * Checks if {@code uri} is a file.
     * 
     * @param context
     *            {@link Context}.
     * @param authority
     *            the file provider authority.
     * @param uri
     *            the URI you want to check.
     * @return {@code true} if {@code uri} is a file, {@code false} otherwise.
     */
    public static boolean isFile(Context context, String authority, Uri uri) {
        Cursor cursor = context.getContentResolver().query(
                BaseFile.genContentIdUriBase(authority).buildUpon().appendPath(uri.toString()).build(), null, null,
                null, null);
        if (cursor == null)
            return false;

        try {
            if (cursor.moveToFirst())
                return isFile(cursor);
            return false;
        } finally {
            cursor.close();
        }
    }// isFile()

    /**
     * Checks if {@code cursor} is a file.
     * 
     * @param cursor
     *            the cursor points to a file.
     * @return {@code true} if {@code uri} is a file, {@code false} otherwise.
     */
    public static boolean isFile(Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndex(BaseFile._ColumnType)) == BaseFile._FileTypeFile;
    }// isFile()

    /**
     * Gets file name of {@code uri}.
     * 
     * @param context
     *            {@link Context}.
     * @param authority
     *            the file provider authority.
     * @param uri
     *            the URI you want to get.
     * @return the file name if {@code uri} is a file, {@code null} otherwise.
     */
    public static String getFileName(Context context, String authority, Uri uri) {
        Cursor cursor = context.getContentResolver().query(
                BaseFile.genContentIdUriBase(authority).buildUpon().appendPath(uri.toString()).build(), null, null,
                null, null);
        if (cursor == null)
            return null;

        try {
            if (cursor.moveToFirst())
                return getFileName(cursor);
            return null;
        } finally {
            cursor.close();
        }
    }// getFileName()

    /**
     * Gets filename of {@code cursor}.
     * 
     * @param cursor
     *            the cursor points to a file.
     * @return the filename.
     */
    public static String getFileName(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(BaseFile._ColumnName));
    }// getFileName()

    /**
     * Gets file type of the file pointed by {@code uri}.
     * 
     * @param context
     *            {@link Context}.
     * @param authority
     *            the file provider authority.
     * @param uri
     *            the URI you want to get.
     * @return the file type of {@code uri}, can be one of
     *         {@link #_FileTypeDirectory}, {@link #_FileTypeFile},
     *         {@link #_FileTypeUnknown}, {@link #_FileTypeNotExisted}.
     */
    public static int getFileType(Context context, String authority, Uri uri) {
        Cursor cursor = context.getContentResolver().query(
                BaseFile.genContentIdUriBase(authority).buildUpon().appendPath(uri.toString()).build(), null, null,
                null, null);
        if (cursor == null)
            return BaseFile._FileTypeNotExisted;

        try {
            if (cursor.moveToFirst())
                return getFileType(cursor);
            return BaseFile._FileTypeNotExisted;
        } finally {
            cursor.close();
        }
    }// getFileType()

    /**
     * Gets file type of the file pointed by {@code cursor}.
     * 
     * @param cursor
     *            the cursor points to a file.
     * @return the file type, can be one of {@link #_FileTypeDirectory},
     *         {@link #_FileTypeFile}, {@link #_FileTypeUnknown},
     *         {@link #_FileTypeNotExisted}.
     */
    public static int getFileType(Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndex(BaseFile._ColumnType));
    }// getFileType()

    /**
     * Gets URI of {@code cursor}.
     * 
     * @param cursor
     *            the cursor points to a file.
     * @return the URI.
     */
    public static Uri getUri(Cursor cursor) {
        return Uri.parse(cursor.getString(cursor.getColumnIndex(BaseFile._ColumnUri)));
    }// getFileName()

    /**
     * Checks if the file pointed by {@code uri} is existed or not.
     * 
     * @param context
     *            {@link Context}.
     * @param authority
     *            the file provider authority.
     * @param uri
     *            the URI you want to check.
     * @return {@code true} or {@code false}.
     */
    public static boolean fileExists(Context context, String authority, Uri uri) {
        Cursor cursor = context.getContentResolver().query(
                BaseFile.genContentIdUriBase(authority).buildUpon().appendPath(uri.toString()).build(), null, null,
                null, null);
        if (cursor == null)
            return false;

        try {
            if (cursor.moveToFirst())
                return cursor.getInt(cursor.getColumnIndex(BaseFile._ColumnType)) != BaseFile._FileTypeNotExisted;
            return false;
        } finally {
            cursor.close();
        }
    }// fileExists()

    /**
     * Checks if the file pointed by {@code uri} is readable or not.
     * 
     * @param context
     *            {@link Context}.
     * @param authority
     *            the file provider authority.
     * @param uri
     *            the URI you want to check.
     * @return {@code true} or {@code false}.
     */
    public static boolean fileCanRead(Context context, String authority, Uri uri) {
        Cursor cursor = context.getContentResolver().query(
                BaseFile.genContentIdUriBase(authority).buildUpon().appendPath(uri.toString()).build(), null, null,
                null, null);
        if (cursor == null)
            return false;

        try {
            if (cursor.moveToFirst())
                return fileCanRead(cursor);
            return false;
        } finally {
            cursor.close();
        }
    }// fileCanRead()

    /**
     * Checks if the file pointed be {@code cursor} is readable or not.
     * 
     * @param cursor
     *            the cursor points to a file.
     * @return {@code true} or {@code false}.
     */
    public static boolean fileCanRead(Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndex(BaseFile._ColumnCanRead)) != 0
                && (cursor.getInt(cursor.getColumnIndex(BaseFile._ColumnType)) == BaseFile._FileTypeDirectory || cursor
                        .getInt(cursor.getColumnIndex(BaseFile._ColumnType)) == BaseFile._FileTypeFile);
    }// fileCanRead()

    /**
     * Checks if the file pointed by {@code uri} is writable or not.
     * 
     * @param context
     *            {@link Context}.
     * @param authority
     *            the file provider authority.
     * @param uri
     *            the URI you want to check.
     * @return {@code true} or {@code false}.
     */
    public static boolean fileCanWrite(Context context, String authority, Uri uri) {
        Cursor cursor = context.getContentResolver().query(
                BaseFile.genContentIdUriBase(authority).buildUpon().appendPath(uri.toString()).build(), null, null,
                null, null);
        if (cursor == null)
            return false;

        try {
            if (cursor.moveToFirst())
                return fileCanWrite(cursor);
            return false;
        } finally {
            cursor.close();
        }
    }// fileCanWrite()

    /**
     * Checks if the file pointed by {@code cursor} is writable or not.
     * 
     * @param cursor
     *            the cursor points to a file.
     * @return {@code true} or {@code false}.
     */
    public static boolean fileCanWrite(Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndex(BaseFile._ColumnCanWrite)) != 0
                && (cursor.getInt(cursor.getColumnIndex(BaseFile._ColumnType)) == BaseFile._FileTypeDirectory || cursor
                        .getInt(cursor.getColumnIndex(BaseFile._ColumnType)) == BaseFile._FileTypeFile);
    }// fileCanWrite()

    /**
     * Gets default path of a provider.
     * 
     * @param context
     *            {@link Context}.
     * @param authority
     *            the provider's authority.
     * @return the default path, can be {@code null}.
     */
    public static Uri getDefaultPath(Context context, String authority) {
        Cursor cursor = context.getContentResolver().query(BaseFile.genContentUriBase(authority), null, null, null,
                null);
        if (cursor == null)
            return null;

        try {
            if (cursor.moveToFirst())
                return Uri.parse(cursor.getString(cursor.getColumnIndex(BaseFile._ColumnUri)));
            return null;
        } finally {
            cursor.close();
        }
    }// getDefaultPath()

    /**
     * Gets parent directory of {@code uri}.
     * 
     * @param context
     *            {@link Context}.
     * @param authority
     *            the file provider authority.
     * @param uri
     *            the URI of an existing file.
     * @return the parent file if it exists, {@code null} otherwise.
     */
    public static Uri getParentFile(Context context, String authority, Uri uri) {
        Cursor cursor = context.getContentResolver().query(
                BaseFile.genContentIdUriBase(authority).buildUpon().appendPath(uri.toString())
                        .appendQueryParameter(BaseFile._ParamGetParent, Boolean.toString(true)).build(), null, null,
                null, null);
        if (cursor == null)
            return null;

        try {
            if (cursor.moveToFirst())
                return Uri.parse(cursor.getString(cursor.getColumnIndex(BaseFile._ColumnUri)));
            return null;
        } finally {
            cursor.close();
        }
    }// getParentFile()

    /**
     * Checks if {@code uri1} is ancestor of {@code uri2}.
     * 
     * @param context
     *            {@link Context}.
     * @param authority
     *            the provider authority.
     * @param uri1
     *            the first URI.
     * @param uri2
     *            the second URI.
     * @return {@code true} if {@code uri1} is ancestor of {@code uri2},
     *         {@code false} otherwise.
     */
    public static boolean isAncestorOf(Context context, String authority, Uri uri1, Uri uri2) {
        Cursor cursor = context.getContentResolver().query(
                BaseFile.genContentIdUriBase(authority).buildUpon().appendPath(uri1.toString())
                        .appendQueryParameter(BaseFile._ParamIsAncestorOf, uri2.toString()).build(), null, null, null,
                null);
        if (cursor == null)
            return false;

        try {
            return true;
        } finally {
            cursor.close();
        }
    }// isAncestorOf()
}
