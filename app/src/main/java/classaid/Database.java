package classaid;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by Vincent on 12/11/2016.
 */

import classaid.database.*;

public class Database {

    private SQLiteDatabase db;

    private Database(SQLiteDatabase database)
    {
        db = database;
    }

    public static Database getDatabase(Context con, int year, boolean readOnly)
    {
        DatabaseOpenHelper helper = new DatabaseOpenHelper(con, "db_" + year);
        if(readOnly)
        {
            return new Database(helper.getReadableDatabase());
        }
        return new Database(helper.getWritableDatabase());
    }

    public  SQLiteDatabase getSQLiteDatabase()
    {
        return this.db;
    }

    public boolean isReadOnly()
    {
        return db.isReadOnly();
    }

    public void close()
    {
        db.close();
    }

    public Cursor query(String table, String[] columns, String select, String[] selectArgs, String groupBy, String having, String orderBy)
    {
        return db.query(table, columns, select, selectArgs, groupBy, having, orderBy);
    }

    public Cursor rawQuery(String sql, String[] selectionArgs)
    {
        return db.rawQuery(sql, selectionArgs);
    }

    public long insert(String table, ContentValues values)
    {
        return db.insert(table, null, values);
    }

    public int update(String table, ContentValues values, String whereClause, String[] whereArgs)
    {
        return db.update(table, values, whereClause, whereArgs);
    }

    public int delete(String table, String whereClause, String[] whereArgs)
    {
        return db.delete(table, whereClause, whereArgs);
    }

    public List<Eleve> getEleves()
    {
        List<Eleve> list = new ArrayList<Eleve>();

        Cursor c = this.rawQuery("SELECT Eleve_id, Eleve.Personne_id, Personne_nom, Personne_prenom, Personne_dateNaissance, Personne_sexe " +
        " FROM Eleve JOIN Personne ON Eleve.Personne_id = Personne.Personne_id", null);

        if(!c.moveToFirst())
        {
            c.close();
            return list;
        }
        else
        {
            list.add(new Eleve(this, c));
        }

        while(c.moveToNext())
        {
            list.add(new Eleve(this, c));
        }

        c.close();
        return list;
    }

    public Eleve getEleve(int id)
    {
        Cursor c = this.rawQuery("SELECT Eleve_id, Eleve.Personne_id, Personne_nom, Personne_prenom, Personne_dateNaissance, Personne_sexe " +
                "FROM Eleve JOIN Personne ON Eleve.Personne_id = Personne.Personne_id " +
                " WHERE Eleve_id = " + id, null);

        if(!c.moveToFirst())
        {
            c.close();
            return null;
        }

        Eleve ret = new Eleve(this, c);
        c.close();
        return ret;
    }

    public Eleve getEleve(String nom, String prenom)
    {
        Cursor c = this.rawQuery("SELECT " + Eleve.SelectClause +
                " FROM Eleve JOIN Personne ON Eleve.Personne_id = Personne.Personne_id " +
                " WHERE Personne_nom = '" + nom + "' AND Personne_prenom = '" + prenom + "'", null);

        if(!c.moveToFirst())
        {
            c.close();
            return null;
        }

        Eleve ret = new Eleve(this, c);
        c.close();
        return ret;
    }

    public Eleve addEleve(String nom, String prenom, Date naissance, int sexe)
    {
        ContentValues values = new ContentValues();
        values.put("Personne_prenom", prenom);
        values.put("Personne_nom", nom);
        values.put("Personne_dateNaissance", naissance.getTime());
        values.put("Personne_sexe", sexe);
        long personne_id = this.insert("Personne", values);
        if(personne_id == -1) return null;

        // on peut aussi utiliser c.moveToLast() pour récuperer le dernier element inserer
        // ce serait d'ailleurs plus propre de faire comme ça

        values = new ContentValues();
        values.put("Personne_id", personne_id);
        long eleve_id = this.insert("Eleve", values);
        if(eleve_id == -1) return null;

        return getEleve((int)eleve_id);
    }

    public DonneeSupplementaire getDonneeSupplementaire(int id)
    {
        Cursor c = this.rawQuery("SELECT " + DonneeSupplementaire.SelectClause +
                " FROM DonneeSupplementaire " +
                " WHERE DonneeSupplementaire_id = " + id, null);

        if(!c.moveToFirst())
        {
            c.close();
            return null;
        }

        DonneeSupplementaire ret = new DonneeSupplementaire(this, c);
        c.close();
        return ret;
    }

