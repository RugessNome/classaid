package classaid.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import classaid.database.Devoir;
import classaid.database.Eleve;
import classaid.database.Note;

public class Devoir_Activity extends Activity {

    private Devoir devoir;

    private SimpleAdaptater listAdaptater;

    private Calendar dateDevoir;

    private static SimpleDateFormat DateDevoirFormat = new SimpleDateFormat("EEE d MMM yyyy");;


    /**
     * Un adaptateur simple pour afficher la liste des élèves n'ayant pas de note
     * dans la base de données.
     * @author Vincent
     */
    class SimpleAdaptater extends BaseAdapter {

        public List<Eleve> eleves;

        public SimpleAdaptater(List<Eleve> elvs)
        {
            eleves = elvs;
        }

        @Override
        public int getCount() {
            return eleves.size();
        }

        @Override
        public Object getItem(int position) {
            return eleves.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position + 1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.listview_eleves_simpleitem, container, false);
            }

            Eleve e = eleves.get(position);

            TextView title = (TextView) convertView.findViewById(R.id.title);
            title.setText(e.getPrenom() + " " + e.getNom());

            return convertView;
        }

        @Override
        public boolean isEmpty()
        {
            return eleves.isEmpty();
        }

        public void remove(int i)
        {
            eleves.remove(i);
            notifyDataSetChanged();
        }

        public void add(Eleve e)
        {
            eleves.add(e);
            notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devoir_layout);


        Intent intent = getIntent();
        if(intent.hasExtra("devoir_id")) {
            Integer devoir_id = intent.getIntExtra("devoir_id", -1);
            if(devoir_id == -1) {
                throw new RuntimeException("Devoir_Activity needs a Devoir entity to work.");
            } else {
                devoir = MainActivity.ClassaidDatabase.getDevoir(devoir_id);
            }
        } else {
            throw new RuntimeException("Devoir_Activity needs a Devoir entity to work.");
        }

        Button date = (Button) findViewById(R.id.date_button);
        dateDevoir = new GregorianCalendar();
        dateDevoir.setTimeInMillis(devoir.getDate().getTime());
        date.setText(DateDevoirFormat.format(devoir.getDate()));

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        EditText commentaire = (EditText) findViewById(R.id.commentaire_textedit);
        commentaire.setText(devoir.getCommentaire());

        Spinner notation = (Spinner) findViewById(R.id.typenotation_spinner);
        notation.setSelection(devoir.getTypeNotation() - 1);

        TableLayout table = (TableLayout) findViewById(R.id.note_tablelayout);

        List<Note> notes = devoir.getNotes();
        for(Note n : notes)
        {
            createTableRow(n, table);
        }

        final TextView label_eleves_non_notes = (TextView) findViewById(R.id.list_eleves_non_notes_label);
        ListView eleves_listview = (ListView) findViewById(R.id.listview_eleves);
        listAdaptater = new SimpleAdaptater(devoir.getElevesNonNotes());
        eleves_listview.setAdapter(listAdaptater);
        eleves_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Eleve e = listAdaptater.eleves.get(position);
                addNote(e);
                listAdaptater.remove(position);
                if(listAdaptater.isEmpty()) {
                    label_eleves_non_notes.setVisibility(View.GONE);
                } else {
                    label_eleves_non_notes.setVisibility(View.VISIBLE);
                }
            }
        });
        if(listAdaptater.isEmpty()) {
            label_eleves_non_notes.setVisibility(View.GONE);
        }
    }

    /**
     * Ajoute une note pour l'élève donné
     * @param e
     */
    protected void addNote(Eleve e)
    {
        Note n = devoir.setNote(e, 0.f, "");
        createTableRow(n, (TableLayout) findViewById(R.id.note_tablelayout));
        final TextView label_eleves_non_notes = (TextView) findViewById(R.id.list_eleves_non_notes_label);
        label_eleves_non_notes.setVisibility(View.VISIBLE);
    }

    protected  void removeNote(TableRow row)
    {
        TableLayout table = (TableLayout) findViewById(R.id.note_tablelayout);
        table.removeView(row);
        Note n = (Note) row.getTag(R.id.noteref);
        listAdaptater.add(n.getEleve());
        final TextView label_eleves_non_notes = (TextView) findViewById(R.id.list_eleves_non_notes_label);
        if(listAdaptater.isEmpty()) {
            label_eleves_non_notes.setVisibility(View.GONE);
        }
        n.delete();
    }

    /**
     * Creer une ligne de tableur permettant de rentrer la note d'un élève
     * @param n
     * @param table
     * @return
     */
    protected TableRow createTableRow(Note n, TableLayout table)
    {
        TableRow row = (TableRow) getLayoutInflater().inflate(R.layout.notetable_item, null);
        row.setTag(R.id.noteref, n);

        TextView eleve = (TextView) row.findViewById(R.id.eleve_label);
        eleve.setText(n.getEleve().getPrenom() + " " + n.getEleve().getNom());

        EditText note = (EditText) row.findViewById(R.id.note_edittext);
        note.setTag(R.id.noteref, n);
        if(n.getAbsent())
        {
            note.setFocusable(false);
            note.setText("Absent");
        }
        else
        {
            note.setText("" + n.getValeur());
        }
        note.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                EditText self = (EditText) v;
                Note note = (Note) self.getTag(R.id.noteref);
                if(note.getAbsent())  {
                    note.setAbsent(false);
                    self.setFocusableInTouchMode(true);
                    self.setText(Float.toString(note.getValeur()));
                } else {
                    note.setAbsent(true);
                    self.setFocusable(false);
                    self.setText("Absent");
                }
                return true;
            }
        });

        EditText appreciation = (EditText) row.findViewById(R.id.appreciation_edittext);
        appreciation.setText(n.getCommentaire());

        ImageButton suppr = (ImageButton) row.findViewById(R.id.bouton_suppr);
        suppr.setTag(R.id.tablerowref, row);
        suppr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageButton that = (ImageButton) v;
                removeNote((TableRow) that.getTag(R.id.tablerowref));
            }
        });


        if(table.getChildCount() % 2 == 1)
        {
            row.setBackgroundColor(0xFF92C94A);
        }
        else
        {
            row.setBackgroundColor(0xFF6F9C33);
        }

        table.addView(row);


        return row;
    }

    /**
     * Sauvegarde dans la base de données les informations rentrées
     */
    @Override
    protected void onPause()
    {
        super.onPause();

        EditText commentaire = (EditText) findViewById(R.id.commentaire_textedit);
        devoir.setCommentaire(commentaire.getText().toString());

        devoir.setDate(new java.sql.Date(dateDevoir.getTimeInMillis()));

        Spinner notation = (Spinner) findViewById(R.id.typenotation_spinner);
        int typenotation = notation.getSelectedItemPosition() + 1;
        devoir.setTypeNotation(typenotation);

        TableLayout table = (TableLayout) findViewById(R.id.note_tablelayout);
        for(int i = 1; i < table.getChildCount(); i++)
        {
            TableRow row = (TableRow) table.getChildAt(i);
            Note n = (Note) row.getTag(R.id.noteref);
            EditText appreciation = (EditText) row.findViewById(R.id.appreciation_edittext);
            n.setCommentaire(appreciation.getText().toString());
            if(!n.getAbsent())
            {
                EditText valeur = (EditText) row.findViewById(R.id.note_edittext);
                n.setValeur(Float.valueOf(valeur.getText().toString()));
            }
        }
    }

    /**
     * Met à jour la date de naissance de l'élève
     * @param year
     * @param month
     * @param day
     */
    protected  void updateDateDevoir(int year, int month, int day)
    {
        dateDevoir = new GregorianCalendar(year, month, day);
        Button button = (Button) findViewById(R.id.date_button);
        button.setText(DateDevoirFormat.format(new Date(dateDevoir.getTimeInMillis())));
    }

    protected void showDatePickerDialog() {
        if(dateDevoir == null) { dateDevoir = Calendar.getInstance(); }

        DatePickerDialog picker = new DatePickerDialog(Devoir_Activity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                updateDateDevoir(year, month, day);
            }
        }, dateDevoir.get(Calendar.YEAR), dateDevoir.get(Calendar.MONTH), dateDevoir.get(Calendar.DAY_OF_MONTH));
        picker.show();
    }
}
