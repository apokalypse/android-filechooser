/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.providers.basefile;

import android.content.ContentProvider;

/**
 * Base provider for files.
 * 
 * @since v5.1 beta
 * @author Hai Bison
 * 
 */
public abstract class BaseFileProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        return true;
    }// onCreate()
}
