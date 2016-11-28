package classaid.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import classaid.database.Competence;
import classaid.database.Devoir;
import classaid.database.Eleve;
import classaid.database.Note;
import classaid.database.TypeNotation;

/**
 * Created by Julia on 25/10/2016.
 */
public class Devoirs_Competences_Activity extends Activity {

    private MyAdaptater listAdaptater;
    private Competence competenceParent;

    /**
     * @author Vincent
     */
    class MyAdaptater extends BaseAdapter {

        public List<Competence> competences;
        public List<Devoir> devoirs;

        public MyAdaptater(List<Competence> comps, List<Devoir> devs)
        {
            competences = comps;
            devoirs = devs;
        }

        public void setItems(List<Competence> comps, List<Devoir> devs)
        {
            competences = comps;
            devoirs = devs;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return competences.size() + devoirs.size();
        }

        @Override
        public Object getItem(int position) {
            if(position < competences.size())
            {
                return competences.get(position);
            }
            return devoirs.get(position - competences.size());
        }

        @Override
        public long getItemId(int position) {
            return position + 1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.listview_devoir_competence_item, container, false);
            }

            if(position < competences.size())
            {
                Competence c = competences.get(position);

                ImageView icon = (ImageView) convertView.findViewById(R.id.item_icon);
                icon.setImageDrawable(getResources().getDrawable(R.drawable.competence_icon));

                TextView title = (TextView) convertView.findViewById(R.id.item_title);
                title.setText(c.getNom());

                TextView description = (TextView) convertView.findViewById(R.id.item_description);

                int subcomp_count = c.getSousCompetences().size();
                int devoir_count = c.getDevoirs().size();
                String des = "";
                if(subcomp_count > 0) {
                    des += subcomp_count + " sous-compétences";
                }
                if(devoir_count > 0)
                {
                    if(des.length() > 0) {
                        des += " | ";
                    }
                    des += devoir_count + " devoir(s)";
                }
                description.setText(des);

            }
            else
            {
                position -= competences.size();
                Devoir d = devoirs.get(position);

                ImageView icon = (ImageView) convertView.findViewById(R.id.item_icon);
                icon.setImageDrawable(getResources().getDrawable(R.drawable.devoir_icon));

                TextView title = (TextView) convertView.findViewById(R.id.item_title);
                String notation = (d.getTypeNotation() == TypeNotation.NotationSur10 ? "Note sur 10" : "Note sur 20");
                title.setText(d.getDate().toString() + " " + notation);

                TextView description = (TextView) convertView.findViewById(R.id.item_description);
                List<Note> notes = d.getNotes();
                String des = "";
                int nbr_absent = Devoir.nbrAbsent(notes);
                if(notes.size() > 0 && nbr_absent < notes.size()) {
                    des += Devoir.moyenne(notes);
                }
                if(nbr_absent > 0) {
                    des += " - " + nbr_absent + " absent(s)";
                }
                String com = d.getCommentaire();
                if(com != null && com.length() > 0) {
                    des += " - " + com;
                }
                description.setText(des);
            }

