/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.providers.bookmark;

import group.pals.android.lib.ui.filechooser.prefs.Prefs;
import group.pals.android.lib.ui.filechooser.providers.DbUtils;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

/**
 * Database for bookmark.
 * 
 * @author Hai Bison
 * @since v5.1 beta
 * 
 */
public class BookmarkHelper extends SQLiteOpenHelper {

    @SuppressWarnings("unused")
    private static final String _ClassName = BookmarkHelper.class.getName();

    private static final String _DatabaseFilename = "Bookmarks.sqlite";
    private static final int _DatabaseVersion = 1;

    // Database creation SQL statements

    /**
     * @since v5.1 beta
     */
    private static final String _PatternDatabaseCreator = String.format("CREATE VIRTUAL TABLE "
            + BookmarkContract.Bookmark._TableName + " USING %%s(" + BookmarkContract.Bookmark._ColumnCreateTime + ","
            + BookmarkContract.Bookmark._ColumnModificationTime + "," + BookmarkContract.Bookmark._ColumnProviderId
            + "," + BookmarkContract.Bookmark._ColumnUri + "," + BookmarkContract.Bookmark._ColumnName
            + ",tokenize=porter);");

    public BookmarkHelper(Context context) {
        // always use application context
        super(context.getApplicationContext(), Prefs.genDatabaseFilename(context, _DatabaseFilename), null,
                _DatabaseVersion);
    }// BookmarkHelper()

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(String.format(_PatternDatabaseCreator,
                Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ? DbUtils._SqliteFts3 : DbUtils._SqliteFts4));
    }// onCreate()

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO
    }// onUpgrade()
}
