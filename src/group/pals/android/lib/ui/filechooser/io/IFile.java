/*
 *   Copyright 2012 Hai Bison
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package group.pals.android.lib.ui.filechooser.io;

import java.io.Serializable;

import android.os.Parcelable;

/**
 * Interface for "file" used in this library. In case you want to use this
 * library for your own file system, have your "file" implement this interface.<br>
 * <b>Note:</b> Remember to implement either {@link Serializable} of
 * {@link Parcelable} too. If not, the library will not work properly. Because
 * Android requires data to be transferred between activities must implement one
 * of two above interfaces.
 * 
 * @author Hai Bison
 * @since v3.2
 */
public interface IFile {

    /**
     * Returns the absolute pathname string of this abstract pathname.
     * 
     * @return The absolute pathname string denoting the same file or directory
     *         as this abstract pathname
     * @throws SecurityException
     *             If a required system property value cannot be accessed.
     */
    String getAbsolutePath() throws SecurityException;

    /**
     * Returns the name of the file or directory denoted by this abstract
     * pathname.
     * 
     * @return The name of the file or directory denoted by this abstract
     *         pathname, or the empty string if this pathname's name sequence is
     *         empty
     */
    String getName();

    /**
     * Tests whether the file denoted by this abstract pathname is a directory.
     * 
     * @return {@code true} if and only if the file denoted by this abstract
     *         pathname exists and is a directory; {@code false} otherwise
     * @throws SecurityException
     *             If a security manager exists and its
     *             {@link SecurityManager#checkRead(java.lang.String)} method
     *             denies read access to the file
     */
    boolean isDirectory() throws SecurityException;

    /**
     * Tests whether the file denoted by this abstract pathname is a normal
     * file.
     * 
     * @return {@code true} if and only if the file denoted by this abstract
     *         pathname exists and is a normal file; {@code false} otherwise
     * @throws SecurityException
     *             If a security manager exists and its
     *             {@link SecurityManager#checkRead(java.lang.String)} method
     *             denies read access to the file
     */
    boolean isFile() throws SecurityException;

    /**
     * Returns the length of the file denoted by this abstract pathname.
     * 
     * @return The length, in bytes, of the file denoted by this abstract
     *         pathname, or {@code 0L} if the file does not exist. Some
     *         operating systems may return {@code 0L} for pathnames denoting
     *         system-dependent entities such as devices or pipes.
     * @throws SecurityException
     *             If a security manager exists and its
     *             {@link SecurityManager#checkRead(java.lang.String)} method
     *             denies read access to the file
     */
    long length() throws SecurityException;

    /**
     * Returns the time that the file denoted by this abstract pathname was last
     * modified.
     * 
     * @return A long value representing the time the file was last modified,
     *         measured in milliseconds since the epoch (00:00:00 GMT, January
     *         1, 1970), or {@code 0L} if the file does not exist or if an I/O
     *         error occurs
     * @throws SecurityException
     *             If a security manager exists and its
     *             {@link SecurityManager#checkRead(java.lang.String)} method
     *             denies read access to the file
     */
    long lastModified() throws SecurityException;

    /**
     * Returns the abstract pathname of this abstract pathname's parent, or
     * {@code null} if this pathname does not name a parent directory.<br>
     * <br>
     * The <i>parent</i> of an abstract pathname consists of the pathname's
     * prefix, if any, and each name in the pathname's name sequence except for
     * the last. If the name sequence is empty then the pathname does not name a
     * parent directory.
     * 
     * @return The abstract pathname of the parent directory named by this
     *         abstract pathname, or {@code null} if this pathname does not name
     *         a parent
     */
    IFile parentFile();

    /**
     * Creates directory.
     * 
     * @return {@code true} if the directory has been created
     * @since v4.0 beta
     */
    boolean mkdir();
}
