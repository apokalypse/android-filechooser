/*
 *    Copyright (c) 2012 Hai Bison
 *
 *    See the file LICENSE at the root directory of this project for copying
 *    permission.
 */

package group.pals.android.lib.ui.filechooser.providers.localfile;

import group.pals.android.lib.ui.filechooser.BuildConfig;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.FileObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

/**
 * Extended class of {@link FileObserver}, to watch for changes of a directory
 * and notify clients of {@link LocalFileProvider} about those changes.
 * 
 * @since v5.1 beta
 * @author Hai Bison
 * 
 */
public class FileObserverEx extends FileObserver {

    private static final String _ClassName = FileObserverEx.class.getName();

    private static final int _FileObserverMask = FileObserver.CREATE
            | FileObserver.DELETE | FileObserver.DELETE_SELF
            | FileObserver.MOVE_SELF | FileObserver.MOVED_FROM
            | FileObserver.MOVED_TO | FileObserver.ATTRIB | FileObserver.MODIFY;

    private static final long _MinTimeBetweenEvents = 10000;
    private static final int _MsgNotifyChanges = 0;

    private final HandlerThread mHandlerThread = new HandlerThread(_ClassName);
    private final Handler mHandler;
    private long mLastEventTime = SystemClock.elapsedRealtime();
    private boolean mWatching = false;

    /**
     * Creates new instance.
     * 
     * @param context
     *            the context.
     * @param path
     *            the path to the directory that you want to watch for changes.
     */
    public FileObserverEx(final Context context, final String path,
            final Uri notificationUri) {
        super(path, _FileObserverMask);

        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {

            @Override
            public void handleMessage(Message msg) {
                if (BuildConfig.DEBUG)
                    Log.d(_ClassName,
                            String.format(
                                    "mHandler.handleMessage() >> path = '%s' | what = %,d",
                                    path, msg.what));

                switch (msg.what) {
                case _MsgNotifyChanges:
                    context.getContentResolver().notifyChange(notificationUri,
                            null);
                    mLastEventTime = SystemClock.elapsedRealtime();
                    break;
                }
            }// handleMessage()
        };
    }// FileObserverEx()

    @Override
    public void onEvent(int event, String path) {
        /*
         * Some bugs of Android...
         */
        if (!mWatching || event == 32768 || path == null
                || mHandler.hasMessages(_MsgNotifyChanges)
                || !mHandlerThread.isAlive() || mHandlerThread.isInterrupted())
            return;

        try {
            if (SystemClock.elapsedRealtime() - mLastEventTime <= _MinTimeBetweenEvents)
                mHandler.sendEmptyMessageDelayed(
                        _MsgNotifyChanges,
                        Math.max(
                                1,
                                _MinTimeBetweenEvents
                                        - (SystemClock.elapsedRealtime() - mLastEventTime)));
            else
                mHandler.sendEmptyMessage(_MsgNotifyChanges);
        } catch (Throwable t) {
            mWatching = false;
        }
    }// onEvent()

    @Override
    public void startWatching() {
        super.startWatching();
        mWatching = true;
    }// startWatching()

    @Override
    public void stopWatching() {
        super.stopWatching();

        if (BuildConfig.DEBUG)
            Log.d(_ClassName, "stopWatching()");

        mWatching = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR)
            HandlerThreadCompat_v5.quit(mHandlerThread);
        mHandlerThread.interrupt();
    }// stopWatching()
}
