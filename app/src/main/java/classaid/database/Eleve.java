package classaid.database;

import java.sql.Date;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by Vincent on 12/11/2016.
 */

import classaid.Database;

/**
 * Représente un élève.
 * @author Vincent
 */
public class Eleve extends DatabaseEntity {

    /**
     * L'id de la personne
     */
    private int personneId;
    /**
     * Le nom
     */
    private String nom;
    /**
     * Le prenom
     */
    private String prenom;
    /**
     * la date de naissance
     */
    private Date dateNaissance;
    /**
     * 0 pour masculin, 1 pour féminin
     */
    private int sexe;
    private List<DonneeSupplementaire> donneesSupplementaires;

    public static String SelectClause = " Eleve_id, Eleve.Personne_id, Personne_nom, Personne_prenom, Personne_dateNaissance, Personne_sexe ";

    /**
     * Construit une entité Eleve à partir d'un tuple d'une base de données
     * @param d la base
     * @param c le tuple
     */
    public Eleve(Database d, Cursor c)
    {
        super(d, c);

        personneId = c.getInt(1);
        nom = c.getString(2);
        prenom = c.getString(3);
        dateNaissance = new Date(c.getInt(4));
        sexe = c.getInt(5);


    }

    /**
     * Renvoie le nom de l'élève
     * @return
     */
    public String getNom()
    {
        return this.nom;
    }

    /**
     * Modifie le nom de l'élève
     * @param n
     * @return true en cas de succès, false sinon
     */
    public boolean setNom(String n)
    {
        if(nom.equals(n)) { return true; }

        Database db = this.getDatabase();
        ContentValues values = new ContentValues();
        values.put("Personne_nom", n);

        int rowsAffected = db.update("Personne", values, "Personne_id = " + personneId, null);
        if(rowsAffected > 0)
        {
            this.nom = n;
        }
        return rowsAffected == 1;
    }

    /**
     * Renvoie le prénom de l'élève
     * @return
     */
    public String getPrenom()
    {
        return this.prenom;
    }

    /**
     * Modifie le prénom de l'élève
     * @param p
     * @return true en cas de succès, false sinon
     */
    public boolean setPrenom(String p)
    {
        if(prenom.equals(p)) { return true; }

        Database db = this.getDatabase();
        ContentValues values = new ContentValues();
        values.put("Personne_prenom", p);

        int rowsAffected = db.update("Personne", values, "Personne_id = " + personneId, null);
        if(rowsAffected > 0)
        {
            this.prenom = p;
        }
        return rowsAffected == 1;
    }

    /**
     * Renvoie le sexe de l'élève
     * @return 0 pour masculin, 1 pour feminin
     */
    public int getSexe()
    {
        return this.sexe;
    }

    public boolean setSexe(int s)
    {
        if(s == this.sexe) { return true; }

        Database db = this.getDatabase();
        ContentValues values = new ContentValues();
        values.put("Personne_sexe", s);

        int rowsAffected = db.update("Personne", values, "Personne_id = " + personneId, null);
        if(rowsAffected > 0)
        {
            this.sexe = s;
        }
        return rowsAffected == 1;
    }

    /**
     * Renvoie la date de naissance de l'élève
     * @return
     */
    public Date getDateNaissance()
    {
        return this.dateNaissance;
    }

    /**
     * Modifie la date de naissance de l'élève
     * @param d
     * @return true en cas de succès, false sinon
     */
    public boolean setDateNaissance(Date d)
    {
        if(d.equals(this.dateNaissance)) { return true; }

        Database db = this.getDatabase();
        ContentValues values = new ContentValues();
        values.put("Personne_dateNaissance", d.getTime());

        int rowsAffected = db.update("Personne", values, "Personne_id = " + personneId, null);
        if(rowsAffected > 0)
        {
            this.dateNaissance = d;
        }
        return rowsAffected == 1;
    }

    /**
     * Renvoie l'age de l'élève
     * @return
     */
    public int getAge()
    {
        if(dateNaissance == null) { return -1; }

        java.util.Date now = new java.util.Date();
        return (int) ((now.getTime() - dateNaissance.getTime()) / 3.15576e+10);
    }

    /**
     * Renvoie la liste des notes de l'élève
     * @return
     */
    public List<Note> getNotes()
    {
        return this.getDatabase().getNotes(this);
    }

    /**
     * Renvoie la liste des données supplémentaires associées à l'élève
     * @return
     */
    public List<DonneeSupplementaire> getDonneesSupplementaires()
    {
        if(donneesSupplementaires != null)
        {
            return donneesSupplementaires;
        }

        this.donneesSupplementaires = this.getDatabase().getDonneesSupplementaires(this);
        return this.donneesSupplementaires;
    }

    /**
     * Ajoute une donnée supplémentaire à cet élève.
     * @param nom
     * @param valeur
     * @return
     */
    public DonneeSupplementaire addDonneeSupplementaire(String nom, String valeur)
    {
        DonneeSupplementaire ret = this.getDatabase().addDonneeSupplementaire(this, nom, valeur);
        if(ret != null) { this.donneesSupplementaires = null; }
        return ret;
    }

    /**
     * Récupere l'appréciation de l'élève pour une compétence donnée
     * @param c la compétence
     * @return l'appréciation en cas de succès, null sinon
     */
    public Appreciation getAppreciation(Competence c)
    {
        return this.getDatabase().getAppreciation(this, c);
    }

}
