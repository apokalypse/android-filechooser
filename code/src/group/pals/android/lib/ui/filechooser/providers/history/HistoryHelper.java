/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.providers.history;

import group.pals.android.lib.ui.filechooser.prefs.Prefs;
import group.pals.android.lib.ui.filechooser.providers.DbUtils;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

/**
 * SQLite helper for history database.
 * 
 * @since v5.1 beta
 * @author Hai Bison
 * 
 */
public class HistoryHelper extends SQLiteOpenHelper {

    private static final String _DatabaseFilename = "History.sqlite";
    private static final int _DatabaseVersion = 1;

    /**
     * @since v5.1 beta
     */
    private static final String _PatternDatabaseCreator_v3 = String
            .format("CREATE VIRTUAL TABLE "
                    + HistoryContract.History._TableName + " USING %%s("
                    + HistoryContract.History._ColumnCreateTime + ","
                    + HistoryContract.History._ColumnModificationTime + ","
                    + HistoryContract.History._ColumnProviderId + ","
                    + HistoryContract.History._ColumnFileType + ","
                    + HistoryContract.History._ColumnUri + ",tokenize=porter);");

    public HistoryHelper(Context context) {
        // always use application context
        super(context.getApplicationContext(), Prefs.genDatabaseFilename(
                context, _DatabaseFilename), null, _DatabaseVersion);
    }// HistoryHelper()

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(String
                .format(_PatternDatabaseCreator_v3,
                        Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ? DbUtils._SqliteFts3
                                : DbUtils._SqliteFts4));
    }// onCreate()

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO
    }// onUpgrade()
}
