/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.providers.localfile;

import group.pals.android.lib.ui.filechooser.BuildConfig;
import group.pals.android.lib.ui.filechooser.providers.ProviderUtils;
import group.pals.android.lib.ui.filechooser.providers.basefile.BaseFileContract.BaseFile;
import group.pals.android.lib.ui.filechooser.providers.basefile.BaseFileProvider;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CancellationException;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MatrixCursor.RowBuilder;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;

/**
 * Local file provider.
 * 
 * @since v5.1 beta
 * @author Hai Bison
 * 
 */
public class LocalFileProvider extends BaseFileProvider {

    /**
     * Used for debugging or something...
     */
    private static final String _ClassName = LocalFileProvider.class.getName();

    /*
     * Constants used by the Uri matcher to choose an action based on the
     * pattern of the incoming URI.
     */

    /**
     * The incoming URI matches the default directory URI pattern.
     */
    private static final int _DefaultDirectory = 1;

    /**
     * The incoming URI matches the single directory URI pattern.
     */
    private static final int _Directory = 2;

    /**
     * The incoming URI matches the single file URI pattern.
     */
    private static final int _File = 3;

    /**
     * A {@link UriMatcher} instance.
     */
    private static final UriMatcher _UriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /**
     * Map of task IDs to their interruption signals.
     */
    private static final SparseBooleanArray _MapInterruption = new SparseBooleanArray();

    static {
        _UriMatcher.addURI(LocalFileContract._Authority, BaseFile._PathDirectory, _DefaultDirectory);
        _UriMatcher.addURI(LocalFileContract._Authority, BaseFile._PathDirectory + "/*", _Directory);
        _UriMatcher.addURI(LocalFileContract._Authority, BaseFile._PathFile + "/*", _File);
    }// static

    private final Collator mCollator = Collator.getInstance();

