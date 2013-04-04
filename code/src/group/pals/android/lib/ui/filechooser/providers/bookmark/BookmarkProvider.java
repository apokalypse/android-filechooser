/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.providers.bookmark;

import group.pals.android.lib.ui.filechooser.providers.DbUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Bookmark provider.
 * 
 * @author Hai Bison
 * @since v5.1 beta
 * 
 */
public class BookmarkProvider extends ContentProvider {

    @SuppressWarnings("unused")
    private static final String _ClassName = BookmarkProvider.class.getName();

    /*
     * Constants used by the Uri matcher to choose an action based on the
     * pattern of the incoming URI.
     */

    /**
     * The incoming URI matches the Bookmark URI pattern.
     */
    private static final int _Bookmarks = 1;

    /**
     * The incoming URI matches the Bookmark ID URI pattern.
     */
    private static final int _BookmarkId = 2;

    /**
     * A {@link UriMatcher} instance.
     */
    private static final UriMatcher _UriMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    private static final Map<String, String> _ColumnMap = new HashMap<String, String>();

    static {
        _UriMatcher.addURI(BookmarkContract._Authority,
                BookmarkContract.Bookmark._PathBookmarks, _Bookmarks);
        _UriMatcher.addURI(BookmarkContract._Authority,
                BookmarkContract.Bookmark._PathBookmarks + "/#", _BookmarkId);

        _ColumnMap.put(DbUtils._SqliteFtsColumnRowId,
                DbUtils._SqliteFtsColumnRowId + " AS "
                        + BookmarkContract.Bookmark._ID);
        _ColumnMap.put(BookmarkContract.Bookmark._ColumnName,
                BookmarkContract.Bookmark._ColumnName);
        _ColumnMap.put(BookmarkContract.Bookmark._ColumnProviderId,
                BookmarkContract.Bookmark._ColumnProviderId);
        _ColumnMap.put(BookmarkContract.Bookmark._ColumnUri,
                BookmarkContract.Bookmark._ColumnUri);
        _ColumnMap.put(BookmarkContract.Bookmark._ColumnCreateTime,
                BookmarkContract.Bookmark._ColumnCreateTime);
        _ColumnMap.put(BookmarkContract.Bookmark._ColumnModificationTime,
                BookmarkContract.Bookmark._ColumnModificationTime);
    }// static

    private BookmarkHelper mBookmarkHelper;

    @Override
    public boolean onCreate() {
        mBookmarkHelper = new BookmarkHelper(getContext());
        return true;
    }// onCreate()