    public List<DonneeSupplementaire> getDonneesSupplementaires(Eleve e)
    {
        Cursor c = this.rawQuery("SELECT " + DonneeSupplementaire.SelectClause +
                " FROM DonneeSupplementaire ", null);

        List<DonneeSupplementaire> list = new ArrayList<DonneeSupplementaire>();
        if(!c.moveToFirst())
        {
            c.close();
            return list;
        }
        else
        {
            list.add(new DonneeSupplementaire(this, c));
        }

        while(c.moveToNext())
        {
            list.add(new DonneeSupplementaire(this, c));
        }

        c.close();
        return list;
    }

    public DonneeSupplementaire addDonneeSupplementaire(Eleve e, String nom, String val)
    {
        ContentValues values = new ContentValues();
        values.put("DonneeSupplementaire_nom", nom);
        values.put("DonneeSupplementaire_valeur", val);
        values.put("Eleve_id", e.id());
        long donnee_supplementaire_id = this.insert(DonneeSupplementaire.TableName, values);
        if(donnee_supplementaire_id == -1) return null;
        return getDonneeSupplementaire((int)donnee_supplementaire_id);
    }

    public List<Competence> getCompetences()
    {
        Cursor c = this.rawQuery("SELECT " + Competence.SelectClause +
                "FROM Competence ", null);

        List<Competence> list = new ArrayList<Competence>();
        if(!c.moveToFirst())
        {
            c.close();
            return list;
        }
        else
        {
            list.add(new Competence(this, c));
        }

        while(c.moveToNext())
        {
            list.add(new Competence(this, c));
        }

        c.close();
        return list;
    }

    public List<Competence> getSousCompetences(Competence parent)
    {
        Cursor c = this.rawQuery("SELECT " + Competence.SelectClause +
                " FROM Competence " +
                " WHERE Competence_parent = " + parent.id(), null);

        List<Competence> list = new ArrayList<Competence>();
        if(!c.moveToFirst())
        {
            c.close();
            return list;
        }
        else
        {
            list.add(new Competence(this, c));
        }

        while(c.moveToNext())
        {
            list.add(new Competence(this, c));
        }

        c.close();
        return list;
    }


    public Competence getCompetence(int id)
    {
        Cursor c = this.rawQuery("SELECT " + Competence.SelectClause +
                " FROM Competence " +
                " WHERE Competence_id = " + id, null);

        if(!c.moveToFirst())
        {
            c.close();
            return null;
        }

        Competence ret = new Competence(this, c);
        c.close();
        return ret;
    }

    public Competence addCompetence(String nom)
    {
        ContentValues values = new ContentValues();
        values.put("Competence_nom", nom);
        long competence_id = this.insert("Competence", values);
        if(competence_id == -1) return null;
        return getCompetence((int)competence_id);
    }

    public Competence addCompetence(String nom, Competence parent)
    {
        ContentValues values = new ContentValues();
        values.put("Competence_nom", nom);
        values.put("Competence_parent", parent.id());
        long competence_id = this.insert("Competence", values);
        if(competence_id == -1) return null;
        return getCompetence((int)competence_id);
    }

    public Devoir getDevoir(int id)
    {
        Cursor c = this.rawQuery("SELECT " + Devoir.SelectClause +
                " FROM Devoir " +
                " WHERE Devoir_id = " + id, null);

        if(!c.moveToFirst())
        {
            c.close();
            return null;
        }

        Devoir ret = new Devoir(this, c);
        c.close();
        return ret;
    }

    public Devoir addDevoir(Competence comp, Date d, int typeNotation, boolean creationNoteAuto)
    {
        ContentValues values = new ContentValues();
        values.put("Devoir_date", d.getTime());
        values.put("Competence_id", comp.id());
        values.put("TypeNotation_id", typeNotation);
        long devoir_id = this.insert("Devoir", values);
        if(devoir_id == -1) return null;

        if(creationNoteAuto)
        {
            List<Eleve> eleves = getEleves();
            for(Eleve e : eleves)
            {
                values = new ContentValues();
                values.put("Note_absent", 0);
                values.put("Note_commentaire", "");
                values.putNull("Note_valeur");
                values.put("Devoir_id", devoir_id);
                values.put("Eleve_id", e.id());
                this.insert("Note", values);
            }
        }

        return getDevoir((int)devoir_id);
    }

