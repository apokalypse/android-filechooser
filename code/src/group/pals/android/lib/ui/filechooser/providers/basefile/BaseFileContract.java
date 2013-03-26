/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.providers.basefile;

import group.pals.android.lib.ui.filechooser.providers.BaseColumns;
import group.pals.android.lib.ui.filechooser.providers.ProviderUtils;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

/**
 * Base file contract.
 * 
 * @since v5.1 beta
 * @author Hai Bison
 * 
 */
public class BaseFileContract {

    /**
     * This class cannot be instantiated.
     */
    private BaseFileContract() {
    }// BaseFileContract()

    /**
     * Base file.
     * 
     * @since v5.1 beta
     * @author Hai Bison
     * 
     */
    public static final class BaseFile implements BaseColumns {

        /**
         * This class cannot be instantiated.
         */
        private BaseFile() {
        }// BaseFile()

        /*
         * FILE TYPE.
         */

        /**
         * Directory.
         */
        public static final int _FileTypeDirectory = 0;
        /**
         * File.
         */
        public static final int _FileTypeFile = 1;
        /**
         * Unknown file type.
         */
        public static final int _FileTypeUnknown = 2;
        /**
         * File is not existed.
         */
        public static final int _FileTypeNotExisted = 3;

        /*
         * FILTER MODE.
         */

        /**
         * Only files.
         */
        public static final int _FilterFilesOnly = 0;
        /**
         * Only directories.
         */
        public static final int _FilterDirectoriesOnly = 1;
        /**
         * Files and directories.
         */
        public static final int _FilterFilesAndDirectories = 2;

        /*
         * SORT MODE.
         */

        /**
         * Sort by name.
         */
        public static final int _SortByName = 0;
        /**
         * Sort by size.
         */
        public static final int _SortBySize = 1;
        /**
         * Sort by last modified.
         */
        public static final int _SortByModificationTime = 2;

        /*
         * PATHS
         */

        /**
         * The path to a single directory's contents. You query this path to get
         * the contents of that directory.
         */
        public static final String _PathDir = "dir";
        /**
         * The path to a single file. This can be a file or a directory.
         */
        public static final String _PathFile = "file";
        /**
         * The path to query the provider's information such as name, ID...
         */
        public static final String _PathApi = "api";

        /*
         * COMMANDS.
         */

        /**
         * Use this command to cancel a previous task you executed. You set the
         * task ID with {@link #_ParamTaskId}.
         * 
         * @see #_ParamTaskId
         */
        public static final String _CmdCancel = "cancel";

        /**
         * Use this command along with two parameters: a source directory (
         * {@link #_ParamSource}) and a target file/ directory (
         * {@link #_ParamTarget}). It will return a <i>non-null but empty</i>
         * cursor if the given file is a directory and it is ancestor of the
         * file provided by this parameter. Note that you also have to call
         * {@link Cursor#close()} anyway.
         * <p>
         * If the given file is not a directory or is not ancestor of the file
         * provided by this parameter, the result will be {@code null}.
         * </p>
         * <p>
         * For example, with local file, this query returns {@code true}:
         * </p>
         * <p>
         * {@code content://local-file-authority/api/is_ancestor_of?source="/mnt/sdcard"&target="/mnt/sdcard/Android/data/cache"}
         * </p>
         * <p>
         * Note that no matter how many levels between the ancestor and the
         * descendant are, it is still the ancestor. This is <b><i>not</i></b>
         * the same concept as "parent", which will return {@code false} in
         * above example.
         * </p>
         * 
         * @see #_ParamSource
         * @see #_ParamTarget
         */
        public static final String _CmdIsAncestorOf = "is_ancestor_of";

        /**
         * Use this command to get default path of a provider.
         * <p>
         * Type: {@code String}
         * </p>
         */
        public static final String _CmdGetDefaultPath = "get_default_path";

        /**
         * Use this parameter to get parent file of a file. You provide the
         * source file with {@link #_ParamSource}.
         * 
         * @see #_ParamSource
         */
        public static final String _CmdGetParent = "get_parent";

        /*
         * PARAMETERS.
         */

        /**
         * Use this parameter to provide the source file.
         * <p>
         * Type: URI
         * <p>
         */
        public static final String _ParamSource = "source";

        /**
         * Use this parameter to provide the target file.
         * <p>
         * Type: URI
         * <p>
         */
        public static final String _ParamTarget = "target";

        /**
         * Use this parameter to provide the name of new file/ directory you
         * want to create.
         * <p>
         * Type: {@code String}
         * </p>
         * 
         * @see #_ParamFileType
         */
        public static final String _ParamName = "name";

