package classaid.database;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by Vincent on 13/11/2016.
 */

import classaid.Database;

/**
 * Réprésente une donnée supplémentaire.
 */
public class DonneeSupplementaire extends DatabaseEntity {

    public static String TableName = "DonneeSupplementaire";
    public static String SelectClause = " DonneeSupplementaire_id, DonneeSupplementaire_nom, DonneeSupplementaire_valeur, DonneeSupplementaire.Eleve_id ";

    /**
     * Le nom de la donnée supplémentaire
     */
    private String nom;
    /**
     * La valeur de la donnée
     */
    private String valeur;
    /**
     * L'élève associé à la donnée.
     */
    private int eleve;

    /**
     * Construit une entité DonneeSupplementaire à partir d'un tuple d'une base de données
     * @param d la base de données
     * @param c le tuple
     */
    public DonneeSupplementaire(Database d, Cursor c)
    {
        super(d, c);
        nom = c.getString(1);
        valeur = c.getString(2);
        eleve = c.getInt(3);
    }

    /**
     * Renvoie le nom de la donnée supplémentaire
     * @return
     */
    public String getNom()
    {
        return this.nom;
    }

    /**
     * Modifie le nom de la donnée supplémentaire
     * @param n
     * @return true en cas de succès, false sinon
     */
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

    /**
     * Renvoie la valeur de la donnée supplementaire
     * @return
     */
    public String getValeur()
    {
        return this.valeur;
    }

    /**
     * Modifie la valeur de la donnée supplémentaire
     * @param v
     * @return true en cas de succès, false sinon
     */
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

    /**
     * Renvoie l'élève associé à cette entité.
     * @return
     */
    public Eleve getEleve()
    {
        return this.getDatabase().getEleve(this.eleve);
    }

    /**
     * Supprime la donnée supplémentaire de la base.
     * <p>
     * Attention, vous ne devez plus utiliser cette entité après cette opération.
     * @return true en cas de succès, false sinon
     */
    public boolean delete()
    {
        return this.getDatabase().delete(DonneeSupplementaire.TableName, "DonneeSupplementaire_id = " + this.id(), null) == 1;
    }

}
