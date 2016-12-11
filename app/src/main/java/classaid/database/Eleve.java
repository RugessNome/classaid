package classaid.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by Vincent on 12/11/2016.
 */

import classaid.Database;

/**
 * Représente un élève.
 * @author Vincent
 */
public class Eleve extends DatabaseEntity {

    /**
     * L'id de la personne
     */
    private int personneId;
    /**
     * Le nom
     */
    private String nom;
    /**
     * Le prenom
     */
    private String prenom;
    /**
     * la date de naissance
     */
    private Date dateNaissance;
    /**
     * 0 pour masculin, 1 pour féminin
     */
    private int sexe;
    /**
     * Le chemin de la photo
     */
    private String photo;
    private List<DonneeSupplementaire> donneesSupplementaires;

    public static String TableName = "Eleve";
    public static String SelectClause = " Eleve_id, Eleve.Personne_id, Personne_nom, Personne_prenom, Personne_dateNaissance, Personne_sexe, Eleve_photo ";

    /**
     * Construit une entité Eleve à partir d'un tuple d'une base de données
     * @param d la base
     * @param c le tuple
     */
    public Eleve(Database d, Cursor c)
    {
        super(d, c);

        personneId = c.getInt(1);
        nom = c.getString(2);
        prenom = c.getString(3);
        dateNaissance = new Date(c.getLong(4));
        sexe = c.getInt(5);
        photo = c.getString(6);


    }

    /**
     * Renvoie l'id de l'entité Personne associé à l'élève.
     * @return
     */
    public int getPersonneId()
    {
        return personneId;
    }

    /**
     * Renvoie le nom de l'élève
     * @return
     */
    public String getNom()
    {
        return this.nom;
    }

    /**
     * Modifie le nom de l'élève
     * @param n
     * @return true en cas de succès, false sinon
     */
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

    /**
     * Renvoie le prénom de l'élève
     * @return
     */
    public String getPrenom()
    {
        return this.prenom;
    }

    /**
     * Modifie le prénom de l'élève
     * @param p
     * @return true en cas de succès, false sinon
     */
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

    /**
     * Renvoie le sexe de l'élève
     * @return 0 pour masculin, 1 pour feminin
     */
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

    /**
     * Renvoie la date de naissance de l'élève
     * @return
     */
    public Date getDateNaissance()
    {
        return this.dateNaissance;
    }

    /**
     * Modifie la date de naissance de l'élève
     * @param d
     * @return true en cas de succès, false sinon
     */
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

    /**
     * Renvoie l'age de l'élève
     * @return
     */
    public int getAge()
    {
        if(dateNaissance == null) { return -1; }

        java.util.Date now = new java.util.Date();
        return (int) ((now.getTime() - dateNaissance.getTime()) / 3.15576e+10);
    }

    /**
     * Renvoie la liste des notes de l'élève
     * @return
     */
    public List<Note> getNotes()
    {
        return this.getDatabase().getNotes(this);
    }

    /**
     * Renvoie la liste des données supplémentaires associées à l'élève
     * @return
     */
    public List<DonneeSupplementaire> getDonneesSupplementaires()
    {
        if(donneesSupplementaires != null)
        {
            return donneesSupplementaires;
        }

        this.donneesSupplementaires = this.getDatabase().getDonneesSupplementaires(this);
        return this.donneesSupplementaires;
    }

    /**
     * Ajoute une donnée supplémentaire à cet élève.
     * @param nom
     * @param valeur
     * @return
     */
    public DonneeSupplementaire addDonneeSupplementaire(String nom, String valeur)
    {
        DonneeSupplementaire ret = this.getDatabase().addDonneeSupplementaire(this, nom, valeur);
        if(ret != null) { this.donneesSupplementaires = null; }
        return ret;
    }

    /**
     * Retourne le chemin de la photo de l'élève.
     * <p>
     * La fonction renvoie une chaîne vide si aucune photo n'a été fournie.
     * </p>
     * @return
     */
    public String getPhoto()
    {
        return this.photo;
    }

