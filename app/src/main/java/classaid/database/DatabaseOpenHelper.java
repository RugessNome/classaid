package classaid.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;

/**
 * Helper permettant d'ouvrir une base de données.
 * @author by Vincent on 12/11/2016.
 */

public class DatabaseOpenHelper extends SQLiteOpenHelper {

    final static private int database_version = 1;

    public DatabaseOpenHelper(Context con, String dbName)
    {
        super(con, dbName, null, database_version);
    }

    /**
     * Exécute les requêtes SQL permettant de créer les tables de la base.
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(
        "CREATE TABLE Personne(" +
        "Personne_id INTEGER PRIMARY KEY,"+
        "Personne_nom TEXT," +
        "Personne_prenom TEXT," +
        "Personne_dateNaissance NUMERIC," +
        "Personne_sexe INTEGER" +
        ");");


        db.execSQL("CREATE TABLE Eleve(" +
                "Eleve_id INTEGER PRIMARY KEY,"+
                "Personne_id INTEGER," +
                " " +
                "FOREIGN KEY(Personne_id) REFERENCES Personne(Personne_id) ON DELETE CASCADE" +
                ");");


        db.execSQL("CREATE TABLE DonneeSupplementaire(" +
                "DonneeSupplementaire_id INTEGER PRIMARY KEY,"+
                "DonneeSupplementaire_nom TEXT NOT NULL," +
                "DonneeSupplementaire_valeur TEXT," +
                "Eleve_id INTEGER," +
                " " +
                "FOREIGN KEY(Eleve_id) REFERENCES Eleve(Eleve_id)  ON DELETE CASCADE" +
                ");");


        db.execSQL("CREATE TABLE Trimestre(" +
                "Trimestre_id INTEGER NOT NULL,"+
                "Trimestre_dateDebut NUMERIC," +
                "Trimestre_dateFin NUMERIC" +
                ");");

        ContentValues trimestre1 = new ContentValues();
        trimestre1.put("Trimestre_id", 1);
        ContentValues trimestre2 = new ContentValues();
        trimestre2.put("Trimestre_id", 2);
        ContentValues trimestre3 = new ContentValues();
        trimestre3.put("Trimestre_id", 3);
        db.insert("Trimestre", null, trimestre1);
        db.insert("Trimestre", null, trimestre2);
        db.insert("Trimestre", null, trimestre3);



        db.execSQL("CREATE TABLE Competence(" +
                "Competence_id INTEGER PRIMARY KEY,"+
                "Competence_nom TEXT," +
                "Competence_parent INTEGER," +
                " " +
                "FOREIGN KEY(Competence_parent) REFERENCES Competence(Competence_id)  ON DELETE CASCADE" +
                ");");


        db.execSQL("CREATE TABLE Appreciation(" +
                "Appreciation_id INTEGER PRIMARY KEY,"+
                "Appreciation_commentaire TEXT," +
                "Eleve_id INTEGER," +
                "Competence_id INTEGER," +
                " " +
                "FOREIGN KEY(Eleve_id) REFERENCES Eleve(Eleve_id)," +
                "FOREIGN KEY(Competence_id) REFERENCES Competence(Competence_id)  ON DELETE CASCADE" +
                ");");


        db.execSQL("CREATE TABLE TypeNotation(" +
                "TypeNotation_id INTEGER PRIMARY KEY,"+
                "TypeNotation_nom TEXT" +
                ");");

        ContentValues typenotation_20 = new ContentValues();
        typenotation_20.put("TypeNotation_id", TypeNotation.NotationSur20);
        typenotation_20.put("TypeNotation_nom", "Note sur 20");

        ContentValues typenotation_10 = new ContentValues();
        typenotation_10.put("TypeNotation_id", TypeNotation.NotationSur10);
        typenotation_10.put("TypeNotation_nom", "Note sur 10");

        db.insert("TypeNotation", null, typenotation_20);
        db.insert("TypeNotation", null, typenotation_10);


        db.execSQL("CREATE TABLE Devoir(" +
                "Devoir_id INTEGER PRIMARY KEY,"+
                "TypeNotation_id INTEGER," +
                "Competence_id INTEGER," +
                "Devoir_date NUMERIC," +
                "Devoir_commentaire TEXT," +
                " " +
                "FOREIGN KEY(Competence_Id) REFERENCES Competence(Competence_Id)  ON DELETE CASCADE," +
                "FOREIGN KEY(TypeNotation_Id) REFERENCES TypeNotation(TypeNotation_Id)  ON DELETE CASCADE" +
                ");");

        db.execSQL("CREATE TABLE Note(" +
                "Note_id INTEGER PRIMARY KEY,"+
                "Note_absent INTEGER," +
                "Note_commentaire TEXT," +
                "Note_valeur REAL," +
                "Eleve_id INTEGER," +
                "Devoir_id INTEGER," +
                " " +
                "FOREIGN KEY(Eleve_id) REFERENCES Eleve(Eleve_id)  ON DELETE CASCADE," +
                "FOREIGN KEY(Devoir_id) REFERENCES Devoir(Devoir_id)  ON DELETE CASCADE" +
                ");");

        db.execSQL("CREATE TABLE ContenuTrimestre(" +
                "ContenuTrimestre_id INTEGER PRIMARY KEY,"+
                "Trimestre_id INTEGER," +
                "Competence_id INTEGER," +
                " " +
                "FOREIGN KEY(Trimestre_id) REFERENCES Trimestre(Trimestre_id) ON DELETE CASCADE," +
                "FOREIGN KEY(Competence_id) REFERENCES Competence(Competence_id)  ON DELETE CASCADE" +
                ");");
    }

    /**
     * Méthode permettant de mettre à jour la base de données. Ne fait rien.
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        return;
    }
}
