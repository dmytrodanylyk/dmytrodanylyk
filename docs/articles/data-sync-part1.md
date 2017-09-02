### What is data synchronization?

!!! info
    "Data synchronization - is the process of establishing consistency among data from a source to a target data storage and vice versa and the continuous harmonization of the data over time." - Wikipedia

In current synchronization schema we need 4 layers:

- Sync
- Storage
- Communication
- User Interface

We will dive into layer details later, the most important is *Sync* layer - think about it as some kind of [Service](http://developer.android.com/guide/components/services.html) which perform operations in background.

### Downloading data from server

Diagram below illustrate our sync process when we need to download data from server.

<iframe src="//slides.com/dmytrodanylyk/android-data-sync/embed#/2/6/" width="700" height="600" scrolling="no" frameborder="0"></iframe>

**Step 1**<br>
Data synchronization can occur automatically (*server send us GCM notification to tell that data on server has been updated*) or manually (*user perform pull to refresh which trigger synchronization*).

**Step 2**<br>
*Sync* service resolve which type of synchronization we need to do and perform requests to server, parse request result and save data to database.

**Step 3**<br>
When synchronization is completed and database is updated, *Sync* service send global application even (*via event bus or local broadcast*) to tell that *Table A*, *B* and *C* was updated.

**Step 4**<br>
User interface - *Activity A*, *B* and *C* are only displaying data from database. To know whenever data of *Table A*, *B* and *C* changes they are subscribed to global application events. When such event occurs, user interface reload data from database.

### Uploading data to server

Diagram below illustrate our sync process when we need to upload data to server.

<iframe src="//slides.com/dmytrodanylyk/android-data-sync/embed#/3/4" width="700" height="600" scrolling="no" frameborder="0"></iframe>

**Step 1**<br>
When user change some data (*update his profile*) we need to save or update data in database and trigger synchronization.

**Step 2**<br>
*Sync* service resolve which data we need to upload to server, parse request result and update data in database.

**Step 3** (*same as during downloading data from server*) <br>
When synchronization is completed and database is updated, *Sync* service send global application even (*via event bus or local broadcast*) to tell that *Table A*, *B* and *C* was updated.

**Step 4** (*same as during downloading data from server*) <br>
User interface - *Activity A*, *B* and *C* are only displaying data from database. To know whenever data of *Table A*, *B* and *C* changes they are subscribed to global application events. When such event occurs, user interface reload data from database.


### What we know so far?

- User Interface displays data from database only.
- Sync service made requests to server, update database and send global application event.

> In next part of this articles series we'll dive into layer details.
