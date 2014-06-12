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

package com.nononsenseapps.filepicker;

import android.annotation.SuppressLint;
import android.content.AsyncTaskLoader;
import android.content.Loader;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.nononsenseapps.notepad.core.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SuppressLint("ValidFragment")
public class DropboxFilePickerFragment
        extends AbstractFilePickerFragment<DropboxAPI.Entry> {

    private final DropboxAPI<AndroidAuthSession> dbApi;
    private FolderCreator folderCreator;

    public DropboxFilePickerFragment(
            final DropboxAPI<AndroidAuthSession> dbApi) {
        super();
        if (dbApi == null) {
            throw new NullPointerException("FileSystem may not be null");
        } else if (!dbApi.getSession().isLinked()) {
            throw new IllegalArgumentException("Must be linked with Dropbox");
        }
        this.dbApi = dbApi;
    }

    /**
     * Return true if the path is a directory and not a file.
     *
     * @param path
     */
    @Override
    protected boolean isDir(final DropboxAPI.Entry path) {
        return path.isDir;
    }

    /**
     * Return the path to the parent directory. Should return the root if
     * from is root.
     *
     * @param from
     */
    @Override
    protected DropboxAPI.Entry getParent(final DropboxAPI.Entry from) {
        // Take care of a slight limitation in Dropbox code:
        if (from.path.length() > 1 && from.path.endsWith("/")) {
            from.path = from.path.substring(0, from.path.length() - 1);
        }
        String parent = from.parentPath();
        if (parent.isEmpty()) {
            parent = "/";
        }

        return getPath(parent);
    }

    /**
     * Convert the path to the type used.
     *
     * @param path
     */
    @Override
    protected DropboxAPI.Entry getPath(final String path) {
        final DropboxAPI.Entry entry = new DropboxAPI.Entry();
        entry.path = path;
        entry.isDir = true;
        return entry;
    }

    /**
     * @param entry
     * @return the full path to the file
     */
    @Override
    protected String getFullPath(final DropboxAPI.Entry entry) {
        return entry.path;
    }

    /**
     * @param path
     * @return the name of this file/folder
     */
    @Override
    protected String getName(final DropboxAPI.Entry path) {
        return path.fileName();
    }

    /**
     * Get the root path (lowest allowed).
     */
    @Override
    protected DropboxAPI.Entry getRoot() {
        return getPath("/");
    }

    /**
     * Convert the path to a URI for the return intent
     *
     * @param entry
     * @return
     */
    @Override
    protected Uri toUri(final DropboxAPI.Entry entry) {
        return new Uri.Builder().scheme("dropbox").path(entry.path).build();
    }

    /**
     * @return a comparator that can sort the items alphabetically
     */
    @Override
    protected Comparator<DropboxAPI.Entry> getComparator() {
        return new Comparator<DropboxAPI.Entry>() {
            @Override
            public int compare(final DropboxAPI.Entry lhs,
                    final DropboxAPI.Entry rhs) {
                if (lhs.isDir && !rhs.isDir) {
                    return -1;
                } else if (rhs.isDir && !lhs.isDir) {
                    return 1;
                } else {
                    return lhs.fileName().toLowerCase()
                            .compareTo(rhs.fileName().toLowerCase());
                }
            }
        };
    }

    @Override
    protected Loader<List<DropboxAPI.Entry>> getLoader() {
        return new AsyncTaskLoader<List<DropboxAPI.Entry>>(getActivity()) {

            @Override
            public List<DropboxAPI.Entry> loadInBackground() {
                ArrayList<DropboxAPI.Entry> files =
                        new ArrayList<DropboxAPI.Entry>();
                try {
                    if (!dbApi.metadata(currentPath.path, 1, null, false,
                            null).isDir) {
                        currentPath = getRoot();
                    }

                    DropboxAPI.Entry dirEntry =
                            dbApi.metadata(currentPath.path, 0, null, true,
                                    null);
                    for (DropboxAPI.Entry entry : dirEntry.contents) {
                        if ((mode == MODE_FILE || mode == MODE_FILE_AND_DIR) ||
                            entry.isDir) {
                            files.add(entry);
                        }
                    }
                } catch (DropboxException e) {
                }
                return files;
            }

            /**
             * Handles a request to start the Loader.
             */
            @Override
            protected void onStartLoading() {
                super.onStartLoading();

                if (currentPath == null || !currentPath.isDir) {
                    currentPath = getRoot();
                }

                forceLoad();
            }

            /**
             * Handles a request to completely reset the Loader.
             */
            @Override
            protected void onReset() {
                super.onReset();
            }
        };
    }

    @Override
    public void onNewFolder(final String name) {
        File folder = new File(currentPath.path, name);

        if (folderCreator == null) {
            folderCreator = new FolderCreator();
        }

        folderCreator.execute(folder.getPath());

        //        try {
        //            dbApi.createFolder(folder.getPath());
        //            currentPath = folder;
        //            refresh();
        //        } catch (DropboxException e) {
        //            Toast.makeText(getActivity(), R.string.create_folder_error,
        //                    Toast.LENGTH_SHORT).show();
        //        }

    }

    private class FolderCreator extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(final String... paths) {
            for (String path : paths) {
                try {
                    dbApi.createFolder(path);
                    currentPath = dbApi.metadata(path, 1, null, false, null);
                    refresh();
                } catch (DropboxException e) {
                    Toast.makeText(getActivity(), R.string.create_folder_error,
                            Toast.LENGTH_SHORT).show();
                }
            }
            return null;
        }
    }
}