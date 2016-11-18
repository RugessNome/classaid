package classaid.database;

import android.content.ContentValues;
import android.database.Cursor;

import classaid.Database;

/**
 * Réprésente une appréciation.
 * @author Vincent on 13/11/2016.
 */

public class Appreciation extends DatabaseEntity {

    /**
     * Le nom de la table associée à cette entité.
     */
    public static String TableName = "Appreciation";
    /**
     * La valeur de la clause SELECT permettant de créer une entité de type Appréciation
     */
    public static String SelectClause = " Appreciation_id, Appreciation_commentaire, Appreciation.Competence_id, Appreciation.Eleve_id ";

    /**
     * L'identifiant de l'élève associé à l'appréciation
     */
    private int eleve;
    /**
     * L'identifiant de la compétence associée à l'appréciation
     */
    private int competence;
    /**
     * Le commentaire de l'appréciation.
     */
    private String commentaire;

    /**
     * Construit une appréciation à partir d'un tuple d'une base de données.
     * @param d La base de données
     * @param c Un curseur dont la valeur acutel est le tuple représentant l'entité
     */
    public Appreciation(Database d, Cursor c)
    {
        super(d, c);

        this.commentaire = c.getString(1);
        this.competence = c.getInt(2);
        this.eleve = c.getInt(3);
    }

    /**
     * Renvoie l'élève associé à l'appréciation
     * @return
     */
    public Eleve getEleve()
    {
        return this.getDatabase().getEleve(this.eleve);
    }

    /**
     * Renvoie la compétence associée à l'appréciation
     * @return
     */
    public Competence getCompetence()
    {
        return this.getDatabase().getCompetence(this.competence);
    }

    /**
     * Renvoie la valeur de l'appréciation
     * @return
     */
    public String getCommentaire()
    {
        return this.commentaire;
    }

    /**
     * Modifie la valeur de l'appréciation.
     * @param c la nouvelle valeur du commentaire
     * @return true en cas de succès, false en cas d'échec
     */
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