    public Note getNote(int id)
    {
        Cursor c = this.rawQuery("SELECT " + Note.SelectClause +
                " FROM Note JOIN Devoir ON Note.Devoir_id = Devoir.Devoir_id " +
                " JOIN TypeNotation ON Devoir.TypeNotation_id = TypeNotation.TypeNotation_id " +
                " WHERE Note_id = " + id, null);

        if(!c.moveToFirst())
        {
            c.close();
            return null;
        }

        Note ret = new Note(this, c);
        c.close();
        return ret;
    }

    public Note getNote(Eleve e, Devoir d)
    {
        Cursor c = this.rawQuery("SELECT " + Note.SelectClause +
                " FROM Note JOIN Devoir ON Note.Devoir_id = Devoir.Devoir_id " +
                " JOIN TypeNotation ON Devoir.TypeNotation_id = TypeNotation.TypeNotation_id " +
                " WHERE Note.Eleve_id = " + e.id() + " AND Note.Devoir_id = " + d.id(), null);

        if(!c.moveToFirst())
        {
            c.close();
            return null;
        }

        Note ret = new Note(this, c);
        c.close();
        return ret;
    }

    public List<Note> getNotes(Devoir d)
    {
        Cursor c = this.rawQuery("SELECT " + Note.SelectClause +
                " FROM Note JOIN Devoir ON Note.Devoir_id = Devoir.Devoir_id " +
                " JOIN TypeNotation ON Devoir.TypeNotation_id = TypeNotation.TypeNotation_id " +
                " WHERE Note.Devoir_id = " + d.id(), null);

        List<Note> list = new ArrayList<Note>();
        if(!c.moveToFirst())
        {
            c.close();
            return list;
        }
        else
        {
            list.add(new Note(this, c));
        }

        while(c.moveToNext())
        {
            list.add(new Note(this, c));
        }

        c.close();
        return list;
    }

    public List<Note> getNotes(Eleve e)
    {
        Cursor c = this.rawQuery("SELECT " + Note.SelectClause +
                " FROM Note JOIN Devoir ON Note.Devoir_id = Devoir.Devoir_id " +
                " JOIN TypeNotation ON Devoir.TypeNotation_id = TypeNotation.TypeNotation_id " +
                " WHERE Note.Eleve_id = " + e.id(), null);

        List<Note> list = new ArrayList<Note>();
        if(!c.moveToFirst())
        {
            c.close();
            return list;
        }
        else
        {
            list.add(new Note(this, c));
        }

        while(c.moveToNext())
        {
            list.add(new Note(this, c));
        }

        c.close();
        return list;
    }

    public List<Note> getNotes(Eleve e, int trimestre)
    {
        Cursor c = this.rawQuery("SELECT " + Note.SelectClause +
                " FROM Note JOIN Devoir ON Note.Devoir_id = Devoir.Devoir_id " +
                " JOIN TypeNotation ON Devoir.TypeNotation_id = TypeNotation.TypeNotation_id " +
                " JOIN Trimestre ON date(Devoir_date) BETWEEN date(Trimestre_dateDebut) AND date(Trimestre_dateFin) " +
                " WHERE Note.Eleve_id = " + e.id() + " AND Trimestre_id = " + trimestre, null);

        List<Note> list = new ArrayList<Note>();
        if(!c.moveToFirst())
        {
            c.close();
            return list;
        }
        else
        {
            list.add(new Note(this, c));
        }

        while(c.moveToNext())
        {
            list.add(new Note(this, c));
        }

        c.close();
        return list;
    }

    public List<Note> getNotes(Eleve e, Competence comp, int trimestre)
    {
        Cursor c = this.rawQuery("SELECT " + Note.SelectClause +
                " FROM Note JOIN Devoir ON Note.Devoir_id = Devoir.Devoir_id " +
                " JOIN Competence ON Competence.Competence_id = Devoir.Competence_id " +
                " JOIN TypeNotation ON Devoir.TypeNotation_id = TypeNotation.TypeNotation_id " +
                " JOIN Trimestre ON date(Devoir_date) BETWEEN date(Trimestre_dateDebut) AND date(Trimestre_dateFin) " +
                " WHERE Note.Eleve_id = " + e.id() + " AND Trimestre_id = " + trimestre +
                " AND Competence.Competence_id = " + comp.id() , null);

        List<Note> list = new ArrayList<Note>();
        if(!c.moveToFirst())
        {
            c.close();
            return list;
        }
        else
        {
            list.add(new Note(this, c));
        }

        while(c.moveToNext())
        {
            list.add(new Note(this, c));
        }

        c.close();
        return list;
    }

