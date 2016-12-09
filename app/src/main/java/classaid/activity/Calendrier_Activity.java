package classaid.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.FragmentManager;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.app.TimePickerDialog;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.Toast;
import android.app.Activity;
import android.widget.BaseAdapter;

import classaid.calendrier.Database;

/**
 * Created by Julia on 25/10/2016.
 */
public class Calendrier_Activity extends Activity {
    CalendarView calendar;
    /*private Trombinoscope_Activity.MyAdaptater listAdaptater; */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_calendrier);

        //initializes the calendarview
        initializeCalendar();

    }

    public void initializeCalendar(){
        calendar = (CalendarView) findViewById(R.id.calendar);

        // etablit le premier jour de la semaine, on choisit lundi
        calendar.setFirstDayOfWeek(2);


         calendar.setOnDateChangeListener(new OnDateChangeListener() {
             //affiche la phrase du jour en tant que toast
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int day) {
                //TODO ajouter "+ Database.getAnecdote()
                Toast.makeText(getApplicationContext(), "la phrase du jour  : " , Toast.LENGTH_LONG).show();
            }
        });





    }
}