    @Override
    public String getType(Uri uri) {
        /*
         * Chooses the MIME type based on the incoming URI pattern.
         */
        switch (_UriMatcher.match(uri)) {
        case _Directory:
            return BaseFile._ContentType;

        case _File:
            return BaseFile._ContentItemType;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }// getType()

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;

        switch (_UriMatcher.match(uri)) {
        case _File: {
            int taskId = ProviderUtils.getIntQueryParam(uri, BaseFile._ParamTaskId, 0);
            boolean isCancelled = ProviderUtils.getBooleanQueryParam(uri, BaseFile._ParamCancel);
            if (isCancelled) {
                synchronized (_MapInterruption) {
                    _MapInterruption.put(taskId, true);
                }
                return 0;
            }// client wants to cancel the previous task

            boolean isRecursive = ProviderUtils.getBooleanQueryParam(uri, BaseFile._ParamRecursive, true);
            File file = new File(Uri.parse(uri.getLastPathSegment()).getPath());
            if (file.isFile()) {
                if (file.delete())
                    count = 1;
            } else {
                count = deleteFile(taskId, file, isRecursive);
                if (_MapInterruption.get(taskId)) {
                    if (BuildConfig.DEBUG)
                        Log.d(_ClassName, "delete() >> cancelled...");
                    _MapInterruption.delete(taskId);
                }
            }

            break;// single file
        }

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (BuildConfig.DEBUG)
            Log.d(_ClassName, "delete() >> count = " + count);

        if (count > 0) {
            /*
             * Gets a handle to the content resolver object for the current
             * context, and notifies it that the incoming URI changed. The
             * object passes this along to the resolver framework, and observers
             * that have registered themselves for the provider are notified.
             */
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return count;
    }// delete()

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (_UriMatcher.match(uri)) {
        case _Directory:
            File file = new File(Uri.parse(uri.getLastPathSegment()).getPath());
            if (!file.isDirectory() || !file.canWrite())
                return null;

            File newFile = new File(String.format("%s/%s", file.getAbsolutePath(),
                    values.getAsString(BaseFile._ColumnUri)));

            switch (values.getAsInteger(BaseFile._ColumnType)) {
            case BaseFile._FileTypeDirectory:
                newFile.mkdir();
                break;// _FileTypeDirectory

            case BaseFile._FileTypeFile:
                try {
                    newFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;// _FileTypeFile

            default:
                return null;
            }

            if (newFile.exists()) {
                Uri newUri = Uri.parse(newFile.toURI().toString());
                /*
                 * Notifies observers registered against this provider that the
                 * data changed.
                 */
                getContext().getContentResolver().notifyChange(newUri, null);
                return newUri;
            }
            return null;// _Directory

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }// insert()

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (BuildConfig.DEBUG)
            Log.d(_ClassName,
                    String.format("query() >> uri = %s (%s) >> match = %s", uri, uri.getLastPathSegment(),
                            _UriMatcher.match(uri)));

        MatrixCursor matrixCursor = new MatrixCursor(new String[] { BaseFile._ID, BaseFile._ColumnUri,
                BaseFile._ColumnName, BaseFile._ColumnCanRead, BaseFile._ColumnSize, BaseFile._ColumnType,
                BaseFile._ColumnModificationTime });

        switch (_UriMatcher.match(uri)) {
        case _DefaultDirectory: {
            File file = Environment.getExternalStorageDirectory();
            if (file == null || !file.isDirectory())
                file = new File("/");
            int type = file.isFile() ? BaseFile._FileTypeFile : (file.isDirectory() ? BaseFile._FileTypeDirectory
                    : BaseFile._FileTypeUnknown);
            RowBuilder newRow = matrixCursor.newRow();
            newRow.add(0);// _ID
            newRow.add(Uri.fromFile(file).toString());
            newRow.add(file.getName());
            newRow.add(file.canRead() ? 1 : 0);
            newRow.add(file.length());
            newRow.add(type);
            newRow.add(file.lastModified());

            return matrixCursor;// _DefaultDirectory
        }

        case _Directory: {
            File file = new File(Uri.parse(uri.getLastPathSegment()).getPath());

            if (BuildConfig.DEBUG)
                Log.d(_ClassName, "srcFile = " + file);

            /*
             * Prepare params...
             */
            int taskId = ProviderUtils.getIntQueryParam(uri, BaseFile._ParamTaskId, 0);
            boolean isCancelled = ProviderUtils.getBooleanQueryParam(uri, BaseFile._ParamCancel);

            if (isCancelled) {
                synchronized (_MapInterruption) {
                    _MapInterruption.put(taskId, true);
                }
                return null;
            }// client wants to cancel the previous task

            if (!file.isDirectory() || !file.canRead())
                return null;

            /*
             * Prepare params...
             */
            boolean showHiddenFiles = ProviderUtils.getBooleanQueryParam(uri, BaseFile._ParamShowHiddenFiles);
            boolean sortAscending = ProviderUtils.getBooleanQueryParam(uri, BaseFile._ParamSortAscending, true);
            int sortBy = ProviderUtils.getIntQueryParam(uri, BaseFile._ParamSortBy, BaseFile._SortByName);
            int filterMode = ProviderUtils.getIntQueryParam(uri, BaseFile._ParamFilterMode,
                    BaseFile._FilterFilesAndDirectories);
            int limit = ProviderUtils.getIntQueryParam(uri, BaseFile._ParamLimit, 1000);

            boolean[] hasMoreFiles = { false };
            List<File> files = new ArrayList<File>();
            listFiles(taskId, file, showHiddenFiles, filterMode, limit, files, hasMoreFiles);
            if (!_MapInterruption.get(taskId)) {
                sortFiles(taskId, files, sortAscending, sortBy);
                if (!_MapInterruption.get(taskId)) {
                    for (int i = 0; i < files.size(); i++) {
                        if (_MapInterruption.get(taskId))
                            break;

                        File f = files.get(i);
                        int type = f.isFile() ? BaseFile._FileTypeFile : (f.isDirectory() ? BaseFile._FileTypeDirectory
                                : BaseFile._FileTypeUnknown);
                        RowBuilder newRow = matrixCursor.newRow();
                        newRow.add(i);// _ID
                        newRow.add(Uri.fromFile(f).toString());
                        newRow.add(f.getName());
                        newRow.add(f.canRead() ? 1 : 0);
                        newRow.add(f.length());
                        newRow.add(type);
                        newRow.add(f.lastModified());
                    }// for files

                    RowBuilder newRow = matrixCursor.newRow();
                    newRow.add(files.size());// _ID
                    newRow.add(Uri.fromFile(file).buildUpon()
                            .appendQueryParameter(BaseFile._ParamHasMoreFiles, Boolean.toString(hasMoreFiles[0]))
                            .build().toString());
                    newRow.add(file.getName());
                }
            }

            if (_MapInterruption.get(taskId)) {
                if (BuildConfig.DEBUG)
                    Log.d(_ClassName, "query() >> cancelled...");
                _MapInterruption.delete(taskId);
                return null;
            }

            /*
             * Tells the Cursor what URI to watch, so it knows when its source
             * data changes.
             */
            matrixCursor.setNotificationUri(getContext().getContentResolver(), uri);

            break;// _Directory
        }

        case _File: {
            Uri fileUri = Uri.parse(uri.getLastPathSegment());
            String appendName = uri.getQueryParameter(BaseFile._ParamAppendName);

            File file = new File(String.format("%s%s", fileUri.getPath(),
                    !TextUtils.isEmpty(appendName) ? String.format("/%s", appendName) : ""));
            if (ProviderUtils.getBooleanQueryParam(uri, BaseFile._ParamGetParent))
                file = file.getParentFile();
            if (file == null)
                return null;

            int type = file.isFile() ? BaseFile._FileTypeFile : (file.isDirectory() ? BaseFile._FileTypeDirectory
                    : (file.exists() ? BaseFile._FileTypeUnknown : BaseFile._FileTypeNotExisted));
            RowBuilder newRow = matrixCursor.newRow();
            newRow.add(0);// _ID
            newRow.add(Uri.fromFile(file).toString());
            newRow.add(file.getName());
            newRow.add(file.canRead() ? 1 : 0);
            newRow.add(file.length());
            newRow.add(type);
            newRow.add(file.lastModified());

            break;// _File
        }

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        return matrixCursor;
    }// query()

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }// update()

    /*
     * UTILITIES
     */

    /**
     * Lists all file inside {@code dir}.
     * 
     * @param taskId
     *            the task ID.
     * @param dir
     *            the source directory.
     * @param showHiddenFiles
     *            {@code true} or {@code false}.
     * @param filterMode
     *            can be one of {@link BaseFile#_FilterDirectoriesOnly},
     *            {@link BaseFile#_FilterFilesOnly},
     *            {@link BaseFile#_FilterFilesAndDirectories}.
     * @param limit
     *            the limit.
     * @param results
     *            the results.
     * @param hasMoreFiles
     *            the first item will contain a value representing that there is
     *            more files (exceeding {@code limit}) or not.
     */
    private void listFiles(final int taskId, final File dir, final boolean showHiddenFiles, final int filterMode,
            final int limit, final List<File> results, final boolean hasMoreFiles[]) {
        hasMoreFiles[0] = false;
        try {
            dir.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    if (_MapInterruption.get(taskId))
                        throw new CancellationException();

                    if (filterMode == BaseFile._FilterDirectoriesOnly && pathname.isFile())
                        return false;
                    if (!showHiddenFiles && pathname.getName().startsWith("."))
                        return false;

                    if (results.size() >= limit) {
                        hasMoreFiles[0] = true;
                        throw new CancellationException();
                    }
                    results.add(pathname);

                    return false;
                }// accept()
            });
        } catch (CancellationException e) {
            if (BuildConfig.DEBUG)
                Log.d(_ClassName, "listFiles() >> cancelled...");
        }
    }// listFiles()

    /**
     * Sorts {@code files}.
     * 
     * @param taskId
     *            the task ID.
     * @param files
     *            list of files.
     * @param ascending
     *            {@code true} or {@code false}.
     * @param sortBy
     *            can be one of {@link BaseFile.#_SortByModificationTime},
     *            {@link BaseFile.#_SortByName}, {@link BaseFile.#_SortBySize}.
     */
    private void sortFiles(final int taskId, final List<File> files, final boolean ascending, final int sortBy) {
        try {
            Collections.sort(files, new Comparator<File>() {

                @Override
                public int compare(File lhs, File rhs) {
                    if (_MapInterruption.get(taskId))
                        throw new CancellationException();

                    if ((lhs.isDirectory() && rhs.isDirectory()) || (lhs.isFile() && rhs.isFile())) {
                        // default is to compare by name (case insensitive)
                        int res = mCollator.compare(lhs.getName(), rhs.getName());

                        switch (sortBy) {
                        case BaseFile._SortByName:
                            break;// SortByName

                        case BaseFile._SortBySize:
                            if (lhs.length() > rhs.length())
                                res = 1;
                            else if (lhs.length() < rhs.length())
                                res = -1;
                            break;// SortBySize

                        case BaseFile._SortByModificationTime:
                            if (lhs.lastModified() > rhs.lastModified())
                                res = 1;
                            else if (lhs.lastModified() < rhs.lastModified())
                                res = -1;
                            break;// SortByDate
                        }

                        return ascending ? res : -res;
                    }

                    return lhs.isDirectory() ? -1 : 1;
                }// compare()
            });
        } catch (CancellationException e) {
            if (BuildConfig.DEBUG)
                Log.d(_ClassName, "sortFiles() >> cancelled...");
        }
    }// sortFiles()

    /**
     * Deletes {@code file}.
     * 
     * @param taskId
     *            the task ID.
     * @param file
     *            {@link File}.
     * @param recursive
     *            if {@code true} and {@code file} is a directory, this thread
     *            will delete all sub files/ folders of it recursively.
     * @return the total files deleted.
     */
    private int deleteFile(final int taskId, final File file, final boolean recursive) {
        final int[] _count = { 0 };
        if (_MapInterruption.get(taskId))
            return _count[0];

        if (file.isFile()) {
            if (file.delete())
                _count[0]++;
            return _count[0];
        }

        /*
         * If the directory is empty, try to delete it and return here.
         */
        if (file.delete()) {
            _count[0]++;
            return _count[0];
        }

        if (!recursive)
            return _count[0];

        try {
            try {
                file.listFiles(new FileFilter() {

                    @Override
                    public boolean accept(File pathname) {
                        if (_MapInterruption.get(taskId))
                            throw new CancellationException();

                        if (pathname.isFile()) {
                            if (pathname.delete())
                                _count[0]++;
                        } else if (pathname.isDirectory()) {
                            if (recursive)
                                _count[0] += deleteFile(taskId, pathname, recursive);
                            else if (pathname.delete())
                                _count[0]++;
                        }

                        return false;
                    }// accept()
                });
            } catch (CancellationException e) {
                return _count[0];
            }

            if (file.delete())
                _count[0]++;
        } catch (Throwable t) {
            // TODO
        }

        return _count[0];
    }// deleteFile()
}
