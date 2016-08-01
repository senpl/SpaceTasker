package com.nononsenseapps.notepad.sync.googleapi;

import android.content.Context;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Pair;

import com.nononsenseapps.notepad.database.Task;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;

import static com.nononsenseapps.notepad.sync.googleapi.GoogleTaskSync.synchronizeTasksRemotely;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
        GoogleTaskList gTaskList = new GoogleTaskList(1L, "remote1", 0L, "bob@gmail.com");
        GoogleTasksClient client = mock(GoogleTasksClient.class);
        List<Pair<Task, GoogleTask>> taskPairs = new ArrayList<Pair<Task, GoogleTask>>();
        Task task = new Task();
        GoogleTask gTask = mock(GoogleTask.class);
        taskPairs.add(new Pair<Task, GoogleTask>(task, gTask));

        // Deleted locally
        when(gTask.isDeleted()).thenReturn(true);

        RetrofitError error = mock(RetrofitError.class);
        doThrow(error).when(client).deleteTask(any(GoogleTask.class), any(GoogleTaskList.class));

        synchronizeTasksRemotely(context, taskPairs, gTaskList, client);

        // Verify that it was deleted locally
        verify(gTask).delete(context);
    }
}