    @Override
    public String getType(Uri uri) {
        /*
         * Chooses the MIME type based on the incoming URI pattern.
         */
        switch (_UriMatcher.match(uri)) {
        case _Bookmarks:
            return BookmarkContract.Bookmark._ContentType;

        case _BookmarkId:
            return BookmarkContract.Bookmark._ContentItemType;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }// getType()

    @Override
    public synchronized int delete(Uri uri, String selection,
            String[] selectionArgs) {
        // Opens the database object in "write" mode.
        SQLiteDatabase db = mBookmarkHelper.getWritableDatabase();
        String finalWhere;

        int count;

        // Does the delete based on the incoming URI pattern.
        switch (_UriMatcher.match(uri)) {
        /*
         * If the incoming pattern matches the general pattern for Bookmark
         * items, does a delete based on the incoming "where" columns and
         * arguments.
         */
        case _Bookmarks: {
            count = db.delete(BookmarkContract.Bookmark._TableName, selection,
                    selectionArgs);
            break;
        }// _Bookmarks

        /*
         * If the incoming URI matches a single note ID, does the delete based
         * on the incoming data, but modifies the where clause to restrict it to
         * the particular Bookmark item ID.
         */
        case _BookmarkId: {
            /*
             * Starts a final WHERE clause by restricting it to the desired
             * Bookmark item ID.
             */
            finalWhere = DbUtils._SqliteFtsColumnRowId + " = "
                    + uri.getLastPathSegment();

            /*
             * If there were additional selection criteria, append them to the
             * final WHERE clause
             */
            if (selection != null)
                finalWhere = finalWhere + " AND " + selection;

            // Performs the delete.
            count = db.delete(BookmarkContract.Bookmark._TableName, finalWhere,
                    selectionArgs);
            break;
        }// _BookmarkId

        // If the incoming pattern is invalid, throws an exception.
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        /*
         * Gets a handle to the content resolver object for the current context,
         * and notifies it that the incoming URI changed. The object passes this
         * along to the resolver framework, and observers that have registered
         * themselves for the provider are notified.
         */
        getContext().getContentResolver().notifyChange(uri, null);

        // Returns the number of rows deleted.
        return count;
    }// delete()

    @Override
    public synchronized Uri insert(Uri uri, ContentValues values) {
        /*
         * Validates the incoming URI. Only the full provider URI is allowed for
         * inserts.
         */
        if (_UriMatcher.match(uri) != _Bookmarks)
            throw new IllegalArgumentException("Unknown URI " + uri);

        // Gets the current time in milliseconds
        long now = new Date().getTime();

        /*
         * If the values map doesn't contain the creation date/ modification
         * date, sets the value to the current time.
         */
        for (String col : new String[] {
                BookmarkContract.Bookmark._ColumnCreateTime,
                BookmarkContract.Bookmark._ColumnModificationTime })
            if (!values.containsKey(col))
                values.put(col, DbUtils.formatNumber(now));

        // Opens the database object in "write" mode.
        SQLiteDatabase db = mBookmarkHelper.getWritableDatabase();

        // Performs the insert and returns the ID of the new note.
        long rowId = db.insert(BookmarkContract.Bookmark._TableName, null,
                values);

        // If the insert succeeded, the row ID exists.
        if (rowId > 0) {
            /*
             * Creates a URI with the note ID pattern and the new row ID
             * appended to it.
             */
            Uri noteUri = ContentUris.withAppendedId(
                    BookmarkContract.Bookmark._ContentIdUriBase, rowId);

            /*
             * Notifies observers registered against this provider that the data
             * changed.
             */
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        /*
         * If the insert didn't succeed, then the rowID is <= 0. Throws an
         * exception.
         */
        throw new SQLException("Failed to insert row into " + uri);
    }// insert()

    @Override
    public synchronized Cursor query(Uri uri, String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(BookmarkContract.Bookmark._TableName);
        qb.setProjectionMap(_ColumnMap);

        SQLiteDatabase db = null;
        Cursor cursor = null;

        /*
         * Choose the projection and adjust the "where" clause based on URI
         * pattern-matching.
         */
        switch (_UriMatcher.match(uri)) {
        case _Bookmarks: {
            if (Arrays.equals(projection,
                    new String[] { BookmarkContract.Bookmark._COUNT })) {
                db = mBookmarkHelper.getReadableDatabase();
                cursor = db.rawQuery(
                        String.format(
                                "SELECT COUNT(*) AS %s FROM %s %s",
                                BookmarkContract.Bookmark._COUNT,
                                BookmarkContract.Bookmark._TableName,
                                selection != null ? String.format("WHERE %s",
                                        selection) : "").trim(), null);
            }

            break;
        }// _Bookmarks

        /*
         * If the incoming URI is for a single Bookmark item identified by its
         * ID, chooses the Bookmark item ID projection, and appends
         * "_ID = <history-item-ID>" to the where clause, so that it selects
         * that single Bookmark item.
         */
        case _BookmarkId: {
            qb.appendWhere(DbUtils._SqliteFtsColumnRowId + " = "
                    + uri.getLastPathSegment());
            break;
        }// _BookmarkId

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        if (TextUtils.isEmpty(sortOrder))
            sortOrder = BookmarkContract.Bookmark._DefaultSortOrder;

        if (db == null) {
            /*
             * Opens the database object in "read" mode, since no writes need to
             * be done.
             */
            db = mBookmarkHelper.getReadableDatabase();
            /*
             * Performs the query. If no problems occur trying to read the
             * database, then a Cursor object is returned; otherwise, the cursor
             * variable contains null. If no records were selected, then the
             * Cursor object is empty, and Cursor.getCount() returns 0.
             */
            cursor = qb.query(db, projection, selection, selectionArgs, null,
                    null, sortOrder);
        }

        /*
         * Tells the Cursor what URI to watch, so it knows when its source data
         * changes.
         */
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }// query()

    @Override
    public synchronized int update(Uri uri, ContentValues values,
            String selection, String[] selectionArgs) {
        // Opens the database object in "write" mode.
        SQLiteDatabase db = mBookmarkHelper.getWritableDatabase();
        int count;
        String finalWhere;

        // Does the update based on the incoming URI pattern
        switch (_UriMatcher.match(uri)) {
        /*
         * If the incoming URI matches the general Bookmark items pattern, does
         * the update based on the incoming data.
         */
        case _Bookmarks: {
            // Does the update and returns the number of rows updated.
            count = db.update(BookmarkContract.Bookmark._TableName, values,
                    selection, selectionArgs);
            break;
        }// _Bookmarks

        /*
         * If the incoming URI matches a single Bookmark item ID, does the
         * update based on the incoming data, but modifies the where clause to
         * restrict it to the particular history item ID.
         */
        case _BookmarkId: {
            /*
             * Starts creating the final WHERE clause by restricting it to the
             * incoming item ID.
             */
            finalWhere = DbUtils._SqliteFtsColumnRowId + " = "
                    + uri.getLastPathSegment();

            /*
             * If there were additional selection criteria, append them to the
             * final WHERE clause
             */
            if (selection != null)
                finalWhere = finalWhere + " AND " + selection;

            // Does the update and returns the number of rows updated.
            count = db.update(BookmarkContract.Bookmark._TableName, values,
                    finalWhere, selectionArgs);
            break;
        }// _BookmarkId

        // If the incoming pattern is invalid, throws an exception.
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        /*
         * Gets a handle to the content resolver object for the current context,
         * and notifies it that the incoming URI changed. The object passes this
         * along to the resolver framework, and observers that have registered
         * themselves for the provider are notified.
         */
        getContext().getContentResolver().notifyChange(uri, null);

        // Returns the number of rows updated.
        return count;
    }// update()
}
