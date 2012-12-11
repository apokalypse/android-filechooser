/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.providers;

/**
 * The base columns.
 * 
 * @author Hai Bison
 * @since v5.1 beta
 * 
 */
public interface BaseColumns extends android.provider.BaseColumns {

    /**
     * Column name for the creation timestamp.
     * <P>
     * Type: {@code String} ({@code long} from {@link java.util.Date#getTime()}
     * ).
     * </P>
     */
    public static final String _ColumnCreateTime = "create_time";

    /**
     * Column name for the modification timestamp.
     * <P>
     * Type: {@code String} ({@code long} from {@link java.util.Date#getTime()}
     * ).
     * </P>
     */
    public static final String _ColumnModificationTime = "modification_time";
}
