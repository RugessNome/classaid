package classaid.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import classaid.database.Trimestre;

public class TrimestreActivity extends AppCompatActivity {

    private static SimpleDateFormat TrimestreDateFormat = new SimpleDateFormat("EEE d MMM yyyy");;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trimestre);
        setTitle("Trimestre");

        Trimestre trim1 = MainActivity.ClassaidDatabase.getTrimestre(1);
        setupBouton((Button) findViewById(R.id.trimestre1_debut), 1, true, trim1.getDateDebut());
        setupBouton((Button) findViewById(R.id.trimestre1_fin), 1, false, trim1.getDateFin());

        Trimestre trim2 = MainActivity.ClassaidDatabase.getTrimestre(2);
        setupBouton((Button) findViewById(R.id.trimestre2_debut), 2, true, trim2.getDateDebut());
        setupBouton((Button) findViewById(R.id.trimestre2_fin), 2, false, trim2.getDateFin());

        Trimestre trim3 = MainActivity.ClassaidDatabase.getTrimestre(3);
        setupBouton((Button) findViewById(R.id.trimestre3_debut), 3, true, trim3.getDateDebut());
        setupBouton((Button) findViewById(R.id.trimestre3_fin), 3, false, trim3.getDateFin());

    }


    protected void setupBouton(Button b, int numTrim, boolean debut, Calendar d) {
        if(d != null) {
            b.setText(TrimestreDateFormat.format(d.getTime()));
        }
        b.setTag(R.id.dateref, d);
        b.setTag(R.id.est_date_debut, new Boolean(debut));
        b.setTag(R.id.trimestre_id, new Integer(numTrim));
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog((Button) v);
            }
        });

    }


    protected void updateBouton(Button b, int year, int month, int day) {
        Calendar d = new GregorianCalendar(year, month, day);

        b.setText(TrimestreDateFormat.format(d.getTime()));

        if(b.getId() == R.id.trimestre1_debut) {
            Trimestre t = MainActivity.ClassaidDatabase.getTrimestre(1);
            t.setDateDebut(d);
        } else if(b.getId() == R.id.trimestre1_fin) {
            Trimestre t = MainActivity.ClassaidDatabase.getTrimestre(1);
            t.setDateFin(d);
        } else if(b.getId() == R.id.trimestre2_debut) {
            Trimestre t = MainActivity.ClassaidDatabase.getTrimestre(2);
            t.setDateDebut(d);
        } else if(b.getId() == R.id.trimestre2_fin) {
            Trimestre t = MainActivity.ClassaidDatabase.getTrimestre(2);
            t.setDateFin(d);
        } else if(b.getId() == R.id.trimestre3_debut) {
            Trimestre t = MainActivity.ClassaidDatabase.getTrimestre(3);
            t.setDateDebut(d);
        } else if(b.getId() == R.id.trimestre3_fin) {
            Trimestre t = MainActivity.ClassaidDatabase.getTrimestre(3);
            t.setDateFin(d);
        }
    }

    protected void showDatePickerDialog(Button b) {
        Calendar d = (Calendar) b.getTag(R.id.dateref);
        if(d == null) { d = Calendar.getInstance(); }

        final Button button = b;
        /*
        DialogFragment newFragment = new DatePickerFragment();
        if(d == null) { d = Calendar.getInstance(); }
        Bundle args = new Bundle();
        args.putInt("day", d.get(Calendar.DAY_OF_MONTH));
        args.putInt("month", d.get(Calendar.MONTH));
        args.putInt("year", d.get(Calendar.YEAR));
        args.putInt("trimestre", ((Integer) b.getTag(R.id.trimestre_id)).intValue());
        args.putBoolean("debut_trimestre", ((Boolean) b.getTag(R.id.est_date_debut)).booleanValue());
        newFragment.setArguments(args);
        newFragment.show(getSupportFragmentManager(), "datePicker");
        */
        DatePickerDialog picker = new DatePickerDialog(TrimestreActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                updateBouton(button, year, month, day);
            }
        }, d.get(Calendar.YEAR), d.get(Calendar.MONTH), d.get(Calendar.DAY_OF_MONTH));
        picker.show();
    }
}
