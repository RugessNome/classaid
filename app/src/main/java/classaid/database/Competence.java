package classaid.database;

import java.sql.Date;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by Vincent on 13/11/2016.
 */

import classaid.Database;

public class Competence extends DatabaseEntity {

    public static String TableName = "Competence";
    public static String SelectClause = " Competence_id, Competence_nom, Competence_parent ";

    private String nom;
    private Long parent;

    public Competence(Database d, Cursor c)
    {
        super(d, c);

        this.nom = c.getString(1);
        this.parent = c.isNull(2) ? null : c.getLong(2);
    }

    public String getNom()
    {
        return this.nom;
    }

    public boolean setNom(String n)
    {
        if(nom.equals(n)) { return true; }

        Database db = this.getDatabase();
        ContentValues values = new ContentValues();
        values.put("Competence_nom", n);

        int rowsAffected = db.update(this.TableName, values, "Competence_id = " + this.id(), null);
        if(rowsAffected > 0)
        {
            this.nom = n;
        }
        return rowsAffected == 1;
    }

    public Competence getParent()
    {
        if(this.parent == null) { return null; }

        return this.getDatabase().getCompetence(this.parent.intValue());
    }

    public List<Competence> getSousCompetences()
    {
        return this.getDatabase().getSousCompetences(this);
    }

    public Competence addCompetence(String nom)
    {
        return this.getDatabase().addCompetence(nom, this);
    }

    public Devoir addDevoir(Date d, int typeNotation, boolean creationNoteAuto)
    {
        return this.getDatabase().addDevoir(this, d, typeNotation, creationNoteAuto);
    }

    public int depth()
    {
        if(this.parent == null)
        {
            return 0;
        }
        return 1 + this.getParent().depth();
    }

    public List<Note> getNotes(Eleve e, int trimestre)
    {
        return this.getDatabase().getNotes(e, this, trimestre);
    }

    public Appreciation getAppreciation(Eleve e)
    {
        return this.getDatabase().getAppreciation(e, this);
    }

    public Appreciation setAppreciation(Eleve e, String val)
    {
        Appreciation a = getAppreciation(e);
        if(a == null)
        {
            return this.getDatabase().addAppreciation(e, this, val);
        }

        a.setCommentaire(val);
        return a;
    }
}
