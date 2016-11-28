package classaid.database;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by Vincent on 12/11/2016.
 */

import classaid.Database;

/**
 * Représente une Note
 * @author Vincent
 */
public class Note extends DatabaseEntity {
    /**
     * L'élève
     */
    private int eleve;
    /**
     * Le devoir
     */
    private int devoir;
    /**
     * Le type de notation
     */
    private int typeNotation;
    /**
     * L'élève était-il absent ?
     */
    private boolean absent;
    /**
     * Valeur de la note
     */
    private float valeur;
    /**
     * Commentaire du correcteur
     */
    private String commentaire;


    public static String TableName = "Note";
    public static String SelectClause = " Note_id, Note_absent, Note_valeur, Note_commentaire, Note.Eleve_id, Note.Devoir_id, Devoir.TypeNotation_id ";

    /**
     * Construit une entité Note à partir d'un tuple d'une base de données
     * @param d la base
     * @param c le tuple
     */
    public Note(Database d, Cursor c)
    {
        super(d, c);

        eleve = c.getInt(4);
        devoir = c.getInt(5);
        absent = (c.getInt(1) == 1);
        valeur = c.getFloat(2);
        commentaire = c.getString(3);
    }

    /**
     * Récupère la note d'un élève à un devoir.
     * @param e l'élève
     * @param d le devoir
     * @return null en cas d'échec
     */
    public static Note getNote(Eleve e, Devoir d)
    {
        return e.getDatabase().getNote(e, d);
    }

    /**
     * Renvoie l'élève
     * @return
     */
    public Eleve getEleve()
    {
        return this.getDatabase().getEleve(this.eleve);
    }

    /**
     * Renvoie le devoir auquel s'applique la note
     * @return
     */
    public Devoir getDevoir()
    {
        return this.getDatabase().getDevoir(this.devoir);
    }

    /**
     * Renvoie true si l'élève était absent pour le devoir
     * @return
     */
    public boolean getAbsent()
    {
        return this.absent;
    }

    /**
     * Modifie la valeur de l'attribut absence de l'entité.
     * @param a
     * @return true en cas de succès, false en cas d'échec
     */
    public boolean setAbsent(boolean a)
    {
        if(this.absent == a) { return true; }

        Database db = this.getDatabase();
        ContentValues values = new ContentValues();
        values.put("Note_absent", a ? 1 : 0);

        int rowsAffected = db.update(this.TableName, values, "Note_id = " + this.id(), null);
        if(rowsAffected > 0)
        {
            this.absent = a;
        }
        return rowsAffected == 1;
    }

    /**
     * Renvoie la valeur de la note.
     * <p>
     * Si l'élève était absent pour le devoir, la valeur renvoyé par cette fonction n'est pas
     * définie.
     * @return
     */
    public float getValeur()
    {
        return this.valeur;
    }

    /**
     * Change la valeur de la note.
     * @param v
     * @return true en cas de succès, false sinon
     */
    public boolean setValeur(float v)
    {
        if(this.valeur == v) { return true; }

        Database db = this.getDatabase();
        ContentValues values = new ContentValues();
        values.put("Note_valeur", v);

        int rowsAffected = db.update(this.TableName, values, "Note_id = " + this.id(), null);
        if(rowsAffected > 0)
        {
            this.valeur = v;
        }
        return rowsAffected == 1;
    }

    /**
     * Renvoie la valeur de la note sur une échelle de 100.
     * @return
     */
    public float scaledValue()
    {
        if(this.typeNotation == TypeNotation.NotationSur10)
        {
            return this.valeur * 10;
        }
        else if(this.typeNotation == TypeNotation.NotationSur20)
        {
            return this.valeur * 5;
        }

        return -1;
    }

    /**
     * Renvoie le commentaire associé à la note
     * @return
     */
    public String getCommentaire()
    {
        return this.commentaire;
    }

    /**
     * Modifie le commentaire associé à la note
     * @param c
     * @return true en cas de succès, false sinon
     */
    public boolean setCommentaire(String c)
    {
        if(c.equals(this.commentaire)) { return true; }

        Database db = this.getDatabase();
        ContentValues values = new ContentValues();
        values.put("Note_commentaire", c);

        int rowsAffected = db.update(this.TableName, values, "Note_id = " + this.id(), null);
        if(rowsAffected > 0)
        {
            this.commentaire = c;
        }
        return rowsAffected == 1;
    }

    /**
     * Renvoie le type de notation du devoir.
     * @return
     */
    public int getTypeNotation()
    {
        return this.typeNotation;
    }

    /**
     * Supprime la note de la base de données.
     */
    public void delete()
    {
        this.getDatabase().delete(Note.TableName, "Note_id = " + this.id(), null);
    }

}
