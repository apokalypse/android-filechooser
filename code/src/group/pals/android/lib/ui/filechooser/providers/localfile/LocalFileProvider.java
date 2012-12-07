/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.providers.localfile;

import group.pals.android.lib.ui.filechooser.providers.basefile.BaseFileContract.BaseFile;
import group.pals.android.lib.ui.filechooser.providers.basefile.BaseFileProvider;
import group.pals.android.lib.ui.filechooser.providers.basefile.ProviderUtils;

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
import android.util.SparseBooleanArray;

/**
 * Local file provider.
 * 
 * @since v5.1 beta
 * @author Hai Bison
 * 
 */
public class LocalFileProvider extends BaseFileProvider {

    /*
     * Constants used by the Uri matcher to choose an action based on the
     * pattern of the incoming URI.
     */

    /**
     * The incoming URI matches the single directory URI pattern.
     */
    private static final int _Directory = 1;

    /**
     * The incoming URI matches the single file URI pattern.
     */
    private static final int _File = 2;

    /**
     * A {@link UriMatcher} instance.
     */
    private static final UriMatcher _UriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /**
     * Map of task IDs to their interruption signals.
     */
    private static final SparseBooleanArray _MapInterruption = new SparseBooleanArray();

    static {
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

            if (taskId == 0)
                return 0;

            boolean isCancelled = uri.getBooleanQueryParameter(BaseFile._ParamCancel, false);
            if (isCancelled) {
                synchronized (_MapInterruption) {
                    _MapInterruption.put(taskId, true);
                }
            }// client wants to cancel the previous task
            else {
                boolean isRecursive = uri.getBooleanQueryParameter(BaseFile._ParamRecursive, false);
                File file = new File(uri.getLastPathSegment());
                if (file.isFile()) {
                    if (file.delete())
                        count = 1;
                } else {
                    deleteFile(taskId, file, isRecursive);
                    _MapInterruption.delete(taskId);
                }
            }// client wants to create new task

            break;// single file
        }

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

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
            File file = new File(uri.getLastPathSegment());
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
        final File _srcFile = new File(uri.getLastPathSegment());
        if (!_srcFile.exists())
            return null;

        MatrixCursor matrixCursor = new MatrixCursor(new String[] { BaseFile._ID, BaseFile._ColumnCanRead,
                BaseFile._ColumnSize, BaseFile._ColumnType, BaseFile._ColumnUri, BaseFile._ColumnModificationTime });

        switch (_UriMatcher.match(uri)) {
        case _Directory: {
            if (!_srcFile.isDirectory() || !_srcFile.canRead())
                return null;

            /*
             * Prepare params...
             */
            int taskId = ProviderUtils.getIntQueryParam(uri, BaseFile._ParamTaskId, 0);
            boolean isCancelled = uri.getBooleanQueryParameter(BaseFile._ParamCancel, false);

            if (isCancelled) {
                synchronized (_MapInterruption) {
                    if (_MapInterruption.indexOfKey(taskId) >= 0)
                        _MapInterruption.put(taskId, true);
                }
            }// client wants to cancel the previous task
            else {
                /*
                 * Prepare params...
                 */
                boolean showHiddenFiles = uri.getBooleanQueryParameter(BaseFile._ParamShowHiddenFiles, false);
                boolean sortAscending = uri.getBooleanQueryParameter(BaseFile._ParamSortAscending, true);
                int sortBy = ProviderUtils.getIntQueryParam(uri, BaseFile._ParamSortBy, BaseFile._SortByName);
                int filterMode = ProviderUtils.getIntQueryParam(uri, BaseFile._ParamFilterMode,
                        BaseFile._FilterFilesAndDirectories);
                int limit = ProviderUtils.getIntQueryParam(uri, BaseFile._ParamLimit, 1000);

                List<File> files = new ArrayList<File>();
                listFiles(taskId, _srcFile, showHiddenFiles, filterMode, limit, files);
                if (!_MapInterruption.get(taskId)) {
                    sortFiles(files, sortAscending, sortBy);
                    for (int i = 0; i < files.size(); i++) {
                        File f = files.get(i);
                        int type = f.isFile() ? BaseFile._FileTypeFile : (f.isDirectory() ? BaseFile._FileTypeDirectory
                                : BaseFile._FileTypeUnknown);
                        RowBuilder newRow = matrixCursor.newRow();
                        newRow.add(i);// _ID
                        newRow.add(f.canRead());
                        newRow.add(f.length());
                        newRow.add(type);
                        newRow.add(f.toURI().toString());
                        newRow.add(f.lastModified());
                    }// for files
                }
                _MapInterruption.delete(taskId);
            }// client wants to query new data

            break;// _Directory
        }

        case _File: {
            int type = _srcFile.isFile() ? BaseFile._FileTypeFile
                    : (_srcFile.isDirectory() ? BaseFile._FileTypeDirectory : BaseFile._FileTypeUnknown);
            RowBuilder newRow = matrixCursor.newRow();
            newRow.add(0);// _ID
            newRow.add(_srcFile.canRead());
            newRow.add(_srcFile.length());
            newRow.add(type);
            newRow.add(_srcFile.toURI().toString());
            newRow.add(_srcFile.lastModified());

            break;// _File
        }

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        /*
         * Tells the Cursor what URI to watch, so it knows when its source data
         * changes.
         */
        matrixCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return matrixCursor;
    }// query()

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new IllegalArgumentException("Unsupported method update()");
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
     */
    private void listFiles(final int taskId, final File dir, final boolean showHiddenFiles, final int filterMode,
            final int limit, final List<File> results) {
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

                    if (results.size() >= limit)
                        throw new CancellationException();
                    results.add(pathname);

                    return false;
                }// accept()
            });
        } catch (CancellationException e) {
            // ignore it
        }
    }// listFiles()

    /**
     * Sorts {@code files}.
     * 
     * @param files
     *            list of files.
     * @param ascending
     *            {@code true} or {@code false}.
     * @param sortBy
     *            can be one of {@link BaseFile.#_SortByModificationTime},
     *            {@link BaseFile.#_SortByName}, {@link BaseFile.#_SortBySize}.
     */
    private void sortFiles(final List<File> files, final boolean ascending, final int sortBy) {
        Collections.sort(files, new Comparator<File>() {

            @Override
            public int compare(File lhs, File rhs) {
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
     */
    private void deleteFile(final int taskId, final File file, final boolean recursive) {
        if (_MapInterruption.get(taskId))
            return;

        if (file.isFile()) {
            file.delete();
            return;
        }

        /*
         * If the directory is empty, try to delete it and return here.
         */
        if (file.delete() || !recursive)
            return;

        try {
            try {
                file.listFiles(new FileFilter() {

                    @Override
                    public boolean accept(File pathname) {
                        if (_MapInterruption.get(taskId))
                            throw new CancellationException();

                        if (pathname.isFile())
                            pathname.delete();
                        else if (pathname.isDirectory()) {
                            if (recursive)
                                deleteFile(taskId, pathname, recursive);
                            else
                                pathname.delete();
                        }

                        return false;
                    }// accept()
                });
            } catch (CancellationException e) {
                return;
            }

            file.delete();
        } catch (Throwable t) {
            // TODO
        }
    }// deleteFile()
}
