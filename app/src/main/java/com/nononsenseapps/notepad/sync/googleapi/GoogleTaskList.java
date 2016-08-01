/*
 * Copyright (C) 2012 Jonas Kalderstam
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nononsenseapps.notepad.sync.googleapi;

import com.nononsenseapps.notepad.database.RemoteTaskList;
import com.nononsenseapps.notepad.database.TaskList;
import com.nononsenseapps.utils.time.RFC3339Date;

import android.database.Cursor;
import com.nononsenseapps.helpers.Log;

public class GoogleTaskList extends RemoteTaskList {

	private static final String TAG = "nononsenseapps";
	public static final String SERVICENAME = "googletasks";
	//public String id = null;
	private String title = null;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isRemotelyDeleted() {
        return remotelyDeleted;
    }

    public boolean isRedownload() {
        return redownload;
    }

    public void setRedownload(boolean redownload) {
        this.redownload = redownload;
    }

    private boolean remotelyDeleted = false;

	// Intended for when default list is deleted. When that fails, redownload it and its contents
	private boolean redownload = false;

	// private GoogleAPITalker api;

    public GoogleTaskList(GoogleTasksAPI.TaskListResource taskListResource, String accountName) {
        super();
        this.service = SERVICENAME;
        account = accountName;

        updateFromTaskListResource(taskListResource);
    }
	
	public GoogleTaskList(final TaskList dbList, final String accountName) {
		super();
		this.title = dbList.title;
		this.dbid = dbList._id;
		this.account = accountName;
		this.service = SERVICENAME;
	}

	public GoogleTaskList(final String accountName) {
		super();
		this.account = accountName;
		this.service = SERVICENAME;
	}
	
	public GoogleTaskList(final Cursor c) {
		super(c);
		this.service = SERVICENAME;
	}

	public GoogleTaskList(final Long dbid, final String remoteId, final Long updated, final String account) {
		super(dbid, remoteId, updated, account);
		this.service = SERVICENAME;
	}

    /**
	 * Includes title and not id
	 */
	public GoogleTasksAPI.TaskListResource toTaskListResource() {
        GoogleTasksAPI.TaskListResource taskListResource = new GoogleTasksAPI.TaskListResource();

        taskListResource.title = title;

        return taskListResource;
	}

    /**
     * Update all fields from the resource
     */
    public void updateFromTaskListResource(GoogleTasksAPI.TaskListResource taskListResource) {
        remoteId = taskListResource.id;
        title = taskListResource.title;

        try {
            updated = RFC3339Date.parseRFC3339Date(taskListResource.updated).getTime();
        }
        catch (Exception e) {
            Log.d(TAG, e.getLocalizedMessage());
            updated = 0L;
        }
    }

	/**
	 * Returns true if the TaskList has the same remote id or the same database
	 * id.
	 */
	@Override
	public boolean equals(Object o) {
		boolean equal = false;
		if (GoogleTaskList.class.isInstance(o)) {
			// It's a list!
			GoogleTaskList list = (GoogleTaskList) o;
			if (dbid != -1 && dbid == list.dbid) {
				equal = true;
			}
			if (remoteId != null && remoteId.equals(list.remoteId)) {
				equal = true;
			}
		}
		return equal;
	}

    public void setRemotelyDeleted(boolean remotelyDeleted) {
        this.remotelyDeleted = remotelyDeleted;
    }

    public boolean getRemotelyDeleted() {
        return remotelyDeleted;
    }
}
