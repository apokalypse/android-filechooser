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

package group.pals.android.lib.ui.filechooser.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * Base service for a file provider.
 * 
 * @author Hai Bison
 * @since v3.1
 */
public abstract class FileProviderService extends Service implements
        IFileProvider {

    /*-------------------------------------------------------------------
     * Service
     */

    @Override
    public IBinder onBind(Intent intent) {
        return fBinder;
    }

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {

        public IFileProvider getService() {
            return FileProviderService.this;
        }
    }// LocalBinder

    // This is the object that receives interactions from clients. See
    // RemoteService for a more complete example.
    private final IBinder fBinder = new LocalBinder();

    /*-------------------------------------------------------------------
     * IFileProvider
     */

    private boolean displayHiddenFiles = false;
    private String regexFilenameFilter = null;
    private FilterMode filterMode = FilterMode.FilesOnly;
    private int maxFileCount = 1024;
    private SortType sortType = SortType.SortByName;
    private SortOrder sortOrder = SortOrder.Ascending;

    @Override
    public void setDisplayHiddenFiles(boolean display) {
        displayHiddenFiles = display;
    };

    @Override
    public boolean isDisplayHiddenFiles() {
        return displayHiddenFiles;
    }

    @Override
    public void setRegexFilenameFilter(String regex) {
        regexFilenameFilter = regex;
    };

    @Override
    public String getRegexFilenameFilter() {
        return regexFilenameFilter;
    }

    @Override
    public void setFilterMode(FilterMode fm) {
        filterMode = fm;
    }

    @Override
    public FilterMode getFilterMode() {
        return filterMode;
    }

    @Override
    public void setSortType(SortType st) {
        sortType = st;
    }

    @Override
    public SortType getSortType() {
        return sortType;
    }

    @Override
    public void setSortOrder(SortOrder so) {
        sortOrder = so;
    }

    @Override
    public SortOrder getSortOrder() {
        return sortOrder;
    }

    @Override
    public void setMaxFileCount(int max) {
        maxFileCount = max;
    };

    @Override
    public int getMaxFileCount() {
        return maxFileCount;
    }
}
