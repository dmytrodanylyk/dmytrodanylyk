Assuming you have your own [SQLiteOpenHelper](http://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper.html).

```prettyprint
public class DatabaseHelper extends SQLiteOpenHelper { ... }
```

Now you want to write data to database in separate threads.

```prettyprint
 // Thread 1
 Context context = getApplicationContext();
 DatabaseHelper helper = new DatabaseHelper(context);
 SQLiteDatabase database = helper.getWritableDatabase();
 database.insert(…);
 database.close();

 // Thread 2
 Context context = getApplicationContext();
 DatabaseHelper helper = new DatabaseHelper(context);
 SQLiteDatabase database = helper.getWritableDatabase();
 database.insert(…);
 database.close();
```

You will get following message in your *logcat* and one of your changes will not be written.

```prettyprint
android.database.sqlite.SQLiteDatabaseLockedException: database is locked (code 5)
```

This is happening because every time you create new `SQLiteOpenHelper` object you are actually making **new database connection**. If you try to write to the database from actual distinct connections at the same time, one will fail.

>To use database with multiple threads we need to make sure we are using one database connection.

Let’s make singleton class `DatabaseManager` which will hold and return single `SQLiteOpenHelper` object.

```prettyprint
public class DatabaseManager {

    private static DatabaseManager instance;
    private static SQLiteOpenHelper mDatabaseHelper;

    public static synchronized void initializeInstance(SQLiteOpenHelper helper) {
        if (instance == null) {
            instance = new DatabaseManager();
            mDatabaseHelper = helper;
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(DatabaseManager.class.getSimpleName() +
                    " is not initialized, call initialize(..) method first.");
        }

        return instance;
    }

    public synchronized SQLiteDatabase getDatabase() {
        return mDatabaseHelper.getWritableDatabase();
    }

}
```

Updated code which write data to database in separate threads will look like this.

```prettyprint
// In your application class
DatabaseManager.initializeInstance(new DatabaseHelper());

// Thread 1
DatabaseManager manager = DatabaseManager.getInstance();
SQLiteDatabase database = manager.getDatabase()
database.insert(…);
database.close();

// Thread 2
DatabaseManager manager = DatabaseManager.getInstance();
SQLiteDatabase database = manager.getDatabase()
database.insert(…);
database.close();
```

This will bring you another crash.

```prettyprint
java.lang.IllegalStateException: attempt to re-open an already-closed object: SQLiteDatabase
```

Since we are using only one database connection, method `getDatabase()` return same instance of `SQLiteDatabase` object for *Thread1* and *Thread2*. What is happening, *Thread1* may close database, while *Thread2* is still using it. That’s why we have `IllegalStateException` crash.

We need to make sure no-one is using database and only then close it. Some folks on [stackoveflow](http://stackoverflow.com/) recommended to never close your *SQLiteDatabase*. This will honor you with following logcat message. So I don't think this is good idea at all.

```prettyprint
Leak found
Caused by: java.lang.IllegalStateException: SQLiteDatabase created and never closed
```

### Working sample

One possible solution is to make counter to track opening / closing database connection.

```prettyprint
public class DatabaseManager {

    private AtomicInteger mOpenCounter = new AtomicInteger();

    private static DatabaseManager instance;
    private static SQLiteOpenHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;

    public static synchronized void initializeInstance(SQLiteOpenHelper helper) {
        if (instance == null) {
            instance = new DatabaseManager();
            mDatabaseHelper = helper;
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(DatabaseManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }

        return instance;
    }

    public synchronized SQLiteDatabase openDatabase() {
        if(mOpenCounter.incrementAndGet() == 1) {
            // Opening new database
            mDatabase = mDatabaseHelper.getWritableDatabase();
        }
        return mDatabase;
    }

    public synchronized void closeDatabase() {
        if(mOpenCounter.decrementAndGet() == 0) {
            // Closing database
            mDatabase.close();

        }
    }
}
```

And use it as follows.

```prettyprint
SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
database.insert(...);
// database.close(); Don't close it directly!
DatabaseManager.getInstance().closeDatabase(); // correct way
```

Every time you need database you should call `openDatabase()` method of `DatabaseManager` class. Inside this method, we have a counter, which indicate how many times database is opened. If it equals to one, it means we need to create new database connection, if not, database connection is already established.

The same happens in `closeDatabase()` method. Every time we call this method, counter is decreased, whenever it goes to zero, we are closing database connection.

Now you should be able to use your database and be sure - it's thread safe.