        /**
         * Use this parameter to provide the type of new file that you want to
         * create. It can be {@link #_FileTypeDirectory} or
         * {@link #_FileTypeFile}. If not provided, default is
         * {@link #_FileTypeDirectory}.
         * 
         * @see #_ParamName
         */
        public static final String _ParamFileType = "file_type";

        /**
         * Use this parameter to set an ID to any task.<br>
         * Default: {@code 0} with all methods.
         * <p>
         * Type: {@code Integer}
         * </p>
         */
        public static final String _ParamTaskId = "task_id";

        /**
         * Use this parameter for operators which can work recursively, such as
         * deleting a directory... The value can be {@code "true"} or
         * {@code "1"} for {@code true}, {@code "false"} or {@code "0"} for
         * {@code false}.
         * <p>
         * Default:
         * </p>
         * <p>
         * <li>{@code "true"} with {@code delete()}.</li>
         * </p>
         * <p>
         * Type: {@code Boolean}
         * </p>
         */
        public static final String _ParamRecursive = "recursive";

        /**
         * Use this parameter to show hidden files. The value can be
         * {@code "true"} or {@code "1"} for {@code true}, {@code "false"} or
         * {@code "0"} for {@code false}.
         * <p>
         * Default: {@code "false"} with {@code query()}.
         * </p>
         * <p>
         * Type: {@code Boolean}
         * </p>
         */
        public static final String _ParamShowHiddenFiles = "show_hidden_files";

        /**
         * Use this parameter to filter file type. Can be one of
         * {@link #_FilterFilesOnly}, {@link #_FilterDirectoriesOnly},
         * {@link #_FilterFilesAndDirectories}.
         * <p>
         * Default: {@link #_FilterFilesAndDirectories} with {@code query()}.
         * </p>
         * <p>
         * Type: {@code Integer}
         * </p>
         */
        public static final String _ParamFilterMode = "filter_mode";

        /**
         * Use this parameter to sort files. Can be one of
         * {@link #_SortByModificationTime}, {@link #_SortByName},
         * {@link #_SortBySize}.
         * <p>
         * Default: {@link #_SortByName} with {@code query()}.
         * </p>
         * <p>
         * Type: {@code Integer}
         * </p>
         */
        public static final String _ParamSortBy = "sort_by";

        /**
         * Use this parameter for sort order. Can be {@code "true"} or
         * {@code "1"} for {@code true}, {@code "false"} or {@code "0"} for
         * {@code false}.
         * <p>
         * Default: {@code "true"} with {@code query()}.
         * </p>
         * <p>
         * Type: {@code Boolean}
         * </p>
         */
        public static final String _ParamSortAscending = "sort_ascending";

        /**
         * Use this parameter to limit results.
         * <p>
         * Default: {@code 1000} with {@code query()}.
         * </p>
         * <p>
         * Type: {@code Integer}
         * </p>
         */
        public static final String _ParamLimit = "limit";

        /**
         * This parameter is returned from the provider. It's only used for
         * {@code query()} while querying directory contents. Can be
         * {@code "true"} or {@code "1"} for {@code true}, {@code "false"} or
         * {@code "0"} for {@code false}.
         * <p>
         * Type: {@code Boolean}
         * </p>
         */
        public static final String _ParamHasMoreFiles = "has_more_files";

        /**
         * Use this parameter to append a file name to a full path of directory
         * to obtains its full pathname.
         * <p>
         * This parameter can be use together with {@link #_ParamAppendPath},
         * the priority is lesser than that parameter.
         * </p>
         * <p>
         * <li>Scope:
         * {@link ContentResolver#query(Uri, String[], String, String[], String)}
         * and related.</li>
         * </p>
         * <p>
         * Type: {@code String}
         * </p>
         */
        public static final String _ParamAppendName = "append_name";

        /**
         * Use this parameter to append a partial path to a full path of
         * directory to obtains its full pathname. The value is a URI, every
         * path segment of the URI is a partial name. You can build the URI with
         * scheme {@link ContentResolver#SCHEME_FILE}, appending your paths with
         * {@link Uri.Builder#appendPath(String)}.
         * <p>
         * This parameter can be use together with {@link #_ParamAppendName},
         * the priority is higher than that parameter.
         * </p>
         * <p>
         * <li>Scope:
         * {@link ContentResolver#query(Uri, String[], String, String[], String)}
         * and related.</li>
         * </p>
         * <p>
         * Type: {@code String}
         * </p>
         * 
         * @see #_ParamAppendName
         */
        public static final String _ParamAppendPath = "append_path";

