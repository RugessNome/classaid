package classaid.database;

import android.database.Cursor;

import classaid.Database;

/**
 * Représente une entité de la base de données.
 * @author by Vincent on 12/11/2016.
 */

public class DatabaseEntity {
    /**
     * La base de données contenant l'entité.
     */
    public Database db;
    /**
     * L'identifiant de l'entité dans sa table.
     */
    private int id;

    /**
     * Construit une entité invalide.
     */
    public DatabaseEntity()
    {
        id = -1;
        db = null;
    }

    /**
     * Construit une entité à partir d'un tuple d'une base de donnée.
     * <p>
     * Attention, le premier élément du tuple doit être son id.
     * @param database la base de donnée
     * @param c le tuple
     */
    public DatabaseEntity(Database database, Cursor c) {
        db = database;
        id = c.getInt(0);
    }

    /**
     * Renvoie la base de données contenant l'entité.
     * @return
     */
    public Database getDatabase()
    {
        return db;
    }

    /**
     * Renvoie true si la base de données (et donc cette entité) est en lecture seule.
     * @return
     */
    public boolean isReadOnly()
    {
        return db.isReadOnly();
    }

    /**
     * Renvoie l'identifiant de l'entité.
     * @return
     */
    public int id()
    {
        return this.id;
    }

    /**
     * Renvoie true si l'entité est invalide (id() == -1); false sinon.
     * @return
     */
    public boolean isNull() { return db == null; }


    /**
     * Fonction de comparaison de deux entités.
     * @param other
     * @return
     */
    public boolean equals(DatabaseEntity other)
    {
        return other.id() == this.id && other.getClass().equals(this.getClass());
    }

}
