package classaid.database;

import java.sql.Date;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by Vincent on 12/11/2016.
 */

import classaid.Database;

public class Eleve extends DatabaseEntity {

    private int personneId;
    private String nom;
    private String prenom;
    private Date dateNaissance;
    private int sexe;
    private List<DonneeSupplementaire> donneesSupplementaires;

    public static String SelectClause = " Eleve_id, Eleve.Personne_id, Personne_nom, Personne_prenom, Personne_dateNaissance, Personne_sexe ";

    public Eleve(Database d, Cursor c)
    {
        super(d, c);

        personneId = c.getInt(1);
        nom = c.getString(2);
        prenom = c.getString(3);
        dateNaissance = new Date(c.getInt(4));
        sexe = c.getInt(5);


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
        values.put("Personne_nom", n);

        int rowsAffected = db.update("Personne", values, "Personne_id = " + personneId, null);
        if(rowsAffected > 0)
        {
            this.nom = n;
        }
        return rowsAffected == 1;
    }

    public String getPrenom()
    {
        return this.prenom;
    }

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

    public Date getDateNaissance()
    {
        return this.dateNaissance;
    }

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

    public int getAge()
    {
        if(dateNaissance == null) { return -1; }

        java.util.Date now = new java.util.Date();
        return (int) ((now.getTime() - dateNaissance.getTime()) / 3.15576e+10);
    }

    public List<Note> getNotes()
    {
        return this.getDatabase().getNotes(this);
    }

    public List<DonneeSupplementaire> getDonneesSupplementaires()
    {
        if(donneesSupplementaires != null)
        {
            return donneesSupplementaires;
        }

        this.donneesSupplementaires = this.getDatabase().getDonneesSupplementaires(this);
        return this.donneesSupplementaires;
    }

    public DonneeSupplementaire addDonneeSupplementaire(String nom, String valeur)
    {
        DonneeSupplementaire ret = this.getDatabase().addDonneeSupplementaire(this, nom, valeur);
        if(ret != null) { this.donneesSupplementaires = null; }
        return ret;
    }

    public Appreciation getAppreciation(Competence c)
    {
        return this.getDatabase().getAppreciation(this, c);
    }

}
