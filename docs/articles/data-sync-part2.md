![](/images/screenshot.png)

Let's say we are working on [Notes](https://github.com/dmytrodanylyk/android-data-sync-sample) application, where user can:

- see list of notes
- create new notes

All those data should be synced with server. In our sample application we [simulate](https://github.com/dmytrodanylyk/android-data-sync-sample/blob/master/app%2Fsrc%2Fmain%2Fjava%2Fcom%2Ftodo%2Fsync%2FNotesSync.java#L74) data uploading during *POST* request and [generate random items](https://github.com/dmytrodanylyk/android-data-sync-sample/blob/master/app%2Fsrc%2Fmain%2Fjava%2Fcom%2Ftodo%2Fsync%2FNotesSync.java#L53) during *GET* request.

!!! note
    Application source code is available on [GitHub](https://github.com/dmytrodanylyk/android-data-sync-sample).
### Storage layer

For persistant storage we are going to use [Realm](https://realm.io) because of lazy loading it's fit perfectly in our app.

[NotesStorage](https://github.com/dmytrodanylyk/android-data-sync-sample/blob/master/app%2Fsrc%2Fmain%2Fjava%2Fcom%2Ftodo%2Fdb%2FNotesStorage.java) class contains all necessary methods to save and retrieve notes. If [Note](https://github.com/dmytrodanylyk/android-data-sync-sample/blob/master/app%2Fsrc%2Fmain%2Fjava%2Fcom%2Ftodo%2Fdata%2FNote.java) is marked as `isModified` - it means we need to upload this item to server.

```java
public class Note extends RealmObject {

    @PrimaryKey
    private String id;
    private String title;
    private Date createdDate;
    private boolean isModified;
}
```
### Communication layer

To make communication between *Sync Service* and *User Interface* we are going to use [Green Robot Event Bus v3.0](https://github.com/greenrobot/EventBus)

**SyncRequestEvent**

When we want to request sync from *User Interface* we need to send [SyncRequestEvent](https://github.com/dmytrodanylyk/android-data-sync-sample/blob/master/app%2Fsrc%2Fmain%2Fjava%2Fcom%2Ftodo%2Fsync%2Fevent%2FSyncRequestEvent.java). In sample application we have 2 such places:

- when user trigger pull to refresh

```java
swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
    @Override
    public void onRefresh() {
        SyncService.request(SyncType.NOTES);
    }
});
```

- when user create new note

```java
void onCreateNoteClicked() {
    ...
    SyncService.request(SyncType.NOTES);
    ...
}
```

`SyncService` has a static helper method `request` which just send `SyncRequestEvent` via event bus.

```java
public static void request(@NonNull SyncType type) {
    EventBusManager.send(new SyncRequestEvent(type));
}
```

**SyncEvent**

When synchronization is in progress or completed *Sync Service* send [SyncEvent](https://github.com/dmytrodanylyk/android-data-sync-sample/blob/master/app%2Fsrc%2Fmain%2Fjava%2Fcom%2Ftodo%2Fsync%2Fevent%2FSyncEvent.java) with [SyncType](https://github.com/dmytrodanylyk/android-data-sync-sample/blob/master/app%2Fsrc%2Fmain%2Fjava%2Fcom%2Ftodo%2Fsync%2Fevent%2FSyncType.java) and [SyncStatus](https://github.com/dmytrodanylyk/android-data-sync-sample/blob/master/app%2Fsrc%2Fmain%2Fjava%2Fcom%2Ftodo%2Fsync%2Fevent%2FSyncStatus.java).

```java
void sync() {
    if (NetworkUtils.isNetworkConnected(context)) {
        SyncEvent.send(getSyncType(), SyncStatus.IN_PROGRESS);
        post();
        get();
        SyncEvent.send(getSyncType(), SyncStatus.COMPLETED);
    }
}
```

*User Interface* is listening for this event to know when to display progress bar or invalidate data.

```java
public void onEvent(SyncEvent event) {
    if(event.getType() == SyncType.NOTES) {
        if (event.getStatus() == SyncStatus.IN_PROGRESS) {
            swipeView.setRefreshing(true);
        } else if (event.getStatus() == SyncStatus.COMPLETED) {
            swipeView.setRefreshing(false);
            adapter.notifyDataSetChanged();
        }  
    }
}
```

### Sync layer

Sync layer is a simple [service](https://github.com/dmytrodanylyk/android-data-sync-sample/blob/master/app%2Fsrc%2Fmain%2Fjava%2Fcom%2Ftodo%2Fsync%2FSyncService.java) with few helper classes which execute requests in the background via *Executor*. You can also use *Sync Adapter* or *GCM Network Manager* to make it smarter, please check [slides](http://slides.com/dmytrodanylyk/android-data-sync#/12) (press arrow down) for more details.

It listen for a [SyncRequestEvent](https://github.com/dmytrodanylyk/android-data-sync-sample/blob/master/app%2Fsrc%2Fmain%2Fjava%2Fcom%2Ftodo%2Fsync%2Fevent%2FSyncRequestEvent.java) and view [SyncManager](https://github.com/dmytrodanylyk/android-data-sync-sample/blob/master/app%2Fsrc%2Fmain%2Fjava%2Fcom%2Ftodo%2Fsync%2FSyncManager.java) class decide which type of sync we need to call.

```java
public class SyncService extends Service {

    private ExecutorService executor = null;
    private SyncManager syncManager = null;

    @Override
    public void onCreate() {
        executor = Executors.newSingleThreadExecutor();
        syncManager = new SyncManager(getApplicationContext());
        EventBusManager.register(this);
    }

    @Override
    public void onDestroy() {
        EventBusManager.unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void onEvent(@NonNull final SyncRequestEvent event) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                syncManager.doSync(event.getSyncType());
            }
        });
    }
}
```

In our example [NotesSync](https://github.com/dmytrodanylyk/android-data-sync-sample/blob/master/app%2Fsrc%2Fmain%2Fjava%2Fcom%2Ftodo%2Fsync%2FNotesSync.java) class contain implementation of *POST* and *GET* methods. Since we don't have real server implementation is following:

* *POST* - method grab all items which are marked as `isModified`, simulate request delay and mark them as not modified (as if they were successfully uploaded to server).

```java
@Override
protected void post() {
    Realm realm = Realm.getDefaultInstance();
    List<Note> noteList = NotesStorage.getAllModified(realm);
    if (!noteList.isEmpty()) {
        L.d("Notes POST request start");
        L.v("%d modified items need to be uploaded to server", noteList.size());
        log(noteList);
        simulateRequestDelay();
        NotesStorage.markAsNotModified(mapToIdList(noteList));
        L.d("Notes POST request end");
    }
    realm.close();
}
```

* *GET* - method simulate request delay, generate random amount of items and save them to database (as if there were new items available on server).

```java
@Override
protected void get() {
    L.d("Notes GET request start");
    simulateRequestDelay();
    List<Note> noteList = generateNoteItems();
    L.v("%d new items available", noteList.size());
    log(noteList);
    NotesStorage.save(noteList);
    L.d("Notes GET request end");
}
```

### User Interface layer

In our application we have two screens:

* [NotesActivity](https://github.com/dmytrodanylyk/android-data-sync-sample/blob/master/app%2Fsrc%2Fmain%2Fjava%2Fcom%2Ftodo%2Fui%2Fnotes%2FNotesActivity.java) - where user can see list of notes and perform pull to refresh.
* [NewNoteActivity](https://github.com/dmytrodanylyk/android-data-sync-sample/blob/master/app%2Fsrc%2Fmain%2Fjava%2Fcom%2Ftodo%2Fui%2Fnotes%2FNewNoteActivity.java) - where user can create new note.


### How all layers works together?

Let's try to describe what happens when user create new note - step by step.

**Step 1**

When user press *create* button we save new note to realm database (1) and send *SyncEvent* (2) with *SyncType.NOTES* and *SyncStatus.COMPLETED* to tell all screens which are displaying notes to reload data from realm (4) - in our case it's *NotesActivity*.

Then we send *SyncRequestEvent* (3) to trigger synchronization.

```java
// NewNoteActivity.class

void onCreateNoteClicked() {
    String noteTitle = editNoteTitle.getText().toString();
    saveNewNote(noteTitle); // 1
    SyncEvent.send(SyncType.NOTES, SyncStatus.COMPLETED); // 2
    SyncService.request(SyncType.NOTES); // 3
    finish();
}
```

```java
// NotesActivity.class

public void onEvent(SyncEvent event) {
    if(event.getType() == SyncType.NOTES) {
        if (event.getStatus() == SyncStatus.IN_PROGRESS) {
            swipeView.setRefreshing(true);
        } else if (event.getStatus() == SyncStatus.COMPLETED) { // 4
            swipeView.setRefreshing(false);
            adapter.notifyDataSetChanged();
        }  
    }
}
```

**Step 2**

If internet is available *SyncService* send *SyncEvent* (1) with *SyncType.NOTES* and *SyncStatus.IN_PROGRESS* to indicate that notes synchronization is in progress right now.

```java
// AbsSync.class

void sync() {
    if (NetworkUtils.isNetworkConnected(context)) {
        SyncEvent.send(getSyncType(), SyncStatus.IN_PROGRESS); // 1
        post();
        get();
        SyncEvent.send(getSyncType(), SyncStatus.COMPLETED); // 2
    }
}
```

When *NotesActivity* catch this event it displays loading indicator (3).

```java
// NotesActivity.class

public void onEvent(SyncEvent event) {
    if(event.getType() == SyncType.NOTES) {
        if (event.getStatus() == SyncStatus.IN_PROGRESS) { // 3
            swipeView.setRefreshing(true);
        } else if (event.getStatus() == SyncStatus.COMPLETED) {
            swipeView.setRefreshing(false);
            adapter.notifyDataSetChanged();
        }  
    }
}
```

Then it perform *GET* and *POST* requests and update data in database.

Finally it send *SyncEvent* (2) with *SyncType.NOTES* and *SyncStatus.COMPLETED* to tell all screens which are displaying notes to reload data from realm (4) - in our case it's *NotesActivity*.

```java
// NotesActivity.class

public void onEvent(SyncEvent event) {
    if(event.getType() == SyncType.NOTES) {
        if (event.getStatus() == SyncStatus.IN_PROGRESS) {
            swipeView.setRefreshing(true);
        } else if (event.getStatus() == SyncStatus.COMPLETED) { // 4
            swipeView.setRefreshing(false);
            adapter.notifyDataSetChanged();
        }  
    }
}
```

!!! info
    Such android data synchronization model is used in my current project and behaves quite well. Feel free to ask any question.
