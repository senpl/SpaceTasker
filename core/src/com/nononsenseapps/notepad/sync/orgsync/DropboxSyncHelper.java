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

import android.content.Context;
import android.preference.PreferenceManager;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;
import com.nononsenseapps.build.Config;
import com.nononsenseapps.notepad.prefs.SyncPrefs;

public class DropboxSyncHelper {

    /**
     * Returns a Dropbox API object which is used for synchronization.
     */
    public static DropboxAPI<AndroidAuthSession> getDBApi(final Context context) {
        final DropboxAPI<AndroidAuthSession> mDBApi;

        // And later in some initialization function:
        final AppKeyPair appKeys = new AppKeyPair(Config.getKeyDropboxSyncPublic
                (context),
                Config.getKeyDropboxSyncSecret(context));
        final AndroidAuthSession session;

        if (PreferenceManager.getDefaultSharedPreferences(context).contains
                (SyncPrefs.KEY_DROPBOX_TOKEN)) {
            session = new AndroidAuthSession(appKeys,
                    PreferenceManager.getDefaultSharedPreferences(context)
                            .getString(SyncPrefs.KEY_DROPBOX_TOKEN, ""));
        } else {
            session = new AndroidAuthSession(appKeys);
        }
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
        return mDBApi;
    }
}
