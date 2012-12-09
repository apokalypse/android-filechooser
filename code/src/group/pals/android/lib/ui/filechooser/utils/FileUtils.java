/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.utils;

import group.pals.android.lib.ui.filechooser.R;
import group.pals.android.lib.ui.filechooser.io.IFile;
import group.pals.android.lib.ui.filechooser.providers.basefile.BaseFileContract.BaseFile;
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
     * Map of the regexes for file types corresponding to resource IDs for
     * icons.
     */
    private static final SparseArray<String> _MapFileIcons = new SparseArray<String>();

    static {
        _MapFileIcons.put(R.drawable.afc_file_audio, MimeTypes._RegexFileTypeAudios);
        _MapFileIcons.put(R.drawable.afc_file_video, MimeTypes._RegexFileTypeVideos);
        _MapFileIcons.put(R.drawable.afc_file_image, MimeTypes._RegexFileTypeImages);
        _MapFileIcons.put(R.drawable.afc_file_plain_text, MimeTypes._RegexFileTypePlainTexts);

        /*
         * APK files are counted before compressed files.
         */
        _MapFileIcons.put(R.drawable.afc_file_apk, MimeTypes._RegexFileTypeApks);
        _MapFileIcons.put(R.drawable.afc_file_compressed, MimeTypes._RegexFileTypeCompressed);
    }

    /**
     * Gets resource icon ID of an {@link IFile}.
     * 
     * @param file
     *            {@link IFile}.
     * @return the resource icon ID.
     */
    public static int getResIcon(IFile file) {
        if (file == null || !file.exists())
            return android.R.drawable.ic_delete;

        if (file.isFile()) {
            String filename = file.getName();
            for (int i = 0; i < _MapFileIcons.size(); i++)
                if (filename.matches(_MapFileIcons.valueAt(i)))
                    return _MapFileIcons.keyAt(i);

            return R.drawable.afc_file;
        } else if (file.isDirectory())
            return R.drawable.afc_folder;

        return android.R.drawable.ic_delete;
    }// getResIcon()

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
                if (fileName.matches(_MapFileIcons.valueAt(i)))
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