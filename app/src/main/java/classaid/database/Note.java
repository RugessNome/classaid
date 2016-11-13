package classaid.database;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by Vincent on 12/11/2016.
 */

import classaid.Database;

public class Note extends DatabaseEntity {
    private int eleve;
    private int devoir;
    private int typeNotation;
    private boolean absent;
    private float valeur;
    private String commentaire;


    public static String TableName = "Note";
    public static String SelectClause = " Note_id, Note_absent, Note_valeur, Note_commentaire, Eleve_id, Note.Devoir_id, Devoir.TypeNotation_id ";

    public Note(Database d, Cursor c)
    {
        super(d, c);

        eleve = c.getInt(4);
        devoir = c.getInt(5);
        absent = (c.getInt(1) == 1);
        valeur = c.getFloat(2);
        commentaire = c.getString(3);
    }

    public static Note getNote(Eleve e, Devoir d)
    {
        return e.getDatabase().getNote(e, d);
    }

    public Eleve getEleve()
    {
        return this.getDatabase().getEleve(this.eleve);
    }

    public Devoir getDevoir()
    {
        return this.getDatabase().getDevoir(this.devoir);
    }

    public boolean getAbsent()
    {
        return this.absent;
    }

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

    public float getValeur()
    {
        return this.valeur;
    }

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

    public String getCommentaire()
    {
        return this.commentaire;
    }

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

    public int getTypeNotation()
    {
        return this.typeNotation;
    }

    public boolean setTypeNotation(int t)
    {
        if(t == this.typeNotation) { return true; }

        Database db = this.getDatabase();
        ContentValues values = new ContentValues();
        values.put("Note_commentaire", t);

        int rowsAffected = db.update(this.TableName, values, "Note_id = " + this.id(), null);
        if(rowsAffected > 0)
        {
            this.typeNotation = t;
        }
        return rowsAffected == 1;
    }


}
