package classaid.activity;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.EditText;
import android.widget.Toast;


import classaid.calendrier.Database;

/**
 * Created by Julia on 25/10/2016.
 */
public class Calendrier_Activity extends Activity{
    CalendarView calendar;
    Database base;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calendrier);

        //initializes the calendarview
        initializeCalendar();

    }


    public void initializeCalendar() {
        base = Database.getDatabase(getApplicationContext());
        calendar = (CalendarView) findViewById(R.id.calendar);
        final EditText editText = (EditText) findViewById(R.id.editText);
        final Button button4 = (Button) findViewById(R.id.button4);

        //rend invisible les boutons et l'edit text
        button4.setVisibility(View.INVISIBLE);
        editText.setVisibility(View.INVISIBLE);

        // etablit le premier jour de la semaine, on choisit lundi
        calendar.setFirstDayOfWeek(2);


        calendar.setOnDateChangeListener(new OnDateChangeListener() {
            //affiche la phrase du jour en tant que toast

            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int day) {

                editText.setVisibility(View.VISIBLE);

                final Calendar date = new GregorianCalendar(year,month,day);
                String anecdote = base.getAnecdote(date);
                editText.setText(anecdote);

                button4.setVisibility(View.VISIBLE);
                button4.setOnClickListener (new View.OnClickListener(){

                    @Override
                    public void onClick (View v){
                        String anecdoteEdit = editText.getText().toString();
                        base.setAnecdote(date,anecdoteEdit);
                        Toast.makeText(getApplicationContext(), "Bien enregistré les gars!", Toast.LENGTH_LONG).show();

                    }
                });

                /*String date = day+"-"+month+1+"-"+year; */

                //Si jamais on ajoute une image à l'entité Anecdote
                /*Toast toast = new Toast(getApplicationContext());
                ImageView laView = new ImageView(getApplicationContext());
                laView.setImageResource(R.drawable.leNomDeLImageDuJour);
                toast.setView(laView);
                toast.show();*/


            }
        });


    }

}