            return convertView;
        }
    };


    /**
     * Met à jour la liste des élèves
     */
    public void updateListDevoirsCompetences()
    {
        ListView list = (ListView) findViewById(R.id.listview_devoirs_competences);
        List<Competence> comps = null;
        if(this.competenceParent == null) {
            comps = MainActivity.ClassaidDatabase.getCompetences();
        } else {
            comps = MainActivity.ClassaidDatabase.getSousCompetences(this.competenceParent);
        }
        List<Devoir> devs = null;
        if(this.competenceParent != null) {
            devs = this.competenceParent.getDevoirs();
        } else {
            devs = new ArrayList<Devoir>();
        }
        this.listAdaptater.setItems(comps, devs);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        updateListDevoirsCompetences();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_devoirs_competences);

        Intent intent = getIntent();
        if(intent.hasExtra("competence_id")) {
            Integer comp_id = intent.getIntExtra("competence_id", -1);
            if(comp_id != -1) {
                competenceParent = MainActivity.ClassaidDatabase.getCompetence(comp_id);
            } else {
                competenceParent = null;
            }
        } else {
            competenceParent = null;
        }


        final ListView list = (ListView) findViewById(R.id.listview_devoirs_competences);
        list.setLongClickable(true);

        List<Competence> competences = null;
        List<Devoir> devoirs = null;
        if(competenceParent == null) {
            competences = MainActivity.ClassaidDatabase.getCompetences();
            devoirs= new ArrayList<Devoir>();
        } else {
            competences = competenceParent.getSousCompetences();
            devoirs = competenceParent.getDevoirs();
        }
        listAdaptater = new MyAdaptater(competences, devoirs);

        list.setAdapter(listAdaptater);

        registerForContextMenu(list);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position < listAdaptater.competences.size()) {
                    Intent myIntent = new Intent(Devoirs_Competences_Activity.this, Devoirs_Competences_Activity.class);
                    myIntent.putExtra("competence_id", listAdaptater.competences.get(position).id());
                    startActivity(myIntent);
                    return;
                }
                position -= listAdaptater.competences.size();
                Intent myIntent = new Intent(Devoirs_Competences_Activity.this, Devoir_Activity.class);
                myIntent.putExtra("devoir_id", listAdaptater.devoirs.get(position).id());
                startActivity(myIntent);
            }
        });

        /*
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });
        */

        Button creer_competence = (Button) findViewById(R.id.bouton_creer_competence);
        creer_competence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGetCompetenceNameDialog("Nouvelle Compétence", null);
            }
        });

        Button creer_devoir = (Button) findViewById(R.id.bouton_creer_devoir);
        if(competenceParent == null) {
            creer_devoir.setEnabled(false);
        }
        creer_devoir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(competenceParent == null) {
                    return;
                }
                java.text.DateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
                String current_date = df.format(new java.util.Date());
                Devoir d = competenceParent.addDevoir(java.sql.Date.valueOf(current_date), TypeNotation.NotationSur20,false);
                Intent myIntent = new Intent(Devoirs_Competences_Activity.this, Devoir_Activity.class);
                myIntent.putExtra("devoir_id", d.id());
                startActivity(myIntent);
            }
        });

    }

    protected void creerCompetence(String nom)
    {
        if(this.competenceParent == null) {
            MainActivity.ClassaidDatabase.addCompetence(nom);
        } else {
            this.competenceParent.addCompetence(nom);
        }

        updateListDevoirsCompetences();
    }

    /**
     * Affiche un Dialog permettant de spécifier un nom de compétence.
     * @return
     */
    protected void showGetCompetenceNameDialog(String title, Competence comp)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        final EditText input = new EditText(this);
        input.setId(R.id.alertdialog_text_input);
        input.setTag(R.id.alertdialog_text_input_user_data, comp);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        if(comp != null) {
            input.setText(comp.getNom());
        }
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Competence comp = (Competence) input.getTag(R.id.alertdialog_text_input_user_data);
                if(comp == null) {
                    creerCompetence(input.getText().toString());
                } else {
                    comp.setNom(input.getText().toString());
                    updateListDevoirsCompetences();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    /**
     * Créer le menu contextuel floatant lors d'un clic long sur un élève
     * @author Vincent
     * @param v
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        if(info.position < listAdaptater.competences.size())
        {
            menu.add("Renommer");
            menu.add("Supprimer");
        }
        else
        {
            menu.add("Supprimer");
        }
    }

    /**
     * Gère le clic sur une action du menu contextuel crée par onCreateContextMenu().
     * @author Vincent
     * @param item l'item cliqué
     * @return true (valeur indiquant que le clic a été consommé et ne doit pas être traité par une autre fonction)
     */
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        super.onContextItemSelected(item);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if(info.position < listAdaptater.competences.size())
        {
            Competence comp = listAdaptater.competences.get(info.position);
            if(item.getTitle() == "Renommer") {
                showGetCompetenceNameDialog("Renommer une compétence", comp);
            } else {
                // bouton supprimer
                comp.delete();
            }
        }
        else
        {
            Devoir dev = listAdaptater.devoirs.get(info.position - listAdaptater.competences.size());
            // on supprime le devoir
            dev.delete();
        }

        updateListDevoirsCompetences();

        return true;
    }
}
