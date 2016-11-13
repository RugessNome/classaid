package classaid.database;

/**
 * Created by Vincent on 12/11/2016.
 */

import android.database.Cursor;

import classaid.Database;

public class DatabaseEntity {
    public Database db;
    private int id;

    public DatabaseEntity()
    {
        id = -1;
        db = null;
    }

    public DatabaseEntity(Database database, Cursor c)
    {
        db = database;
        id = c.getInt(0);
    }

    public DatabaseEntity(Database database, int id_)
    {
        db = database;
        id = id_;
    }

    public Database getDatabase()
    {
        return db;
    }

    public boolean isReadOnly()
    {
        return db.isReadOnly();
    }

    public int id()
    {
        return this.id;
    }
    public boolean isNull() { return db == null; }

    public boolean sync()
    {
        return false;
    }

    public boolean equals(DatabaseEntity other)
    {
        return other.id() == this.id && other.getClass().equals(this.getClass());
    }

}
