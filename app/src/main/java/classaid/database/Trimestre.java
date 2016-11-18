package classaid.database;

import android.content.ContentValues;
import android.database.Cursor;

import java.sql.Date;
import java.util.List;

import classaid.Database;

/**
 * Représente un Trimestre
 * @author  Vincent on 13/11/2016.
 */

public class Trimestre extends DatabaseEntity {

    public static String TableName = "Trimestre";
    public static String SelectClause = " Trimestre_id, Trimestre_dateDebut, Trimestre_dateFin ";

    private Date dateDebut;
    private Date dateFin;

    public Trimestre(Database d, Cursor c)
    {
        super(d, c);

        this.dateDebut = new Date(c.getLong(1));
        this.dateFin = new Date(c.getLong(2));
    }

    public Date getDateDebut()
    {
        return this.dateDebut;
    }

    /**
     * Modifie la date de début du trimestre
     * @param d
     * @return true en cas de succès, false sinon
     */
    public boolean setDateDebut(Date d)
    {
        if(d.equals(this.dateDebut)) { return true; }

        Database db = this.getDatabase();
        ContentValues values = new ContentValues();
        values.put("Trimestre_dateDebut", d.getTime());

        int rowsAffected = db.update(this.TableName, values, "Trimestre_id = " + this.id(), null);
        if(rowsAffected > 0)
        {
            this.dateDebut = d;
        }
        return rowsAffected == 1;
    }

    public Date getDateFin()
    {
        return this.dateFin;
    }

    /**
     * Modifie la date de fin du trimestre
     * @param d
     * @return true en cas de succès, false sinon
     */
    public boolean setDateFin(Date d)
    {
        if(d.equals(this.dateFin)) { return true; }

        Database db = this.getDatabase();
        ContentValues values = new ContentValues();
        values.put("Trimestre_dateFin", d.getTime());

        int rowsAffected = db.update(this.TableName, values, "Trimestre_id = " + this.id(), null);
        if(rowsAffected > 0)
        {
            this.dateFin = d;
        }
        return rowsAffected == 1;
    }


    /**
     * Renvoie true si le trimestre est le trimestre courant, false sinon.
     * @return
     */
    public boolean isActive()
    {
        java.util.Date now = new java.util.Date();
        if(now.after(this.dateDebut) && now.before(this.dateFin)) { return true; }
        return false;
    }


    /**
     * Renvoie la liste des compétences du trimestre.
     * @return
     */
    public List<Competence> getContenus()
    {
        return this.getDatabase().getContenusTrimestre(this);
    }

}