    public Note addNote(Eleve e, Devoir d, Float val, String com)
    {
        ContentValues values = new ContentValues();
        if(val == null) {
            values.put("Note_absent", true);
            values.putNull("Note_valeur");
        } else {
            values.put("Note_absent", false);
            values.put("Note_valeur", val.floatValue());
        }
        if(com == null) {
            values.putNull("Note_commentaire");
        } else {
            values.put("Note_commentaire", com);
        }

        values.put("Eleve_id", e.id());
        values.put("Devoir_id", d.id());
        long note_id = this.insert("Note", values);
        if(note_id == -1) return null;

        // get last inserted tuple

        Cursor c = this.rawQuery("SELECT " + Note.SelectClause +
                " FROM Note JOIN Devoir ON Note.Devoir_id = Devoir.Devoir_id " +
                " JOIN TypeNotation ON Devoir.TypeNotation_id = TypeNotation.TypeNotation_id ", null);
        if(!c.moveToLast())
        {
            c.close();
            return null;
        }
        Note ret = new Note(this, c);
        c.close();
        return ret;
    }

    public Trimestre getTrimestre(int num)
    {
        Cursor c = this.rawQuery("SELECT " + Trimestre.SelectClause +
                " FROM Trimestre " +
                " WHERE Trimestre_id = " + num, null);

        if(!c.moveToFirst())
        {
            c.close();
            return null;
        }

        Trimestre ret = new Trimestre(this, c);
        c.close();
        return ret;
    }

    public Trimestre getTrimestre(Date d)
    {
        Cursor c = this.rawQuery("SELECT " + Trimestre.SelectClause +
                " FROM Trimestre " +
                " WHERE date(" + d.getTime() + ") BETWEEN date(Trimestre_dateDebut) AND date(Trimestre_dateFin)", null);

        if(!c.moveToFirst())
        {
            c.close();
            return null;
        }

        Trimestre ret = new Trimestre(this, c);
        c.close();
        return ret;
    }

    public List<Competence> getContenusTrimestre(Trimestre t)
    {
        if(t == null) { return null; }

        Cursor c = this.rawQuery("SELECT " + Competence.SelectClause +
                " FROM Competence " +
                " JOIN ContenuTrimestre ON Competence.Competence_id = ContenuTimestre_Competence_id", null);

        List<Competence> list = new ArrayList<Competence>();
        if(!c.moveToFirst())
        {
            c.close();
            return list;
        }
        else
        {
            list.add(new Competence(this, c));
        }

        while(c.moveToNext())
        {
            list.add(new Competence(this, c));
        }

        c.close();
        return list;
    }

    public Appreciation getAppreciation(Eleve e, Competence comp)
    {
        Cursor c = this.rawQuery("SELECT " + Appreciation.SelectClause +
                " FROM Appreciation " +
                " WHERE Appreciation.Eleve_id = " + e.id() + " AND Appreciation.Competence_id = " + comp.id(), null);

        if(!c.moveToFirst())
        {
            c.close();
            return null;
        }

        Appreciation ret = new Appreciation(this, c);
        c.close();
        return ret;
    }

    public Appreciation addAppreciation(Eleve e, Competence comp, String val)
    {
        ContentValues values = new ContentValues();
        values.put("Appreciation_commentaire", val);
        values.put("Eleve_id", e.id());
        values.put("Competence_id", comp.id());
        long appreciation_id = this.insert("Appreciation", values);
        if(appreciation_id == -1) return null;

        // get last inserted tuple
        Cursor c = this.rawQuery("SELECT " + Appreciation.SelectClause +
                " FROM Appreciation ", null);
        if(!c.moveToLast())
        {
            c.close();
            return null;
        }
        Appreciation ret = new Appreciation(this, c);
        c.close();
        return ret;
    }

}
