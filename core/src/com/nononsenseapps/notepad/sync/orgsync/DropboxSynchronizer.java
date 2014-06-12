/*
 * Copyright (c) 2014 Jonas Kalderstam.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nononsenseapps.notepad.sync.orgsync;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxServerException;
import com.nononsenseapps.build.Config;
import com.nononsenseapps.notepad.prefs.SyncPrefs;

import org.cowboyprogrammer.org.OrgFile;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;


public class DropboxSynchronizer extends Synchronizer implements
        SynchronizerInterface {

    // Where files are kept. User changeable in preferences.
    public static final String DEFAULT_DIR = "/NoNonsenseNotes/";
    public static final String PREF_DIR = SyncPrefs.KEY_DROPBOX_DIR;
    public static final String PREF_ENABLED = SyncPrefs.KEY_DROPBOX_ENABLE;
    public final static String SERVICENAME = "DROPBOXORG";
    protected final boolean enabled;
    private final DropboxAPI<AndroidAuthSession> dbApi;
    protected File DIR;

    public DropboxSynchronizer(final Context context) {
        super(context);
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        dbApi = DropboxSyncHelper.getDBApi(context);
        enabled = prefs.getBoolean(PREF_ENABLED, false) && dbApi.getSession()
                .isLinked();
        DIR = new File(prefs.getString(PREF_DIR, DEFAULT_DIR));
    }

    /**
     * @return A unique name for this service. Should be descriptive, like
     * DropboxOrg, SDOrg or SSHOrg.
     */
    @Override
    public String getServiceName() {
        return SERVICENAME;
    }

    /**
     * @return The username of the configured service. Likely an e-mail.
     */
    @Override
    public String getAccountName() {
        try {
            return Long.toString(dbApi.accountInfo().uid);
        } catch (DropboxException e) {
            return null;
        }
    }

    /**
     * Returns true if the synchronizer has been configured. This is called
     * before synchronization. It will be true if the user has selected an
     * account, folder etc...
     */
    @Override
    public boolean isConfigured() {
        if (!enabled) return false;

        // Need to ask dropbox if we are linked.
        if (dbApi.getSession().isLinked()) {
                try {
                    if (!dbApi.metadata(DIR.getPath(), 1, null, false,
                            null).isDir) {
                        dbApi.createFolder(DIR.getPath());
                    }
                } catch (DropboxException e) {
                    return false;
                }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns an OrgFile object with a filename set that is guaranteed to
     * not already exist. Use this method to avoid having multiple objects
     * pointing to the same file.
     *
     * @param desiredName The name you'd want. If it exists,
     *                    it will be used as the base in desiredName1,
     *                    desiredName2, etc. Limited to 99.
     * @return an OrgFile guaranteed not to exist.
     * @throws java.io.IOException
     * @throws IllegalArgumentException
     */
    @Override
    public OrgFile getNewFile(final String desiredName) throws IOException, IllegalArgumentException {
        if (desiredName.contains("/")) {
            throw new IOException("Filename can't contain /");
        }
        String filename;
        try {
            for (int i = 0; i < 100; i++) {
                if (i == 0) {
                    filename = desiredName + ".org";
                } else {
                    filename = desiredName + i + ".org";
                }

                boolean exists = true;
                try {
                    DropboxAPI.Entry entry =
                            dbApi.metadata(new File(DIR, filename).getPath(), 1,
                                    null, false, null);
                    exists = entry.isDeleted;
                } catch (DropboxServerException e) {
                    exists = false;
                }

                if (!exists) {
                    return new OrgFile(filename);
                }
            }
        } catch (DropboxException e) {
            throw new IOException(e);
        }
        throw new IllegalArgumentException("Filename not accessible");
    }

    /**
     * Replaces the file on the remote end with the given content.
     *
     * @param orgFile The file to save. Uses the filename stored in the object.
     */
    @Override
    public void putRemoteFile(final OrgFile orgFile) throws IOException {
        try {
            final File file = new File(DIR, orgFile.getFilename());
            final byte[] bytes = orgFile.treeToString().getBytes("UTF-8");
            final ByteArrayInputStream inputStream = new ByteArrayInputStream
                    (bytes);
            dbApi.putFileOverwrite(file.getPath(), inputStream, bytes.length,
                    null);
            inputStream.close();
        } catch (DropboxException e) {
            throw new IOException(e.getLocalizedMessage());
        }
    }

    /**
     * Delete the file on the remote end.
     *
     * @param orgFile The file to delete.
     */
    @Override
    public void deleteRemoteFile(final OrgFile orgFile) {
        if (orgFile == null) {
            // Nothing to do
            return;
        }
        String path = new File(DIR, orgFile.getFilename()).getPath();
        try {
            dbApi.delete(path);
        } catch (DropboxException e) {
            //e.printStackTrace();
        }
    }

    /**
     * Rename the file on the remote end.
     *
     * @param oldName The name it is currently stored as on the remote end.
     * @param orgFile
     */
    @Override
    public void renameRemoteFile(final String oldName, final OrgFile orgFile) {
        String newPath = new File(DIR, orgFile.getFilename()).getPath();
        String oldPath = new File(DIR, oldName).getPath();

        try {
            dbApi.move(oldPath, newPath);
        } catch (DropboxException e) {
           // e.printStackTrace();
        }
    }

    /**
     * Returns a BufferedReader to the remote file. Null if it doesn't exist.
     *
     * @param filename Name of the file, without path
     */
    @Override
    public BufferedReader getRemoteFile(final String filename) {
        String path = new File(DIR, filename).getPath();
        BufferedReader br = null;
        try {
            DropboxAPI.Entry entry = dbApi.metadata(path, 1, null, false, null);
            if (!entry.isDir && !entry.isDeleted) {
                ByteArrayOutputStream outputStream = new
                        ByteArrayOutputStream();
                DropboxAPI.DropboxFileInfo info = dbApi.getFile(path, null,
                        outputStream, null);
                // Read it
                br = new BufferedReader(new StringReader(outputStream.toString(
                        "UTF-8")));
                outputStream.close();
            }
        } catch (IOException e) {
            Log.d(TAG, e.getLocalizedMessage());
            br = null;
        } catch (DropboxException e) {
            Log.d(TAG, e.getLocalizedMessage());
            br = null;
        }

        return br;
    }

//    /**
//     * Wait until the file has been synced to the newest state. Will wait a
//     * maximum of 30s.
//     * @param file
//     */
//    private void waitUntilSynced(final DbxFile file) {
//        final long MAXTIME = 30*1000;
//        final long STARTTIME = System.currentTimeMillis();
//        try {
//            DbxFileStatus status = file.getNewerStatus();
//
//            while (MAXTIME > (System.currentTimeMillis() - STARTTIME) &&
//                    status != null && !status.isCached) {
//                Log.d(TAG, "Waiting on latest version: " + status
//                        .bytesTransferred + " / " + status.bytesTotal);
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException ignored) {
//                }
//                // Check latest
//                status = file.getNewerStatus();
//            }
//            // Update
//            file.update();
//        } catch (DbxException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * @return a set of all remote files.
     */
    @Override
    public HashSet<String> getRemoteFilenames() {
        final HashSet<String> filenames = new HashSet<String>();
        try {
            DropboxAPI.Entry dirEntry = dbApi.metadata(DIR.getPath(), 0, null, true,
                    null);
            for (DropboxAPI.Entry entry: dirEntry.contents) {
                if (entry.fileName().toLowerCase().endsWith(".org")) {
                    if (entry.isDeleted || entry.isDir) {
                        Log.d(TAG, "Caught invalid file: " + entry.fileName());
                    } else {
                        Log.d(TAG, "Adding: " + entry.fileName());
                        filenames.add(entry.fileName());
                    }
                }
            }
        } catch (DropboxException e) {
            Log.d(TAG, e.getLocalizedMessage());
        }
        return filenames;
    }

    /**
     * Use this to disconnect from any services and cleanup.
     */
    @Override
    public void postSynchronize() {

    }

    @Override
    public Monitor getMonitor() {
        return null;
    }

//    public final class DropboxMonitor implements Monitor,
//    DbxFileSystem.PathListener {
//
//        private DbxFileSystem fs;
//        private final DbxPath dir;
//        private OrgSyncService.SyncHandler handler;
//
//        public DropboxMonitor(final DbxFileSystem fs, final DbxPath dir) {
//            this.fs = fs;
//            this.dir = dir;
//        }
//
//        @Override
//        public void startMonitor(final OrgSyncService.SyncHandler handler) {
//            this.handler = handler;
//            if (fs != null) {
//                fs.addPathListener(this, dir, DbxFileSystem.PathListener.Mode.PATH_OR_CHILD);
//            }
//        }
//
//        @Override
//        public void pauseMonitor() {
//            if (fs != null) {
//                fs.removePathListenerForAll(this);
//            }
//        }
//
//        @Override
//        public void terminate() {
//            pauseMonitor();
//            fs = null;
//        }
//
//        @Override
//        public void onPathChange(final DbxFileSystem dbxFileSystem,
//                final DbxPath dbxPath, final Mode mode) {
//            if (handler != null) {
//                handler.onMonitorChange();
//            }
//        }
//    }
}
