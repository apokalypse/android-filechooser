/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.utils;

import group.pals.android.lib.ui.filechooser.R;
import group.pals.android.lib.ui.filechooser.providers.basefile.BaseFileContract.BaseFile;

import java.util.regex.Pattern;

import android.util.SparseArray;

/**
 * Utilities for files.
 * 
 * @author Hai Bison
 * @since v4.3 beta
 * 
 */
public class FileUtils {

    /**
     * Map of the pattern for file types corresponding to resource IDs for
     * icons.
     */
    private static final SparseArray<Pattern> _MapFileIcons = new SparseArray<Pattern>();

    static {
        _MapFileIcons.put(R.drawable.afc_file_audio, Pattern.compile(MimeTypes._RegexFileTypeAudios));
        _MapFileIcons.put(R.drawable.afc_file_video, Pattern.compile(MimeTypes._RegexFileTypeVideos));
        _MapFileIcons.put(R.drawable.afc_file_image, Pattern.compile(MimeTypes._RegexFileTypeImages));
        _MapFileIcons.put(R.drawable.afc_file_plain_text, Pattern.compile(MimeTypes._RegexFileTypePlainTexts));

        /*
         * APK files are counted before compressed files.
         */
        _MapFileIcons.put(R.drawable.afc_file_apk, Pattern.compile(MimeTypes._RegexFileTypeApks));
        _MapFileIcons.put(R.drawable.afc_file_compressed, Pattern.compile(MimeTypes._RegexFileTypeCompressed));
    }

    /**
     * Gets resource icon based on file type and name.
     * 
     * @param fileType
     *            the file type, can be one of
     *            {@link BaseFile#_FileTypeDirectory},
     *            {@link BaseFile#_FileTypeFile},
     *            {@link BaseFile#_FileTypeUnknown}.
     * @param fileName
     *            the file name.
     * @return the resource icon ID.
     */
    public static int getResIcon(int fileType, String fileName) {
        switch (fileType) {
        case BaseFile._FileTypeDirectory:
            return R.drawable.afc_folder;

        case BaseFile._FileTypeFile:
            for (int i = 0; i < _MapFileIcons.size(); i++)
                if (_MapFileIcons.valueAt(i).matcher(fileName).find())
                    return _MapFileIcons.keyAt(i);

            return R.drawable.afc_file;

        default:
            return android.R.drawable.ic_delete;
        }
    }// getResIcon()

    /**
     * Checks whether the filename given is valid or not.<br>
     * See <a href="http://en.wikipedia.org/wiki/Filename">wiki</a> for more
     * information.
     * 
     * @param name
     *            name of the file
     * @return {@code true} if the {@code name} is valid, and vice versa (if it
     *         contains invalid characters or it is {@code null}/ empty)
     */
    public static boolean isFilenameValid(String name) {
        return name != null && name.trim().matches("[^\\\\/?%*:|\"<>]+");
    }
}