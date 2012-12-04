/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.utils;

import group.pals.android.lib.ui.filechooser.R;
import group.pals.android.lib.ui.filechooser.io.IFile;
import group.pals.android.lib.ui.filechooser.services.IFileProvider;

import java.util.List;

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
     *            {@link IFile}
     * @return the resource icon ID
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

    /**
     * Deletes a file or directory.
     * 
     * @param file
     *            {@link IFile}
     * @param fileProvider
     *            {@link IFileProvider}
     * @param recursive
     *            if {@code true} and {@code file} is a directory, browses the
     *            directory and deletes all of its sub files
     * @return the thread which is deleting files
     */
    public static Thread createDeleteFileThread(final IFile file, final IFileProvider fileProvider,
            final boolean recursive) {
        return new Thread() {

            @Override
            public void run() {
                deleteFile(file);
            }// run()

            private void deleteFile(IFile file) {
                if (isInterrupted())
                    return;

                if (file.isFile()) {
                    file.delete();
                    return;
                } else if (!file.isDirectory())
                    return;

                if (!recursive) {
                    file.delete();
                    return;
                }

                try {
                    List<IFile> files = fileProvider.listAllFiles(file);
                    if (files == null) {
                        file.delete();
                        return;
                    }

                    for (IFile f : files) {
                        if (isInterrupted())
                            return;

                        if (f.isFile())
                            f.delete();
                        else if (f.isDirectory()) {
                            if (recursive)
                                deleteFile(f);
                            else
                                f.delete();
                        }
                    }

                    file.delete();
                } catch (Throwable t) {
                    // TODO
                }
            }// deleteFile()
        };
    }// createDeleteFileThread()
}