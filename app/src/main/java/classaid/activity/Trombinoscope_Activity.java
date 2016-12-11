package classaid.activity;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


import classaid.database.Eleve;

/**
 * Created by Julia on 25/10/2016.
 */
public class Trombinoscope_Activity extends Activity {

    private MyAdaptater listAdaptater;


    /**
     * @author Vincent
     */
    class MyAdaptater extends BaseAdapter {

        public List<Eleve> eleves;
        public Context context;

        public MyAdaptater(List<Eleve> elvs, Context con)
        {
            eleves = elvs;
            context = con;
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
                convertView = getLayoutInflater().inflate(R.layout.listview_eleves_item, container, false);
            }

            Eleve e = eleves.get(position);

            TextView title = (TextView) convertView.findViewById(R.id.title);
            title.setText(e.getPrenom() + " " + e.getNom());

            TextView description = (TextView) convertView.findViewById(R.id.description);
            description.setText(e.getDateNaissance().toString() + " - " + (e.getSexe() == 0 ? "M" : "F"));

            ImageView photo = (ImageView) convertView.findViewById(R.id.photo);
            Bitmap bitmap = e.getBitmapPhoto(context);
            if(bitmap == null) {
                photo.setImageResource(android.R.drawable.ic_menu_my_calendar);
            } else {
                photo.setImageBitmap(bitmap);
            }


            return convertView;
        }


    };


    /**
     * Met à jour la liste des élèves
     */
    public void updateListeEleves()
    {
        ListView list = (ListView) findViewById(R.id.listview_eleves);
        this.listAdaptater = new MyAdaptater(MainActivity.ClassaidDatabase.getEleves(), this.getApplicationContext());
        list.setAdapter(this.listAdaptater);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        updateListeEleves();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_trombi);

        ListView list = (ListView) findViewById(R.id.listview_eleves);
        list.setLongClickable(true);

        registerForContextMenu(list);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent myIntent = new Intent(Trombinoscope_Activity.this, Eleve_Activity.class);
                myIntent.putExtra("eleve_id", listAdaptater.eleves.get(position).id());
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

        Button creer_eleve = (Button) findViewById(R.id.bouton_creer_eleve);
        creer_eleve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(Trombinoscope_Activity.this, Eleve_Activity.class);
                startActivity(myIntent);
            }
        });

        updateListeEleves();
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
        menu.add("Modifier");
        menu.add("Supprimer");
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
        if(item.getTitle() == "Modifier") {
            Intent myIntent = new Intent(Trombinoscope_Activity.this, Eleve_Activity.class);
            myIntent.putExtra("eleve_id", this.listAdaptater.eleves.get(info.position).id());
            startActivity(myIntent);
        } else {
            // on supprime l'élève de la base
            MainActivity.ClassaidDatabase.removeEleve(this.listAdaptater.eleves.get(info.position));
            updateListeEleves();
        }
        return true;
    }

}
