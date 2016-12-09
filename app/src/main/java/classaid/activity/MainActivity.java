package classaid.activity;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

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


        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        System.out.println("pref_annee_scolaire : " + Integer.parseInt(pref.getString("pref_annee_scolaire", "-1")));


        MainActivity.ClassaidDatabase = Database.getDatabase(this.getApplicationContext(), 2016, false);


        // test de la base de données calendrier
        classaid.calendrier.Database.test(this.getApplicationContext());

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

        MenuItem bulletin = menu.findItem(R.id.action_bulletin);
        bulletin.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Eleve e = ClassaidDatabase.getEleve("Eponge", "Bob");
                if(e == null ) return true;
                Trimestre t = ClassaidDatabase.getTrimestre(1);
                if(t == null) return true;
                GenerateurBulletin b = new GenerateurBulletin(getApplicationContext(), e, t);
                b.generePdf("bob_eponge_1.pdf");
                return true;
            }
        });


        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        System.out.println("pref_annee_scolaire : " + Integer.parseInt(pref.getString("pref_annee_scolaire", "-1")));

    }
}