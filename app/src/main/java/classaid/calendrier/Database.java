package classaid.calendrier;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Classe permettant l'accès à la base de données des anecdotes
 * du jour.
 * Created by Vincent on 01/12/2016.
 */

public class Database {

    /**
     * Nom de la table contenant les anecdotes
     */
    public static final String TableName = "Anecdote";

    /**
     * Helper class permettant de créer la base de données
     */
    public static class DatabaseOpenHelper extends SQLiteOpenHelper {

        final static private int database_version = 1;

        public DatabaseOpenHelper(Context con, String dbName)
        {
            super(con, dbName, null, database_version);
        }

        /**
         * Exécute les requêtes SQL permettant de créer les tables de la base.
         * @param db
         */
        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(
                    "CREATE TABLE " + TableName + "(" +
                            "Anecdote_id INTEGER PRIMARY KEY,"+
                            "Anecdote_valeur TEXT " +
                            ");");

            ContentValues anecdote = new ContentValues();
            anecdote.put("Anecdote_valeur", "");
            for(int i = 0; i < 357; i++)
            {
                db.insert(TableName, null, anecdote);
            }

        }

        /**
         * Méthode permettant de mettre à jour la base de données. Ne fait rien.
         * @param db
         * @param oldVersion
         * @param newVersion
         */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            return;
        }
    }


    /**
     * La base de données SQLite
     */
    private SQLiteDatabase database;

    /**
     * Construit un objet de type Database permettant de récupérer les
     * anecdotes du jour à partir d'une base SQLite.
     * @param db
     */
    public Database(SQLiteDatabase db)
    {
        database = db;
    }

    /**
     * Ferme la base de données.
     */
    public void close()
    {
        database.close();
    }

    /**
     * Renvoie un objet de type Database permettant de lire et de modifier les anecdotes
     * du jour.
     * @param con
     * @return
     */
    public static Database getDatabase(Context con)
    {
        DatabaseOpenHelper helper = new DatabaseOpenHelper(con, "anecdotes_db");
        return new Database(helper.getWritableDatabase());
    }

    /**
     * Renvoie la valeur de l'anecdote numéro n.
     * @param n
     * @return
     */
    public String getAnecdote(int n)
    {
        if(n < 0 || n >= 367) { return null; }

        Cursor c = database.query(TableName, new String[]{"Anecdote_id", "Anecdote_valeur"}, "Anecdote_id = " + n, null, null, null, null, null);
        if(!c.moveToFirst()) {
            return null;
        }
        String ret = c.getString(1);
        c.close();
        return ret;
    }

    /**
     * Renvoie la valeur de l'anecdote associée à un jour donnée.
     * Cette fonction renvoie l'anecdote dont le numéro correspond
     * au numéro du jour dans l'année.
     * @param date
     * @return
     */
    public String getAnecdote(Calendar date)
    {
        int dayOfYear = date.get(Calendar.DAY_OF_YEAR);
        return getAnecdote(dayOfYear);
    }

    /**
     * Change la valeur de l'anecdote numéro n.
     * Renvoie true en cas de succès, false sinon.
     * @param n
     * @param val
     * @return
     */
    public boolean setAnecdote(int n, String val)
    {
        if(n < 0 || n >= 367) { return false; }

        ContentValues anec = new ContentValues();
        anec.put("Anecdote_valeur", val);
        return database.update(TableName, anec, "Anecdote_id = " + n, null) == 1;
    }

    /**
     * Modifie l'anecdote associé au jour donnée en entrée.
     * @param date
     * @param val
     * @return
     */
    public boolean setAnecdote(Calendar date, String val)
    {
        int dayOfYear = date.get(Calendar.DAY_OF_YEAR);
        return setAnecdote(dayOfYear, val);
    }


    public static void test(Context con)
    {
        Database db = getDatabase(con);
        db.setAnecdote(25, "Les flamants rose sont rose !");
        System.out.println("Anecdote 25 : " + db.getAnecdote(25));
        Calendar date = new GregorianCalendar(2016, 12, 1);
        db.setAnecdote(date, "C'est le jour de la Nuit Q 2016");
        System.out.println("Anecdote du 01/12/2016 : " + db.getAnecdote(date));
        db.close();
    }
}