        /**
         * Use this parameter to set a positive regex to filter filename (with
         * {@code query()}). If the regex can't be compiled due to syntax error,
         * then it will be ignored.
         * <p>
         * Type: {@code String}
         * </p>
         */
        public static final String _ParamPositiveRegexFilter = "positive_regex_filter";

        /**
         * Use this parameter to set a negative regex to filter filename (with
         * {@code query()}). If the regex can't be compiled due to syntax error,
         * then it will be ignored.
         * <p>
         * Type: {@code String}
         * </p>
         */
        public static final String _ParamNegativeRegexFilter = "negative_regex_filter";

        /**
         * Use this parameter to tell the provider to validate files or not.
         * <p>
         * Type: {@code String} - can be {@code "true"} or {@code "1"} for
         * {@code true}, {@code "false"} or {@code "0"} for {@code false}.
         * </p>
         * <p>
         * Scope:
         * {@link ContentResolver#query(Uri, String[], String, String[], String)}
         * and related.
         * </p>
         * <p>
         * Default: {@code true}
         * </p>
         * 
         * @see #_CmdIsAncestorOf
         */
        public static final String _ParamValidate = "validate";

        /*
         * URI builders.
         */

        /**
         * Generates content URI API for a provider.
         * 
         * @param authority
         *            the authority of file provider.
         * @return The API URI for a provider. Default will return provider name
         *         and ID.
         */
        public static Uri genContentUriApi(String authority) {
            return Uri
                    .parse(ProviderUtils._Scheme + authority + "/" + _PathApi);
        }// genContentUriBase()

        /**
         * Generates content URI base for a single directory's contents. That
         * means this URI is used to get the content of the given directory,
         * <b><i>not</b></i> the attributes of its. To get the attributes of a
         * directory (or a file), use {@link #genContentIdUriBase(String)}.
         * 
         * @param authority
         *            the authority of file provider.
         * @return The base URI for a single directory. You append it with the
         *         URI to full path of the directory.
         */
        public static Uri genContentUriBase(String authority) {
            return Uri.parse(ProviderUtils._Scheme + authority + "/" + _PathDir
                    + "/");
        }// genContentUriBase()

        /**
         * Generates content URI base for a single file.
         * 
         * @param authority
         *            the authority of file provider.
         * @return The base URI for a single file. You append it with the URI to
         *         full path of a single file.
         */
        public static Uri genContentIdUriBase(String authority) {
            return Uri.parse(ProviderUtils._Scheme + authority + "/"
                    + _PathFile + "/");
        }// genContentIdUriBase()

        /*
         * MIME type definitions.
         */

        /**
         * The MIME type providing a directory of files.
         */
        public static final String _ContentType = "vnd.android.cursor.dir/vnd.group.pals.android.lib.ui.filechooser.provider.basefile";

        /**
         * The MIME type of a single file.
         */
        public static final String _ContentItemType = "vnd.android.cursor.item/vnd.group.pals.android.lib.ui.filechooser.provider.basefile";

        /*
         * Column definitions
         */

        /**
         * The URI of this file.
         * <p>
         * Type: {@code String}
         * </p>
         */
        public static final String _ColumnUri = "uri";

        /**
         * The human-readable path of this file.
         * <p>
         * Type: {@code String}
         * </p>
         */
        public static final String _ColumnPath = "path";

        /**
         * The name of this file.
         * <p>
         * Type: {@code String}
         * </p>
         */
        public static final String _ColumnName = "name";

        /**
         * Size of this file.
         * <p>
         * Type: {@code Long}
         * </p>
         */
        public static final String _ColumnSize = "size";

        /**
         * Holds the readable attribute of this file, {@code 0 == false} and
         * {@code 1 == true}.
         * <p>
         * Type: {@code Integer}
         * </p>
         */
        public static final String _ColumnCanRead = "can_read";

        /**
         * Holds the writable attribute of this file, {@code 0 == false} and
         * {@code 1 == true}.
         * <p>
         * Type: {@code Integer}
         * </p>
         */
        public static final String _ColumnCanWrite = "can_write";

        /**
         * The type of this file. Can be one of {@link #_FileTypeDirectory},
         * {@link #_FileTypeFile}, {@link #_FileTypeUnknown},
         * {@link #_FileTypeNotExisted}.
         * <p>
         * Type: {@code Integer}
         * </p>
         */
        public static final String _ColumnType = "type";

        /**
         * The name of this provider.
         * <p>
         * Type: {@code String}
         * </p>
         */
        public static final String _ColumnProviderName = "provider_name";

        /**
         * The ID of this provider.
         * <p>
         * Type: {@code String}
         * </p>
         */
        public static final String _ColumnProviderId = "provider_id";
    }// BaseFile
}
