/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.providers.history;

import group.pals.android.lib.ui.filechooser.providers.BaseColumns;
import group.pals.android.lib.ui.filechooser.providers.ProviderUtils;
import group.pals.android.lib.ui.filechooser.providers.basefile.BaseFileContract.BaseFile;
import android.net.Uri;

/**
 * History contract.
 * 
 * @author Hai Bison
 * @since v5.1 beta
 * 
 */
public final class HistoryContract {

    public static final String _Authority = "group.pals.android.lib.ui.filechooser.provider.History";

    // This class cannot be instantiated
    private HistoryContract() {
    }

    /**
     * History table contract.
     */
    public static final class History implements BaseColumns {

        // This class cannot be instantiated
        private History() {
        }

        /**
         * The table name offered by this provider.
         */
        public static final String _TableName = "history";

        /*
         * URI definitions.
         */

        /**
         * Path parts for the URIs.
         */

        /**
         * Path part for the History URI.
         */
        public static final String _PathHistory = "history";

        /**
         * The content:// style URL for this table.
         */
        public static final Uri _ContentUri = Uri.parse(ProviderUtils._Scheme
                + _Authority + "/" + _PathHistory);

        /**
         * The content URI base for a single history item. Callers must append a
         * numeric history ID to this Uri to retrieve a history item.
         */
        public static final Uri _ContentIdUriBase = Uri
                .parse(ProviderUtils._Scheme + _Authority + "/" + _PathHistory
                        + "/");

        /*
         * MIME type definitions.
         */

        /**
         * The MIME type of {@link #_ContentUri} providing a directory of
         * history items.
         */
        public static final String _ContentType = "vnd.android.cursor.dir/vnd.group.pals.android.lib.ui.filechooser.provider.history";

        /**
         * The MIME type of a {@link #_ContentUri} sub-directory of a single
         * history item.
         */
        public static final String _ContentItemType = "vnd.android.cursor.item/vnd.group.pals.android.lib.ui.filechooser.provider.history";

        /**
         * The default sort order for this table.
         */
        public static final String _DefaultSortOrder = _ColumnModificationTime
                + " DESC";

        /*
         * Column definitions.
         */

        /**
         * Column name for the ID of the provider.
         * <P>
         * Type: {@code String}
         * </P>
         */
        public static final String _ColumnProviderId = "provider_id";

        /**
         * Column name for the type of history. The value can be one of
         * {@link BaseFile#_FileTypeDirectory}, {@link BaseFile#_FileTypeFile}.
         * <P>
         * Type: {@code Integer}
         * </P>
         */
        public static final String _ColumnFileType = "file_type";

        /**
         * Column name for the URI of history.
         * <P>
         * Type: {@code URI}
         * </P>
         */
        public static final String _ColumnUri = "uri";
    }// History
}
