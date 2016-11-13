package classaid.database;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by Vincent on 13/11/2016.
 */

import classaid.Database;


public class DonneeSupplementaire extends DatabaseEntity {

    public static String TableName = "DonneeSupplementaire";
    public static String SelectClause = " DonneeSupplementaire_id, DonneeSupplementaire_nom, DonneeSupplementaire_valeur, Eleve_id ";

    private String nom;
    private String valeur;
    private int eleve;

    public DonneeSupplementaire(Database d, Cursor c)
    {
        super(d, c);
        nom = c.getString(1);
        valeur = c.getString(2);
        eleve = c.getInt(3);
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
        values.put("DonneeSupplementaire_nom", n);

        int rowsAffected = db.update(this.TableName, values, "DonneeSupplementaire_id = " + this.id(), null);
        if(rowsAffected > 0)
        {
            this.nom = n;
        }
        return rowsAffected == 1;
    }

    public String getValeur()
    {
        return this.valeur;
    }

    public boolean setValeur(String v)
    {
        if(valeur.equals(v)) { return true; }

        Database db = this.getDatabase();
        ContentValues values = new ContentValues();
        values.put("DonneeSupplementaire_valeur", v);

        int rowsAffected = db.update(this.TableName, values, "DonneeSupplementaire_id = " + this.id(), null);
        if(rowsAffected > 0)
        {
            this.valeur = v;
        }
        return rowsAffected == 1;
    }

    public Eleve getEleve()
    {
        return this.getDatabase().getEleve(this.eleve);
    }

    public boolean delete()
    {
        return this.getDatabase().delete(DonneeSupplementaire.TableName, "DonneeSupplementaire_id = " + this.id(), null) == 1;
    }

}
