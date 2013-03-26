/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.providers.bookmark;

import group.pals.android.lib.ui.filechooser.providers.BaseColumns;
import group.pals.android.lib.ui.filechooser.providers.ProviderUtils;
import android.net.Uri;

/**
 * Bookmark contract.
 * 
 * @author Hai Bison
 * @since v5.1 beta
 * 
 */
public final class BookmarkContract {

    public static final String _Authority = "group.pals.android.lib.ui.filechooser.provider.Bookmark";

    // This class cannot be instantiated
    private BookmarkContract() {
    }

    /**
     * Bookmark table contract.
     */
    public static final class Bookmark implements BaseColumns {

        // This class cannot be instantiated
        private Bookmark() {
        }

        /**
         * The table name offered by this provider.
         */
        public static final String _TableName = "bookmarks";

        /*
         * URI definitions.
         */

        /**
         * Path parts for the URIs.
         */

        /**
         * Path part for the Bookmark URI.
         */
        public static final String _PathBookmarks = "bookmarks";

        /**
         * The content:// style URL for this table.
         */
        public static final Uri _ContentUri = Uri.parse(ProviderUtils._Scheme
                + _Authority + "/" + _PathBookmarks);

        /**
         * The content URI base for a single Bookmark item. Callers must append
         * a numeric Bookmark id to this Uri to retrieve a Bookmark item.
         */
        public static final Uri _ContentIdUriBase = Uri
                .parse(ProviderUtils._Scheme + _Authority + "/"
                        + _PathBookmarks + "/");

        /*
         * MIME type definitions
         */

        /**
         * The MIME type of {@link #_ContentUri} providing a directory of
         * Bookmark items.
         */
        public static final String _ContentType = "vnd.android.cursor.dir/vnd.group.pals.android.lib.ui.filechooser.provider.bookmarks";

        /**
         * The MIME type of a {@link #_ContentUri} sub-directory of a single
         * Bookmark item.
         */
        public static final String _ContentItemType = "vnd.android.cursor.item/vnd.group.pals.android.lib.ui.filechooser.provider.bookmarks";

        /**
         * The default sort order for this table.
         */
        public static final String _DefaultSortOrder = _ColumnModificationTime
                + " DESC";

        /*
         * Column definitions
         */

        /**
         * Column name for the URI of bookmark.
         * <P>
         * Type: {@code String}
         * </P>
         */
        public static final String _ColumnUri = "uri";

        /**
         * Column name for the name of bookmark.
         * <p>
         * Type: {@code String}
         * </p>
         */
        public static final String _ColumnName = "name";

        /**
         * Column name for the ID of bookmark's provider ID.
         * <p>
         * Type: {@code String}
         * </p>
         */
        public static final String _ColumnProviderId = "provider_id";
    }// Bookmark
}
