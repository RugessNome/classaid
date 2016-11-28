package classaid.activity;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
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


        MainActivity.ClassaidDatabase = Database.getDatabase(this.getApplicationContext(), 2016, false);

     }

    @Override
    public boolean onCreateOptionsMenu (Menu menu){
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        // SharedPreferences pref = getSharedPreferences("preferences", 0);
    }
}