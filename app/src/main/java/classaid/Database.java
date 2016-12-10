package classaid;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import classaid.database.*;

/**
 * Fournit des fonctionnalités permettant d'accéder à une base de données de l'application.
 * <p>
 * Cette classe fournie des méthodes permettant d'effectuer des requêtes sur une base
 * de données SQLite (e.g. query(), insert(), update() et delete()) ainsi que des méthodes
 * permettant de manipuler directement les entités de la base de données (e.g. getEleve(),
 * addEleve(), etc...).
 * <p>
 * Une instance de cette classe ne peut pas être construite par l'utilisateur. La méthode
 * statique getDatabase() doit être utilisée pour obtenir un objet de type Database utilisable.
 * <p>
 * Cette classe est un simple wrapper autour de la classe SQLiteDatabase.
 * @author Vincent on 12/11/2016.
 */

public class Database {

    /**
     * Base de données SQLite.
     */
    private SQLiteDatabase db;
    private int annee;

    /**
     * Construit un objet de type Database à partir d'une base de données SQLite ouverte.
     * @param database
     */
    private Database(SQLiteDatabase database, int a)
    {
        db = database;
        annee = a;
    }

    /**
     * Permet d'ouvrir en lecture seule ou en écriture/lecture une base de données
     * correspondant à une année donnée.
     * <p>
     * Si la base n'existe pas pour l'année donnée, une base utilisable sera automatiquement crée.
     * @param con Un contexte Android (getApplicationContext() permet d'en obtenir un depuis une activité)
     * @param year L'année peremttant d'identifier la base de données.
     * @param readOnly Si vrai, la base sera ouverte en lecture seule.
     * @return Un objet de type Database utilisable pour accéder à la base.
     */
    public static Database getDatabase(Context con, int year, boolean readOnly)
    {
        DatabaseOpenHelper helper = new DatabaseOpenHelper(con, "db_" + year);
        if(readOnly)
        {
            return new Database(helper.getReadableDatabase(), year);
        }
        return new Database(helper.getWritableDatabase(), year);
    }

    /**
     * Renvoie l'année scolaire correspondant à la base de données.
     * @return
     */
    public int getAnnee()
    {
        return annee;
    }

    /**
     * Renvoie la SQLiteDatabase utilisée pour accéder à la base.
     * @return
     */
    public  SQLiteDatabase getSQLiteDatabase()
    {
        return this.db;
    }

    /**
     * Renvoie true si la base est ouverte en lecture seule; faux sinon.
     * @return
     */
    public boolean isReadOnly()
    {
        return db.isReadOnly();
    }

    /**
     * Ferme la base de données.
     * <p>
     * Après cette opération, la base ne pourra plus être utilisée. Il faudrat faire de
     * nouveau appel à getDatabase() pour y accéder.
     *
     */
    public void close()
    {
        db.close();
    }

    /**
     * Permet d"éxecuter une requête SQL de type SELECT.
     * Simple wrapper autour de SQLiteDatabase#query().
     * <p>
     * Il n'est pas recommandé d'utiliser directement cette méthode pour accéder à la base mais
     * plutôt d'utiliser des méthodes permettant de manipuler directement les entités de la base.
     * <p>
     * La méthode rawQuery() peut être utilisée pour faire une requête sur la base sous la
     * forme d'une chaîne de caractères.
     * <p>
     * Voir la classe SQLiteDatabase pour plus de détails.
     * @param table Le nom de la table
     * @param columns Le nom des colonnes à récuperer
     * @param select Clause WHERE de la requête (pouvant contenir des marqueurs '?')
     * @param selectArgs Valeur des marqueurs '?' de la clause WHERE
     * @param groupBy Clause GROUP BY de la requête
     * @param having Clause HAVING de la requête
     * @param orderBy Clause ORDER BY de la requête
     * @return
     */
    public Cursor query(String table, String[] columns, String select, String[] selectArgs, String groupBy, String having, String orderBy)
    {
        return db.query(table, columns, select, selectArgs, groupBy, having, orderBy);
    }

    /**
     * Permet d'effectuer une requête SQL de type SELECT sur la base.
     * Cette fonction est un simple wrapper sur la methode SQLiteDatabase#rawQuery().
     * <p>
     * Il n'est pas recommandé d'utiliser directement cette méthode pour accéder à la base mais
     * plutôt d'utiliser des méthodes permettant de manipuler directement les entités de la base.
     * <p>
     * Voir la classe SQLiteDatabase pour plus de détails.
     * @param sql La requête SQL
     * @param selectionArgs
     * @return
     */
    public Cursor rawQuery(String sql, String[] selectionArgs)
    {
        return db.rawQuery(sql, selectionArgs);
    }

