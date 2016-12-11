package classaid.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import classaid.Database;
import classaid.database.Eleve;
import classaid.database.Trimestre;
import classaid.pdf.GenerateurBulletin;

public class MainActivity extends AppCompatActivity {

    static public Database ClassaidDatabase = null;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vue2);
        setTitle("CLASSAID");

        ImageButton myImageT  = (ImageButton) findViewById(R.id.imageButtonT); //on veut que la view s'ouvre en appuyant sur une imagebouton
        myImageT.setOnClickListener (new View.OnClickListener(){ //Listener qui réagit au click

            @Override
            public void onClick (View v){
                Intent myIntent = new Intent(MainActivity.this, Trombinoscope_Activity.class);
                startActivity(myIntent);

            }
        });

        ImageButton myImageDC  = (ImageButton) findViewById(R.id.imageButtonDC);
        myImageDC.setOnClickListener (new View.OnClickListener(){

            @Override
            public void onClick (View v){
                Intent myIntent = new Intent(MainActivity.this,Devoirs_Competences_Activity.class);
                startActivity(myIntent);

            }
        });

        ImageButton myImageCE  = (ImageButton) findViewById(R.id.imageButtonCE);
        myImageCE.setOnClickListener (new View.OnClickListener(){

            @Override
            public void onClick (View v){
                Intent myIntent = new Intent(MainActivity.this,Calendrier_Activity.class);
                startActivity(myIntent);

            }
        });


        if(!checkAndSetAnneeScolaire()) {
            Toast.makeText(getApplicationContext(), "Vous n'avez pas spécifier d'année scolaire : l'année " + getAnneeScolaire() + " a été choisie par défaut.", Toast.LENGTH_LONG).show();
        }

        MainActivity.ClassaidDatabase = Database.getDatabase(this.getApplicationContext(), getAnneeScolaire(), false);
        //test
        Eleve bob = MainActivity.ClassaidDatabase.getEleve("Eponge", "Bob");
        if(bob == null) {
            bob = MainActivity.ClassaidDatabase.addEleve("Eponge", "Bob", java.sql.Date.valueOf("2016-12-01"), 0);
        }

        // test de la base de données calendrier
        classaid.calendrier.Database.test(this.getApplicationContext());

        if(!checkDatesTrimestre()) {
            Toast.makeText(getApplicationContext(), "Vous n'avez pas encore spécifier les dates de début et de fin de tous les trimestres.", Toast.LENGTH_LONG).show();
        }

     }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        getMenuInflater().inflate(R.menu.main_menu,menu);
        MenuItem settings = menu.findItem(R.id.action_settings);
        settings.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            }
        });

        MenuItem trimestre = menu.findItem(R.id.action_trimestre);
        trimestre.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(MainActivity.this, TrimestreActivity.class);
                startActivity(intent);
                return true;
            }
        });

        final MenuItem bulletin = menu.findItem(R.id.action_bulletin);
        bulletin.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                /*
                Eleve e = ClassaidDatabase.getEleve("Eponge", "Bob");
                if(e == null ) return true;
                Trimestre t = ClassaidDatabase.getTrimestre(1);
                if(t == null) return true;
                GenerateurBulletin b = new GenerateurBulletin(getApplicationContext(), e, t);
                b.generePdf("bob_eponge_1.pdf");
                */

                //showGenerateurBulletinPopup();

                return true;
            }
        });

        Menu submenu = bulletin.getSubMenu();
        MenuItem genTrim1 = submenu.findItem(R.id.trimestre1);
        genTrim1.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                genereBulletins(1);
                return true;
            }
        });
        MenuItem genTrim2 = submenu.findItem(R.id.trimestre2);
        genTrim2.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                genereBulletins(2);
                return true;
            }
        });
        MenuItem genTrim3 = submenu.findItem(R.id.trimestre3);
        genTrim3.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                genereBulletins(3);
                return true;
            }
        });


        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        System.out.println("Chemin logo : " + pref.getString("pref_logo_bulletin", "chemin non specifie"));

        int annee = -1;
        try {
            annee = getAnneeScolaire();
        } catch(RuntimeException e) {
            annee = getAnneeCourante();
            setAnneeScolaire(annee);
            Toast.makeText(getApplicationContext(), "Vous avez rentrer une année scolaire invalide. L'année " + annee + " a donc été choisie.", Toast.LENGTH_LONG).show();
        }

        if(MainActivity.ClassaidDatabase.getAnnee() == annee) {
            return;
        }

        MainActivity.ClassaidDatabase.close();
        MainActivity.ClassaidDatabase = Database.getDatabase(this.getApplicationContext(), annee, false);

    }


    public int getAnneeScolaire()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        int annee = Integer.parseInt(pref.getString("pref_annee_scolaire", "-1"));
        if(annee == -1) {
            throw new RuntimeException("Année scolaire invalide");
        }
        return annee;
    }

    public void setAnneeScolaire(int n) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("pref_annee_scolaire", "" + n);
        editor.commit();
    }

    static public int getAnneeCourante() {
        return (new GregorianCalendar()).get(Calendar.YEAR);
    }

    /**
     * Verifie que l'année scolaire qu'une année scolaire a été fournie et renvoie true si
     * c'est le cas
     * <p>
     * Si aucune année scolaire n'a été fournie, le système choisie l'année courante
     * comme année scolaire.
     * </p>
     * @return
     */
    private boolean checkAndSetAnneeScolaire()
    {
        int annee = -1;
        try {
            annee = getAnneeScolaire();
        } catch(RuntimeException e) {
            setAnneeScolaire(getAnneeCourante());
            return false;
        }
        return true;
    }

    /**
     * Vérifie que des dates de debut et de fin ont été fournies pour les trimestre,
     * si ce n'est pas le cas, renvoie false.
     */
    private boolean checkDatesTrimestre()
    {
        for(int i = 1; i <= 3; ++i) {
            Trimestre t = MainActivity.ClassaidDatabase.getTrimestre(i);
            if(t.getDateDebut() == null || t.getDateFin() == null)
            {
                return false;
            }
        }

        return true;
    }


    /**
     * Génère les bulletins pour le trimestre donné
     * @param numTrim
     */
    private void genereBulletins(int numTrim)
    {
        Trimestre trimestre = MainActivity.ClassaidDatabase.getTrimestre(numTrim);

        if(trimestre.getDateDebut() == null || trimestre.getDateFin() == null) {
            Toast.makeText(getApplicationContext(), "Vous devez rentrer spécifier les dates de début et de fin du trimestre avant de pouvoir générer des bulletins.", Toast.LENGTH_LONG).show();
            return;
        }

        List<Eleve> eleves = MainActivity.ClassaidDatabase.getEleves();

        for(Eleve e : eleves)
        {
            GenerateurBulletin gen = new GenerateurBulletin(this.getApplicationContext(), e, trimestre);
            gen.generePdf(e.getNom() + "_" + e.getPrenom() + "_" + numTrim + ".pdf");
        }

        Toast.makeText(getApplicationContext(), "Génération des bulletins terminé !", Toast.LENGTH_LONG).show();

    }
}