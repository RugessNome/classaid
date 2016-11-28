package classaid.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.sax.TextElementListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import classaid.database.Devoir;
import classaid.database.Eleve;
import classaid.database.Note;
import classaid.database.TypeNotation;

public class Devoir_Activity extends Activity {

    private Devoir devoir;

    private SimpleAdaptater listAdaptater;


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

        public void remove(int i)
        {
            eleves.remove(i);
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

        EditText date = (EditText) findViewById(R.id.date_textedit);
        date.setText(devoir.getDate().toString());

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

        // le spinner sert à selectionner une élève pour lui ajouter une note
        ListView eleves_listview = (ListView) findViewById(R.id.listview_eleves);
        if(devoir == null)
        {
            listAdaptater = new SimpleAdaptater(MainActivity.ClassaidDatabase.getEleves());
        }
        else // devoir != null
        {
            listAdaptater = new SimpleAdaptater(devoir.getElevesNonNotes());
        }
        eleves_listview.setAdapter(listAdaptater);
        eleves_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Eleve e = listAdaptater.eleves.get(position);
                addNote(e);
                listAdaptater.remove(position);
            }
        });
    }

    /**
     * Ajoute une note pour l'élève donné
     * @param e
     */
    protected void addNote(Eleve e)
    {
        Note n = devoir.setNote(e, 0.f, "");
        createTableRow(n, (TableLayout) findViewById(R.id.note_tablelayout));
    }

    protected  void removeNote(TableRow row)
    {
        TableLayout table = (TableLayout) findViewById(R.id.note_tablelayout);
        table.removeView(row);
        Note n = (Note) row.getTag(R.id.noteref);
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
            row.setBackgroundColor(0x92C94A);
        }
        else
        {
            row.setBackgroundColor(0x6F9C33);
        }

        table.addView(row);


        return row;
    }

    /**
     * Sauvegarde dans la base de données les informations rentrées
     */
    @Override
    protected void onStop()
    {
        super.onStop();

        EditText commentaire = (EditText) findViewById(R.id.commentaire_textedit);
        devoir.setCommentaire(commentaire.getText().toString());

        EditText date = (EditText) findViewById(R.id.date_textedit);
        devoir.setDate(java.sql.Date.valueOf(date.getText().toString()));

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
}