    /**
     * Renvoie la photo de l'élève sous la forme d'une image Bitmap.
     * <p>
     * Renvoie null si aucune photo n'a été fournie ou si l photo n'a pas pu être trouvée/chargée.
     * </p>
     * @param con
     * @return
     */
    public Bitmap getBitmapPhoto(Context con)
    {
        try {
            File f = new File(getPhoto());
            if(!f.exists()) {
                return null;
            }
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            return b;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * Change le chemin de la photo de l'élève
     * @param chemin
     */
    public boolean setPhoto(String chemin)
    {
        if(chemin.equals(this.photo)) { return true; }

        Database db = this.getDatabase();
        ContentValues values = new ContentValues();
        values.put("Eleve_photo", chemin);

        int rowsAffected = db.update("Eleve", values, "Eleve_id = " + this.id(), null);
        if(rowsAffected > 0)
        {
            this.photo = chemin;
        }
        return rowsAffected == 1;
    }

    /**
     * Récupere l'appréciation de l'élève pour une compétence donnée
     * @param c la compétence
     * @return l'appréciation en cas de succès, null sinon
     */
    public Appreciation getAppreciation(Competence c)
    {
        return this.getDatabase().getAppreciation(this, c);
    }

    /**
     * Renvoie true si l'eleve a une note pour la competence donné ou une des
     * sous-compétences de n'importe quel niveau de profondeur.
     * <p>
     * Note : l'élève doit avoir au moins une note pour laquelle il n'est pas marqué absent
     * pour être considéré comme étant noté.
     * </p>
     * @param c
     * @param t filtre Trimestre (peut valoir null)
     * @return
     */
    public boolean estNote(Competence c, Trimestre t) {
        List<Note> notes = (t != null ? c.getNotes(this, t.id()) : c.getNotes(this));
        for(Note n : notes) {
            if(n.getAbsent() == false) {
                return true;
            }
        }

        for(Competence sc : c.getSousCompetences()) {
            if(estNote(sc, t)) return true;
        }

        return false;
    }

    /**
     * Renvoie la liste des compétences de profondeur 0 pour lesquelles
     * l'élève possède au moins une note dans l'une des sous-compétence
     * de la compétence.
     * <p>
     * La fonction procède de la manière suivante : elle commence par récupérer la liste des notes
     * de l'élève puis pour chacune de ces notes, elle récupère la compétence associée et remonte
     * jusqu'à une compétence de pronfondeur 0 qu'elle ajoute à la liste de résultat.
     * </p>
     * @arg t Filtre les notes par rapport à un trimestre (peut valoir null)
     * @return
     */
    public List<Competence> getCompetencesNotees(Trimestre t)
    {
        List<Competence> ret = new ArrayList<Competence>();
        List<Note> notes = getNotes();
        for(Note n : notes) {
            if(n.getAbsent()) {
                continue;
            }

            if(t != null && n.getDevoir().getTrimestre().id() != t.id()) {
                continue;
            }

            Competence c = n.getDevoir().getCompetence();
            while(c.depth() > 0)  {
                c = c.getParent();
            }

            // on vérifie que la compétence n'est pas deja dans la liste
            boolean ok = true;
            for(Competence item : ret) {
                if(item.id() == c.id()) {
                    ok = false;
                    break;
                }
            }

            // si ce n'est pas le cas on ajoute la compétence
            if(ok) {
                ret.add(c);
            }
        }

        return ret;
    }

    /**
     * Renvoie la liste des sous-compétences d'une compétence pour lequel l'élève
     * a une note au moins.
     * @param c
     * @param t filtre de trimestre (peut valoir null)
     * @return
     */
    public List<Competence> getSousCompetencesNotees(Competence c, Trimestre t) {
        List<Competence> ret = c.getSousCompetences();
        for(int i = 0; i < ret.size(); i++)
        {
            Competence sc = ret.get(i);
            if(!estNote(sc, t)) {
                ret.remove(i);
                i--;
            }
        }
        return ret;
    }

    /**
     * Calcul le taux de réussite de l'élève pour une compétence donné.
     * <p>
     * L'élève doit etre noté sur cette compétence.
     * </p>
     * <p>
     * Le taux de réussite est défini de la manière suivante :
     * - pour une compétence mère : la moyenne des taux de réussite des sous-compétences
     * - pour une compétence terminal : la moyenne des notes de l'élève dans les devoirs de la coméptence
     * </p>
     * @param c
     * @param t filtre de trimestre (peut valoir null)
     * @return
     */
    public float calculTauxReussite(Competence c, Trimestre t) {
        if(!estNote(c, t)) {
            throw new IllegalArgumentException("Eleve.calculTauxReussite() ne peut travailler que sur une compétence notée");
        }

        List<Note> notes = t == null ? c.getNotes(this) : c.getNotes(this, t.id());
        if(notes.isEmpty()) {
            List<Competence> list = getSousCompetencesNotees(c, t);
            float total = 0.f;
            for(Competence sc : list)
            {
                total += calculTauxReussite(sc, t);
            }
            return total / list.size();
        }

        float total = 0.f;
        int absent = 0;
        for(Note n : notes) {
            if(n.getAbsent()) {
                absent += 1;
                continue;
            }
            total += n.scaledValue();
        }
        if(absent == notes.size()) {
            throw new IllegalArgumentException("Eleve.calculTauxReussite() : l'élève a été absent pour tous les devoirs de cette compétence");
        }
        return total / (notes.size()-absent);
    }

}
