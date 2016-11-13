package classaid.database;

import java.sql.Date;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by Vincent on 12/11/2016.
 */

import classaid.Database;


public class Devoir extends DatabaseEntity {
    int typeNotation;
    Date date;
    String commentaire;

    public static String TableName = "Devoir";
    public static String SelectClause = " Devoir_id, Devoir_date, Devoir_commentaire, TypeNotation_id ";

    public Devoir(Database d, Cursor c)
    {
        super(d, c);

        typeNotation = c.getInt(3);
        date = new Date(c.getLong(1));
        commentaire = c.getString(2);
    }

    public int getTypeNotation()
    {
        return typeNotation;
    }

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

    public Date getDate()
    {
        return this.date;
    }

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

    public String getCommentaire()
    {
        return this.commentaire;
    }

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

    public Trimestre getTrimestre()
    {
        return this.getDatabase().getTrimestre(this.date);
    }

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

        int rowsAffected = this.getDatabase().update("Note", values, "Note_id = " + this.id(), null);
        return this.getDatabase().getNote(e, this);
    }

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
