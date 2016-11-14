package classaid.database;

import android.content.ContentValues;
import android.database.Cursor;

import classaid.Database;

/**
 * Created by Vincent on 13/11/2016.
 */

public class Appreciation extends DatabaseEntity {

    public static String TableName = "Appreciation";
    public static String SelectClause = " Appreciation_id, Appreciation_commentaire, Appreciation.Competence_id, Appreciation.Eleve_id ";

    private int eleve;
    private int competence;
    private String commentaire;

    public Appreciation(Database d, Cursor c)
    {
        super(d, c);

        this.commentaire = c.getString(1);
        this.competence = c.getInt(2);
        this.eleve = c.getInt(3);
    }

    public Eleve getEleve()
    {
        return this.getDatabase().getEleve(this.eleve);
    }

    public Competence getCompetence()
    {
        return this.getDatabase().getCompetence(this.competence);
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
        values.put("Appreciation_commentaire", c);

        int rowsAffected = db.update(this.TableName, values, "Appreciation_id = " + this.id(), null);
        if(rowsAffected > 0)
        {
            this.commentaire = c;
        }
        return rowsAffected == 1;
    }

}
