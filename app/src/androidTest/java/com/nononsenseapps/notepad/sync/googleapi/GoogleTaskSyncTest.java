package com.nononsenseapps.notepad.sync.googleapi;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Pair;

import com.nononsenseapps.notepad.database.Task;

import java.util.ArrayList;
import java.util.List;

import static com.nononsenseapps.notepad.sync.googleapi.GoogleTaskSync.synchronizeTasksRemotely;
import static org.mockito.Mockito.mock;

public class GoogleTaskSyncTest extends AndroidTestCase {

    private Context context;

    @Override
    public void setUp() throws Exception {
        context = getContext();
        System.setProperty(
                "dexmaker.dexcache",
                context.getCacheDir().getPath());
    }

    @SmallTest
    public void testDeleteTask404() throws Exception {
        List<Pair<Task, GoogleTask>> taskPairs = new ArrayList<Pair<Task, GoogleTask>>();
        GoogleTaskList gTaskList = new GoogleTaskList(1L, "remote1", 0L, "bob@gmail.com");
        GoogleTasksClient client = mock(GoogleTasksClient.class);

        synchronizeTasksRemotely(context, taskPairs, gTaskList, client);
    }
}