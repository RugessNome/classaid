package classaid.database;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import classaid.Database;

/**
 * Représente un Trimestre
 * @author  Vincent on 13/11/2016.
 */

public class Trimestre extends DatabaseEntity {

    public static String TableName = "Trimestre";
    public static String SelectClause = " Trimestre_id, Trimestre_dateDebut, Trimestre_dateFin ";

    private Calendar dateDebut;
    private Calendar dateFin;

    public Trimestre(Database d, Cursor c)
    {
        super(d, c);

        if(!c.isNull(1)) {
            this.dateDebut = new GregorianCalendar();
            this.dateDebut.setTimeInMillis(c.getLong(1));
        }
        if(!c.isNull(2)) {
            this.dateFin = new GregorianCalendar();
            this.dateFin.setTimeInMillis(c.getLong(2));
        }
    }

    public Calendar getDateDebut()
    {
        return this.dateDebut;
    }

    /**
     * Modifie la date de début du trimestre
     * @param d
     * @return true en cas de succès, false sinon
     */
    public boolean setDateDebut(Calendar d)
    {
        if(d.equals(this.dateDebut)) { return true; }

        Database db = this.getDatabase();
        ContentValues values = new ContentValues();
        values.put("Trimestre_dateDebut", d.getTimeInMillis());

        int rowsAffected = db.update(this.TableName, values, "Trimestre_id = " + this.id(), null);
        if(rowsAffected > 0)
        {
            this.dateDebut = d;
        }
        return rowsAffected == 1;
    }

    public Calendar getDateFin()
    {
        return this.dateFin;
    }

    /**
     * Modifie la date de fin du trimestre
     * @param d
     * @return true en cas de succès, false sinon
     */
    public boolean setDateFin(Calendar d)
    {
        if(d.equals(this.dateFin)) { return true; }

        Database db = this.getDatabase();
        ContentValues values = new ContentValues();
        values.put("Trimestre_dateFin", d.getTimeInMillis());

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
        java.util.Calendar now = java.util.Calendar.getInstance();
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
