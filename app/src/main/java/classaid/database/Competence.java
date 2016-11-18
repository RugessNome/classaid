package classaid.database;

import java.sql.Date;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by Vincent on 13/11/2016.
 */

import classaid.Database;

/**
 * Représente une compétence.
 * @author Vincent
 */
public class Competence extends DatabaseEntity {

    /**
     * Le nom de la table associée à l'entité.
     */
    public static String TableName = "Competence";
    /**
     * La valeur de la clause SELECT permettant de créer une entité de ce type.
     */
    public static String SelectClause = " Competence_id, Competence_nom, Competence_parent ";

    /**
     * Le nom de la compétence
     */
    private String nom;
    /**
     * L'id de la compétence parente (peut valoir null).
     */
    private Long parent;

    /**
     * Construit une compétence à partir d'un tuple d'une base de données.
     * @param d La base de données
     * @param c Un curseur dont la valeur acutel est le tuple représentant l'entité
     */
    public Competence(Database d, Cursor c)
    {
        super(d, c);

        this.nom = c.getString(1);
        this.parent = c.isNull(2) ? null : c.getLong(2);
    }

    /**
     * Renvoie le nom de la compétence
     * @return
     */
    public String getNom()
    {
        return this.nom;
    }

    /**
     * Modifie le nom de la compétence.
     * @param n
     * @return true en cas de succès, false sinon
     */
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

    /**
     * Renvoie la compétence mère de cette compétence.
     * @return null si la compétence n'a pas de mère
     */
    public Competence getParent()
    {
        if(this.parent == null) { return null; }

        return this.getDatabase().getCompetence(this.parent.intValue());
    }

    /**
     * Renvoie la liste des sous-compétences de cette compétence.
     * @return
     */
    public List<Competence> getSousCompetences()
    {
        return this.getDatabase().getSousCompetences(this);
    }

    /**
     * Ajoute une sous-compétence à cette compétence.
     * @param nom le nom de la sous-compétence
     * @return l'entité de type Compétence en cas de succès, null sinon
     */
    public Competence addCompetence(String nom)
    {
        return this.getDatabase().addCompetence(nom, this);
    }

    /**
     * Ajoute un devoir à cette compétence.
     * <p>
     * Si creationNoteAuto vaut vrai, une entité Note est également créée pour chaque élève
     * (la valeur de la note set mise à null).
     * @param d la date du devoir
     * @param typeNotation le type de notation du devoir
     * @param creationNoteAuto contrôle la création d'entité Note
     * @return le devoir ajouté en cas de succès, null sinon
     */
    public Devoir addDevoir(Date d, int typeNotation, boolean creationNoteAuto)
    {
        return this.getDatabase().addDevoir(this, d, typeNotation, creationNoteAuto);
    }

    /**
     * Renvoie le niveau de profondeur de cette compétence (0 pour une compétence, 1 pour une
     * sous-compétence, 2 pour une sous-sous-compétences, etc...).
     * @return
     */
    public int depth()
    {
        if(this.parent == null)
        {
            return 0;
        }
        return 1 + this.getParent().depth();
    }

    /**
     * Renvoie la liste des notes pour cette compétence pour un trimestre et un élève données.
     * @param e l'élève
     * @param trimestre le numéro du trimestre
     * @return
     */
    public List<Note> getNotes(Eleve e, int trimestre)
    {
        return this.getDatabase().getNotes(e, this, trimestre);
    }

    /**
     * Renvoie l'appréciation associée à un élève pour cette compétence.
     * @param e l'élève
     * @return null en cas d'échec ou s'il n'y a pas d'appréciation associée.
     */
    public Appreciation getAppreciation(Eleve e)
    {
        return this.getDatabase().getAppreciation(e, this);
    }

    /**
     * Modifie la valeur de l'appréciation d'un élève pour cette compétence.
     * <p>
     * Si besoin, l'entité Appreciation associée est créée.
     * @param e l'élève
     * @param val la valeur de l'appréciation
     * @return l'appréciation modifié en cas de succès, null sinon
     */
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
