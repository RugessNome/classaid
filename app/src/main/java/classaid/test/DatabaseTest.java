package classaid.test;

import android.content.Context;
import android.database.Cursor;
import android.widget.TextView;

/**
 * Created by Vincent on 13/11/2016.
 */

import java.sql.Date;
import java.util.List;

import classaid.Database;
import classaid.database.*;


public class DatabaseTest {

    private Context context;
    private TextView log;
    private Database db;

    private DatabaseTest(Context c, TextView l)
    {
        context = c;
        log = l;
    }

    private void print(String str)
    {
        log.append(str + "\n");
    }

    private void run()
    {
        db = Database.getDatabase(context, 2016, false);

        // Ajout des eleves
        Eleve john = db.addEleve("Doe", "John", Date.valueOf("2001-09-11"), 0);
        Eleve alice = db.addEleve("David", "Alice", Date.valueOf("2000-01-06"), 1);
        Eleve bob = db.addEleve("Sponge", "Bob", Date.valueOf("1999-05-01"), 0);

        Competence lecture = db.addCompetence("Lecture");
        // Ajout de sous-compétences
        lecture.addCompetence("Extraire les informations d'un texte court");
        lecture.addCompetence("Savoir lire à voie haute");

        Competence ecriture = db.addCompetence("Ecriture");
        ecriture.addCompetence("Savoir écriture les yeux fermés");
        ecriture.addCompetence("Manipuler correctement la ponctuation");

        Competence math = db.addCompetence("Mathématiques");
        Competence gauss = math.addCompetence("Calculer l'intégrale de Gauss");
        math.addCompetence("Calculer le discrimant d'un polynôme de degré deux et en calculer les racines");
        math.addCompetence("Démontrer l'identité de Bézout");

        Devoir calculerIntegraleGauss = gauss.addDevoir(Date.valueOf("2016-11-13"), TypeNotation.NotationSur20, true);
        calculerIntegraleGauss.setNote(bob, 17.5f, "Good job Bob !");

        math.setAppreciation(bob, "Très carré, bravo Bob");

        List<Eleve> eleves = db.getEleves();
        print("Nombre d'eleves dans la base : " + eleves.size());

        dumpDatabase();

    }

    private String getDate(Cursor c, int column)
    {
        if(c.isNull(column)) return "null";
        return new Date(c.getLong(column)).toString();
    }

    private String getInt(Cursor c, int column)
    {
        if(c.isNull(column)) return "null";
        return "" + c.getInt(column);
    }

    private String getFloat(Cursor c, int column)
    {
        if(c.isNull(column)) return "null";
        return "" + c.getFloat(column);
    }

    private void dumpDatabase()
    {
        print("Personne");
        Cursor c = db.rawQuery("SELECT Personne_id, Personne_nom, Personne_prenom, Personne_dateNaissance, Personne_sexe FROM Personne", null);
        while(c.moveToNext())
        {
            String str = "";
            str += c.getInt(0) + ",";
            str += c.getString(1) + ", ";
            str += c.getString(2) + ", ";
            str += (new Date(c.getLong(3))).toString() + ", ";
            str += c.getString(4);
            print(str);
        }

        print("Eleve(Eleve_id, Personne_id)");
        c = db.rawQuery("SELECT Eleve_id, Personne_id FROM Eleve", null);
        while(c.moveToNext())
        {
            String str = "";
            str += c.getString(0) + ",";
            str += c.getString(1);
            print(str);
        }

        print("Competence(Competence_id, Competence_nom, Competence_parent)");
        c = db.rawQuery("SELECT Competence_id, Competence_nom, Competence_parent FROM Competence", null);
        while(c.moveToNext())
        {
            String str = "";
            str += c.getInt(0) + ",";
            str += c.getString(1) + ", ";
            str += (c.isNull(2) ? "null" : c.getString(2));
            print(str);
        }

        print("Devoir(Devoir_id, Devoir_date, TypeNotation_id, Competence_id)");
        c = db.rawQuery("SELECT Devoir_id, Devoir_date, TypeNotation_id, Competence_id FROM Devoir", null);
        while(c.moveToNext())
        {
            String str = "";
            str += c.getInt(0) + ",";
            str += getDate(c, 1) + ", ";
            str += c.getInt(2) + ", ";
            str += c.getInt(3);
            print(str);
        }

        print("Note(Note_id, Note_absent, Note_commentaire, Note_valeur, Devoir_id, Eleve_id)");
        c = db.rawQuery("SELECT Note_id, Note_absent, Note_commentaire, Note_valeur, Devoir_id, Eleve_id FROM Note", null);
        while(c.moveToNext())
        {
            String str = "";
            str += c.getInt(0) + ",";
            str += c.getInt(1) + ", ";
            str += c.getString(2) + ", ";
            str += getFloat(c, 3) + ", ";
            str += getInt(c, 4) + ", ";
            str += getInt(c, 5);
            print(str);
        }
    }

    public static void exec(Context con, TextView log)
    {
        DatabaseTest test = new DatabaseTest(con, log);
        test.run();
    }


}
