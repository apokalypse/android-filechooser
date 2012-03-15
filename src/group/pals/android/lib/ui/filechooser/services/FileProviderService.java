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
}