    /**
     * Insére un tuple dans la table donnée.
     * <p>
     * Voir la classe SQLiteDatabase pour plus de détails.
     * @param table Le nom de la table
     * @param values Les couples colonne/valeur de l'entrée à insérer
     * @return le ROW_ID de l'entrée insérée
     */
    public long insert(String table, ContentValues values)
    {
        return db.insert(table, null, values);
    }

    /**
     * Effectue une mise à jour sur la base.
     * @param table Le nom de la table à mettre à jour
     * @param values Les nouveau couples colonne/valeur des entrées mises à jour.
     * @param whereClause La clause WHERE permettant de sélectionner les tuples à modifier
     * @param whereArgs La valeur des arguments marqués '?' de la clause WHERE
     * @return le nombre de tuple modifié
     */
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs)
    {
        return db.update(table, values, whereClause, whereArgs);
    }

    /**
     * Supprime des tuples de la base
     * @param table Le nom de la table cible
     * @param whereClause La clause WHERE permettant de sélectionner les tuples à supprimer
     * @param whereArgs La valeur des arguments marqués '?' de la clause WHERE.
     * @return le nombre de tuples supprimés.
     */
    public int delete(String table, String whereClause, String[] whereArgs)
    {
        return db.delete(table, whereClause, whereArgs);
    }

    /**
     * Renvoie la liste des élèves présent dans la base de données.
     * @return
     */
    public List<Eleve> getEleves()
    {
        List<Eleve> list = new ArrayList<Eleve>();

        Cursor c = this.rawQuery("SELECT " + Eleve.SelectClause +
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

    /**
     * Récupère l'élève à partir de son id.
     * @param id
     * @return null si il n'y a pas d'élève ayant l'id donné
     */
    public Eleve getEleve(int id)
    {
        Cursor c = this.rawQuery("SELECT " + Eleve.SelectClause +
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

    /**
     * Récupère un élève à partir de son nom et de son prénom.
     * <p>
     * Attention, la fonction est sensible à la casse.
     * @param nom
     * @param prenom
     * @return null s'il n'y a pas d'élève avec les nom et prénom donnés.
     */
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

    /**
     * Ajoute un élève à la base de données.
     * @param nom
     * @param prenom
     * @param naissance
     * @param sexe 0 pour masculin, 1 pour féminin
     * @return
     */
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

    /**
     * Supprime un élève de la base de données.
     * <p>
     * Toutes les données associés à l'élève (notes, appréciation) sont également supprimées.
     * @param e l'élève
     * @return true en cas de succès, false sinon
     */
    public boolean removeEleve(Eleve e)
    {
        this.delete(Appreciation.TableName, "Eleve_id = " + e.id(), null);
        this.delete(Note.TableName, "Eleve_id = " + e.id(), null);
        this.delete(DonneeSupplementaire.TableName, "Eleve_id = " + e.id(), null);
        int res = this.delete(Eleve.TableName, "Eleve_id = " + e.id(), null);
        this.delete("Personne", "Personne_id = " + e.getPersonneId(), null);
        return res == 1;
    }

    /**
     * Récupère une donnée supplémentaire à partir de son id.
     * @param id
     * @return null s'il n'existe pas de donnée supplémentaire ayant l'id fournit.
     */
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

    /**
     * Renvoie la liste des données supplémentaires associées à un élève.
     * @param e
     * @return
     */
    public List<DonneeSupplementaire> getDonneesSupplementaires(Eleve e)
    {
        Cursor c = this.rawQuery("SELECT " + DonneeSupplementaire.SelectClause +
                " FROM DonneeSupplementaire WHERE DonneeSupplementaire.Eleve_id = " + e.id(), null);

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

    /**
     * Ajoute une donnée supplémentaire à un élève.
     * @param e l'élève
     * @param nom le nom de la donnée
     * @param val la valeur de la donnée
     * @return
     */
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

    /**
     * Renvoie la liste des compétences (qui ne sont pas des sous-compétences).
     * @return
     */
    public List<Competence> getCompetences()
    {
        Cursor c = this.rawQuery("SELECT " + Competence.SelectClause +
                "FROM Competence WHERE Competence_parent IS NULL ", null);

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

    /**
     * Renvoie la liste des sous-compétences d'une compétence.
     * @param parent
     * @return
     */
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


    /**
     * Récupère une compétence à partir de son id.
     * @param id
     * @return
     */
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

    /**
     * Ajoute une compétence à la base.
     * @param nom
     * @return la compétence ajoutée ou null si l'ajout a échoué
     */
    public Competence addCompetence(String nom)
    {
        ContentValues values = new ContentValues();
        values.put("Competence_nom", nom);
        long competence_id = this.insert("Competence", values);
        if(competence_id == -1) return null;
        return getCompetence((int)competence_id);
    }

    /**
     * Ajoute une sous-compétence à la base
     * @param nom Le nom de la nouvelle compétence
     * @param parent La compétence mère de la nouvelle compétence
     * @return La compétence en cas de succès, null sinon.
     */
    public Competence addCompetence(String nom, Competence parent)
    {
        ContentValues values = new ContentValues();
        values.put("Competence_nom", nom);
        values.put("Competence_parent", parent.id());
        long competence_id = this.insert("Competence", values);
        if(competence_id == -1) return null;
        return getCompetence((int)competence_id);
    }

    /**
     * Récupère un devoir à partir de son id.
     * @param id
     * @return null s'il n'y a pas de devoir dans la base ayant cet id
     */
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

    /**
     * Ajoute un devoir à la base.
     * <p>
     * Si creationNoteAuto vaut vrai, la fonction ajoutera également à la base les entrées
     * correspondant aux notes des élèves pour ce devoir (la valeur la note sera mis à null).
     * @param comp Compétence lié au devoir
     * @param d Date du devoir
     * @param typeNotation l'id du type de notation
     * @param creationNoteAuto vrai pour créer automatiquement les notes des élèves
     * @return le devoir ajouté en cas de succès, null sinon.
     */
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

    /**
     * Renvoie la liste des devoirs associée à la compétence donnée en paramètre.
     * @param comp
     * @return
     */
    public List<Devoir> getDevoirs(Competence comp)
    {
        Cursor c = this.rawQuery("SELECT " + Devoir.SelectClause +
                " FROM Devoir " +
                " WHERE Competence_id = " + comp.id(), null);

        List<Devoir> list = new ArrayList<Devoir>();
        if(!c.moveToFirst())
        {
            c.close();
            return list;
        }
        else
        {
            list.add(new Devoir(this, c));
        }

        while(c.moveToNext())
        {
            list.add(new Devoir(this, c));
        }

        c.close();
        return list;
    }

    /**
     * Récupère une note à partir de son id
     * @param id
     * @return null si la note demandée n'existe pas
     */
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

    /**
     * Récupère la note d'un élève pour un devoir donnné
     * @param e l'élève
     * @param d le devoir
     * @return null en cas d'échec, la Note sinon
     */
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

    /**
     * Renvoie la liste des notes associée à un devoir
     * @param d le devoir
     * @return
     */
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

    /**
     * Renvoie la liste des notes associées à un élève
     * @param e l'élève
     * @return
     */
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

    /**
     * Renvoie les notes d'un élève lié à un trimestre
     * @param e l'élève
     * @param trimestre l'id du trimestre (1, 2 ou 3)
     * @return
     */
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

    /**
     * Renvoie les notes d'un élève pour une compétence et un trimestre
     * @param e l'élève
     * @param comp la compétence
     * @param trimestre le numéro du trimestre (1, 2 ou 3)
     * @return
     */
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

    /**
     * Renvoie les notes d'un élève pour une compétence
     * @param e l'élève
     * @param comp la compétence
     * @return
     */
    public List<Note> getNotes(Eleve e, Competence comp)
    {
        Cursor c = this.rawQuery("SELECT " + Note.SelectClause +
                " FROM Note JOIN Devoir ON Note.Devoir_id = Devoir.Devoir_id " +
                " JOIN Competence ON Competence.Competence_id = Devoir.Competence_id " +
                " JOIN TypeNotation ON Devoir.TypeNotation_id = TypeNotation.TypeNotation_id " +
                " JOIN Trimestre ON date(Devoir_date) BETWEEN date(Trimestre_dateDebut) AND date(Trimestre_dateFin) " +
                " WHERE Note.Eleve_id = " + e.id() +
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

    /**
     * Ajoute une note à la base de données
     * @param e l'élève
     * @param d le devoir
     * @param val la valeur de la note (peut valoir null pour un élève absent)
     * @param com le commentaire associé à la note
     * @return la note ajouté en cas de succès, null sinon
     */
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

    /**
     * Récupère une entité Trimestre
     * @param num le numéro du trimestre (1, 2 ou 3)
     * @return null en cas d'échec
     */
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

    /**
     * Renvoie l'entité Trimestre associé à un jour donné
     * @param d la date
     * @return null en cas d'éched
     */
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

    /**
     * Renvoie la liste des compétences associés à un trimestre.
     * @param t l'entité Trimestre associé au trimestre
     * @return
     */
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

    /**
     * Renvoie l'appréciation d'un élève pour une compétence
     * @param e l'élève
     * @param comp la compétence
     * @return null en cas d'échec
     */
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

    /**
     * Ajoute une appréciation à la base
     * @param e l'élève
     * @param comp la compétence
     * @param val le commentaire associé
     * @return l'appréciation ajouté en cas de succès, null sinon
     */
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
