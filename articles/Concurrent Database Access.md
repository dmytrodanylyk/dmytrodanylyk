Concurrent Database Access
--------------------------

I wrote small article which describe how to make access to your android database thread safe.

----------

Assuming you have your own [SQLiteOpenHelper][1].

    public class DatabaseHelper extends SQLiteOpenHelper { ... }

Now you want to write data to database in separate threads.

```java
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

You will get following message in your logcat and one of your changes will not be written.

```java
android.database.sqlite.SQLiteDatabaseLockedException: database is locked (code 5)
```

This is happening because every time you create new `SQLiteOpenHelper` object you are actually making new database connection. If you try to write to the database from actual distinct connections at the same time, one will fail.

### To use database with multiple threads we need to make sure we are using one database connection.

Let’s make singleton class `DatabaseManager` which will hold and return single `SQLiteOpenHelper` object.

```java
public class DatabaseManager {

    private static DatabaseManager instance;
    private static SQLiteOpenHelper mDatabaseHelper;

    public static synchronized void initialize(Context context, SQLiteOpenHelper helper) {
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

    public SQLiteDatabase getDatabase() {
        return new mDatabaseHelper.getWritableDatabase();
    }

}
```

Updated code which write data to database in separate threads will look like this.

```java
 // In your application class
 DatabaseManager.initializeInstance(getApplicationContext());

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

```java
java.lang.IllegalStateException: attempt to re-open an already-closed object: SQLiteDatabase
```

Since we are using only one database connection, method `getDatabase()` return same instance of `SQLiteDatabase` object for `Thread1` and `Thread2`. What is happening, `Thread1` may close database, while `Thread2` is still using it. That’s why we have `IllegalStateException` crash.

We need to make sure no-one is using database and only then close it. Some folks on [stackoveflow][2] recommended to never close your `SQLiteDatabase`. It not only sounds stupid but also honor you with following logcat message.

```java
Leak found
Caused by: java.lang.IllegalStateException: SQLiteDatabase created and never closed
```

Correct way is to update our `DatabaseManager` method `getDatabase()`

```java
public SQLiteDatabase getDatabase() {
  SQLiteDatabase database = mDatabaseHelper.getWritableDatabase();
  database.acquireReference();
  return database;
}
```

As you can see we are now calling [acquireReference()][3] method which will increase database reference counter. Method [close()][4] of `SQLiteDatabase` automatically check reference counter and close database only when reference counter is zero.

----------

Now you should be able to use your database and be sure - it's thread safe.


  [1]: http://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper.html
  [2]: http://stackoverflow.com/
  [3]: http://developer.android.com/reference/android/database/sqlite/SQLiteClosable.html#acquireReference%28%29
  [4]: http://developer.android.com/reference/android/database/sqlite/SQLiteClosable.html#close%28%29