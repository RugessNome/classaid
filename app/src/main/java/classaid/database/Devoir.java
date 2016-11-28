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
 * Réprésente un devoir.
 * @author Vincent
 */

public class Devoir extends DatabaseEntity {
    /**
     * Le type de notation du devoir
     */
    int typeNotation;
    /**
     * La date du devoir
     */
    Date date;
    /**
     * La valeur du commentaire général associé au devoir.
     */
    String commentaire;

    /**
     * Le nom de la table associée à l'entité.
     */
    public static String TableName = "Devoir";
    /**
     * La valeur de la clause SELECT permettant de créer une entité de ce type.
     */
    public static String SelectClause = " Devoir_id, Devoir_date, Devoir_commentaire, Devoir.TypeNotation_id ";

    /**
     * Construit une entité de type Devoir à partir d'un tuple de la base.
     * @param d La base de données
     * @param c le tuple
     */
    public Devoir(Database d, Cursor c)
    {
        super(d, c);

        typeNotation = c.getInt(3);
        date = new Date(c.getLong(1));
        commentaire = c.getString(2);
        if(commentaire == null) commentaire = "";
    }

    /**
     * Renvoie le type de notation du devoir.
     * @return
     */
    public int getTypeNotation()
    {
        return typeNotation;
    }

    /**
     * Modifie le type de notation du devoir.
     * @param t le type de notation
     * @return true si la modification a réussi, false sinon.
     */
    public boolean setTypeNotation(int t)
    {
        if(typeNotation == t) return true;

        Database db = this.getDatabase();
        ContentValues values = new ContentValues();
        values.put("TypeNotation_id", t);

        int rowsAffected = db.update(this.TableName, values, "Devoir_id = " + this.id(), null);
        if(rowsAffected > 0)
        {
            this.typeNotation = t;
        }
        return rowsAffected == 1;
    }

    /**
     * Renvoie la date du devoir.
     * @return
     */
    public Date getDate()
    {
        return this.date;
    }

    /**
     * Modifie la date du devoir
     * @param d la nouvelle date
     * @return true en cas de succès, false sinon.
     */
    public boolean setDate(Date d)
    {
        if(d.equals(this.date)) { return true; }

        Database db = this.getDatabase();
        ContentValues values = new ContentValues();
        values.put("Devoir_date", d.getTime());

        int rowsAffected = db.update(this.TableName, values, "Devoir_id = " + this.id(), null);
        if(rowsAffected > 0)
        {
            this.date = d;
        }
        return rowsAffected == 1;
    }

    /**
     * Renvoie le commentaire associé au devoir.
     * @return
     */
    public String getCommentaire()
    {
        return this.commentaire;
    }

    /**
     * Modifie le commentaire associé au devoir.
     * @param c
     * @return true en cas de succès, false sinon
     */
    public boolean setCommentaire(String c)
    {
        if(c.equals(this.commentaire)) { return true; }

        Database db = this.getDatabase();
        ContentValues values = new ContentValues();
        values.put("Devoir_commentaire", c);

        int rowsAffected = db.update(this.TableName, values, "Devoir_id = " + this.id(), null);
        if(rowsAffected > 0)
        {
            this.commentaire = c;
        }
        return rowsAffected == 1;
    }

    /**
     * Renvoie l'entité Trimestre associé au devoir.
     * @return
     */
    public Trimestre getTrimestre()
    {
        return this.getDatabase().getTrimestre(this.date);
    }

    /**
     * Renvoie la liste des notes pour ce devoir.
     * @return
     */
    public List<Note> getNotes()
    {
        return this.getDatabase().getNotes(this);
    }

    /**
     * Modifie la note d'un élève pour ce devoir
     * @param e l'élève
     * @param n la valeur de la note
     * @param commentaire le commentaire associé à la note
     * @return la Note en cas de succès, null en cas d'échec
     */
    public Note setNote(Eleve e, float n, String commentaire)
    {
        Note note = this.getDatabase().getNote(e, this);
        if(note == null)
        {
            return this.getDatabase().addNote(e, this, n, commentaire);
        }

        ContentValues values = new ContentValues();
        values.put("Note_absent", false);
        values.put("Note_valeur", n);
        if(commentaire == null) {
        values.putNull("Note_commentaire");
        } else {
            values.put("Note_commentaire", commentaire);
        }

        int rowsAffected = this.getDatabase().update("Note", values, "Eleve_id = " + e.id() + " AND Devoir_id = " + this.id(), null);
        return this.getDatabase().getNote(e, this);
    }

    /**
     * Déclare un élève absent pour ce devoir et modifie l'entité Note associée en conséquence.
     * @param e
     * @return la Note associé en cas de succès, null sinon
     */
    public Note setAbsent(Eleve e)
    {
        Note note = this.getDatabase().getNote(e, this);
        if(note == null)
        {
            return this.getDatabase().addNote(e, this, null, "");
        }


        ContentValues values = new ContentValues();
        values.put("Note_absent", true);
        values.putNull("Note_valeur");

        int rowsAffected = this.getDatabase().update("Note", values, "Note_id = " + this.id(), null);
        return this.getDatabase().getNote(e, this);
    }


    /**
     * Renvoie la moyenne des notes
     * @param notes
     * @return
     */
    public static double moyenne(List<Note> notes)
    {
        int nbr_notes = 0;
        double somme = 0.;
        for(Note n : notes)
        {
            if(n.getAbsent()) continue;
            nbr_notes += 1;
            somme += n.getValeur();
        }

        return somme / nbr_notes;
    }

    /**
     * Calcul la moyenne du devoir.
     * @return
     */
    public double moyenne()
    {
        return Devoir.moyenne(this.getNotes());
    }

    /**
     * Renvoie le nombre d'élève noté absent sur l'ensemble des notes
     * @param notes
     * @return
     */
    public static int nbrAbsent(List<Note> notes)
    {
        int nbr_absent = 0;
        for(Note n : notes)
        {
            if(n.getAbsent()) nbr_absent += 1;
        }

        return nbr_absent;
    }

    /**
     * Renvoie le nombre d'élève absent pour ce devoir.
     * @return
     */
    public int nbrAbsent()
    {
        return Devoir.nbrAbsent(this.getNotes());
    }

    /**
     * Renvoie la liste des élèves n'ayant pas de note pour ce devoir.
     * <p>
     * Note: les élèves notés absent sont considérés comme ayant une note.
     * </p>
     * @return
     */
    public List<Eleve> getElevesNonNotes()
    {
        List<Eleve> eleves = this.getDatabase().getEleves();
        List<Note> notes = this.getNotes();
        for(int i = 0; i < eleves.size(); i++)
        {
            Eleve e = eleves.get(i);
            for(Note n : notes)
            {
                if(n.getEleve().id() == e.id())
                {
                    eleves.remove(i);
                    i--;
                    break;
                }
            }
        }

        return eleves;
    }

    /**
     * Supprime le devoir de la base de données.
     * <p>
     * La fonction commence par supprimer toutes les notes associées
     * au devoir puis supprime le devoir en lui-même.
     * <p>
     * A utiliser avec prudence.
     */
    public void delete()
    {
        List<Note> notes = this.getNotes();
        for(Note n : notes)
        {
            n.delete();
        }
        this.getDatabase().delete(Devoir.TableName, "Devoir_id = " + this.id(), null);
    }

}
