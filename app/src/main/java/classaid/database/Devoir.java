package classaid.database;

import java.sql.Date;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by Vincent on 12/11/2016.
 */

import classaid.Database;

/**
 * Réprésente un devoir.
 * @author Vincent
 */

public class Devoir extends DatabaseEntity {
    /**
     * Le type de notation du devoir
     */
    int typeNotation;
    /**
     * La date du devoir
     */
    Date date;
    /**
     * La valeur du commentaire général associé au devoir.
     */
    String commentaire;

    /**
     * Le nom de la table associée à l'entité.
     */
    public static String TableName = "Devoir";
    /**
     * La valeur de la clause SELECT permettant de créer une entité de ce type.
     */
    public static String SelectClause = " Devoir_id, Devoir_date, Devoir_commentaire, Devoir.TypeNotation_id ";

    /**
     * Construit une entité de type Devoir à partir d'un tuple de la base.
     * @param d La base de données
     * @param c le tuple
     */
    public Devoir(Database d, Cursor c)
    {
        super(d, c);

        typeNotation = c.getInt(3);
        date = new Date(c.getLong(1));
        commentaire = c.getString(2);
    }

    /**
     * Renvoie le type de notation du devoir.
     * @return
     */
    public int getTypeNotation()
    {
        return typeNotation;
    }

    /**
     * Modifie le type de notation du devoir.
     * @param t le type de notation
     * @return true si la modification a réussi, false sinon.
     */
    public boolean setTypeNotation(int t)
    {
        if(typeNotation == t) return true;

        Database db = this.getDatabase();
        ContentValues values = new ContentValues();
        values.put("TypeNotation_id", t);

        int rowsAffected = db.update(this.TableName, values, "Devoir_id = " + this.id(), null);
        if(rowsAffected > 0)
        {
            this.typeNotation = t;
        }
        return rowsAffected == 1;
    }

    /**
     * Renvoie la date du devoir.
     * @return
     */
    public Date getDate()
    {
        return this.date;
    }

    /**
     * Modifie la date du devoir
     * @param d la nouvelle date
     * @return true en cas de succès, false sinon.
     */
    public boolean setDate(Date d)
    {
        if(d.equals(this.date)) { return true; }

        Database db = this.getDatabase();
        ContentValues values = new ContentValues();
        values.put("Devoir_date", d.getTime());

        int rowsAffected = db.update(this.TableName, values, "Devoir_id = " + this.id(), null);
        if(rowsAffected > 0)
        {
            this.date = d;
        }
        return rowsAffected == 1;
    }

    /**
     * Renvoie le commentaire associé au devoir.
     * @return
     */
    public String getCommentaire()
    {
        return this.commentaire;
    }

    /**
     * Modifie le commentaire associé au devoir.
     * @param c
     * @return true en cas de succès, false sinon
     */
    public boolean setCommentaire(String c)
    {
        if(c.equals(this.commentaire)) { return true; }

        Database db = this.getDatabase();
        ContentValues values = new ContentValues();
        values.put("Devoir_commentaire", c);

        int rowsAffected = db.update(this.TableName, values, "Devoir_id = " + this.id(), null);
        if(rowsAffected > 0)
        {
            this.commentaire = c;
        }
        return rowsAffected == 1;
    }

    /**
     * Renvoie l'entité Trimestre associé au devoir.
     * @return
     */
    public Trimestre getTrimestre()
    {
        return this.getDatabase().getTrimestre(this.date);
    }

    /**
     * Modifie la note d'un élève pour ce devoir
     * @param e l'élève
     * @param n la valeur de la note
     * @param commentaire le commentaire associé à la note
     * @return la Note en cas de succès, null en cas d'échec
     */
    public Note setNote(Eleve e, float n, String commentaire)
    {
        Note note = this.getDatabase().getNote(e, this);
        if(note == null)
        {
            return this.getDatabase().addNote(e, this, n, commentaire);
        }

        ContentValues values = new ContentValues();
        values.put("Note_absent", false);
        values.put("Note_valeur", n);
        if(commentaire == null) {
        values.putNull("Note_commentaire");
        } else {
            values.put("Note_commentaire", commentaire);
        }

        int rowsAffected = this.getDatabase().update("Note", values, "Eleve_id = " + e.id() + " AND Devoir_id = " + this.id(), null);
        return this.getDatabase().getNote(e, this);
    }

    /**
     * Déclare un élève absent pour ce devoir et modifie l'entité Note associée en conséquence.
     * @param e
     * @return la Note associé en cas de succès, null sinon
     */
    public Note setAbsent(Eleve e)
    {
        Note note = this.getDatabase().getNote(e, this);
        if(note == null)
        {
            return this.getDatabase().addNote(e, this, null, "");
        }


        ContentValues values = new ContentValues();
        values.put("Note_absent", true);
        values.putNull("Note_valeur");

        int rowsAffected = this.getDatabase().update("Note", values, "Note_id = " + this.id(), null);
        return this.getDatabase().getNote(e, this);
    }
}
