package classaid.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import classaid.database.DonneeSupplementaire;
import classaid.database.Eleve;


public class Eleve_Activity extends Activity {

    /**
     * L'id de l'élève sur lequel travail l'activité.
     * Peut valoir null si le but de l'activité est de créer un élève.
     */
    private Integer eleveId;

    /**
     * La date de naissance de l'élève stocké dans un Calendar
     */
    private Calendar dateNaissance;

    private String photoPath = "";

    static private SimpleDateFormat DateNaissanceFormat = new SimpleDateFormat("dd-MM-yyyy");

    static private int TakePhotoRequestCode = 42;
    static private int OpenPhotoRequestCode = 666;

    /**
     * Classe représentant une donnée supplémentaire qui n'existe pas forcément dans
     * la bsae de données.
     */
    class DonneeStruct
    {
        /**
         * L'id de la donnée supplémentaire dans la table correspondante ou -1
         * si la donnée supplémentaire ne correspond à aucune entité de la base.
         */
        public int id;
        /**
         * La vue utilisée pour afficher/modifier cette donnée supplémentaire
         */
        public View view;

        public DonneeStruct(int i)
        {
            id = i;

            view = getLayoutInflater().inflate(R.layout.donnee_supplementaire_view, null, false);
            if(id != -1) {
                DonneeSupplementaire dsup = MainActivity.ClassaidDatabase.getDonneeSupplementaire(id);

                EditText nom = (EditText) view.findViewById(R.id.nom);
                nom.setText(dsup.getNom());

                EditText valeur = (EditText) view.findViewById(R.id.valeur);
                valeur.setText(dsup.getValeur());
            }
        }

        public ImageButton getBoutonSuppr() {
            return (ImageButton) view.findViewById(R.id.bouton_supprimer);
        }

        public String getNom() {
            return ((EditText) view.findViewById(R.id.nom)).getText().toString();
        }

        public String getValeur() {
            return ((EditText) view.findViewById(R.id.valeur)).getText().toString();
        }
    }


    private List<DonneeStruct> donneesSupplementaires;
    /**
     * Liste des données supplémentaires à supprimer lors de l'appuie sur Ok
     */
    private List<DonneeStruct> donneeSupplementairesSuppr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eleve_layout);

        donneesSupplementaires = new ArrayList<DonneeStruct>();
        donneeSupplementairesSuppr = new ArrayList<DonneeStruct>();

        Intent intent = getIntent();
        if(intent.hasExtra("eleve_id")) {
            eleveId = intent.getIntExtra("eleve_id", -1);
            if(eleveId == -1) {
                // erreur : que peut-on faire ?
                eleveId = null;
            }
        } else {
            eleveId = null;
        }

        LinearLayout donnees_sup_layout = (LinearLayout) findViewById(R.id.layout_donnees_supplementaires);

        ImageView photo = (ImageView) findViewById(R.id.photo);
        photo.setLongClickable(true);
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
        photo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                openPhoto();
                return true;
            }
        });

        if(eleveId != null) {
            Eleve e = MainActivity.ClassaidDatabase.getEleve(eleveId.intValue());

            EditText nom = (EditText) findViewById(R.id.nom);
            nom.setText(e.getNom());
            EditText prenom = (EditText) findViewById(R.id.prenom);
            prenom.setText(e.getPrenom());
            Button naissance = (Button) findViewById(R.id.date_naissance);
            dateNaissance = new GregorianCalendar();
            dateNaissance.setTimeInMillis(e.getDateNaissance().getTime());
            naissance.setText(DateNaissanceFormat.format(e.getDateNaissance()));
            Spinner sexe = (Spinner) findViewById(R.id.sexe);
            sexe.setSelection(e.getSexe() == 0 ? 0 : 1);
            photoPath = e.getPhoto();

            Bitmap bitmap = e.getBitmapPhoto(this.getApplicationContext());
            if(bitmap == null) {
                photo.setImageResource(R.mipmap.ic_launcher);
            } else {
                photo.setImageBitmap(bitmap);
            }


            for(DonneeSupplementaire d : e.getDonneesSupplementaires()) {
                DonneeStruct dsup = new DonneeStruct(d.id());
                dsup.getBoutonSuppr().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        supprDonneeSup((ImageButton) v);
                    }
                });
                donneesSupplementaires.add(dsup);
                donnees_sup_layout.addView(dsup.view);
            }
        }

        Button addDonSup = (Button) findViewById(R.id.bouton_creer_donnees_personnelles);
        addDonSup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ajouterDonneeSupp();
            }
        });

        Button ok = (Button) findViewById(R.id.bouton_ok);
        ok.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View v){
                onOk();
            }
        });

        Button annuler = (Button) findViewById(R.id.bouton_annuler);
        annuler.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick (View v){
                // met fin à l'activité Eleve_Activity
                finish();
            }
        });

        Button naissance = (Button) findViewById(R.id.date_naissance);
        naissance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

    }

    /**
     * Sauvegarde les modifications apportés et termine l'activité
     */
    public void onOk()
    {
        // récupération des informations sur l'élève
        String nom = ((EditText) findViewById(R.id.nom)).getText().toString();
        String prenom = ((EditText) findViewById(R.id.prenom)).getText().toString();
        int sexe = ((Spinner) findViewById(R.id.sexe)).getSelectedItemPosition() == 0 ? 0 : 1;

        if(eleveId == null) {
            // création de l'élève
            Eleve eleve = MainActivity.ClassaidDatabase.addEleve(nom, prenom, new java.sql.Date(dateNaissance.getTimeInMillis()), sexe);
            if(eleve == null) {
                // erreur : que faire ?
                return;
            }
            eleve.setPhoto(photoPath);

            // ajout des données supplémentaires
            for(DonneeStruct dsup : donneesSupplementaires) {
                eleve.addDonneeSupplementaire(dsup.getNom(), dsup.getValeur());
            }

        } else {
            // mise à jour de l'élève
            Eleve e = MainActivity.ClassaidDatabase.getEleve(eleveId.intValue());
            if(e == null) {
                // erreur : que faire ?
                return;
            }
            e.setNom(nom);
            e.setPrenom(prenom);
            e.setDateNaissance(new java.sql.Date(dateNaissance.getTimeInMillis()));
            e.setSexe(sexe);
            e.setPhoto(photoPath);

            // ajout ou maj des données supplémentaires
            for(DonneeStruct dsup : donneesSupplementaires) {
                if(dsup.id != -1) {
                    DonneeSupplementaire dsup_entite = MainActivity.ClassaidDatabase.getDonneeSupplementaire(dsup.id);
                    dsup_entite.setNom(dsup.getNom());
                    dsup_entite.setValeur(dsup.getValeur());
                } else {
                    e.addDonneeSupplementaire(dsup.getNom(), dsup.getValeur());
                }
            }

            // suppresion de certaines données supplémentaires
            for(DonneeStruct dsup : donneeSupplementairesSuppr) {
                if(dsup.id == -1) {
                    // ce cas ne devrait jamais arriver
                    continue;
                }
                MainActivity.ClassaidDatabase.getDonneeSupplementaire(dsup.id).delete();
            }

        }

        // met fin à l'activité Eleve_Activity
        finish();
    }

    /**
     * Ajoute une donnée supplémentaire à l'élève.
     * L'ajout ne sera effectif que si l'utilisateur clique sur le bouton Ok
     */
    public void ajouterDonneeSupp() {
        DonneeStruct don = new DonneeStruct(-1);
        this.donneesSupplementaires.add(don);
        LinearLayout donnees_sup_layout = (LinearLayout) findViewById(R.id.layout_donnees_supplementaires);
        donnees_sup_layout.addView(don.view);
    }

    /**
     * Permet de supprimer la donnée suplémentaire associée au bouton de suppresion
     * passé en entré.
     */
    public void supprDonneeSup(ImageButton supprBouton) {
        DonneeStruct dsup = null;
        for(DonneeStruct it : donneesSupplementaires) {
            if(it.getBoutonSuppr() != supprBouton) { continue; }
            dsup = it;
            break;
        }
        if(dsup == null) {
            // erreur : que faire ?
        }
        donneesSupplementaires.remove(dsup);
        if(dsup.id != -1) {
            donneeSupplementairesSuppr.add(dsup);
        }

        LinearLayout donnees_sup_layout = (LinearLayout) findViewById(R.id.layout_donnees_supplementaires);
        donnees_sup_layout.removeView(dsup.view);
    }

    /**
     * Met à jour la date de naissance de l'élève
     * @param year
     * @param month
     * @param day
     */
    protected  void updateDateNaissance(int year, int month, int day)
    {
        dateNaissance = new GregorianCalendar(year, month, day);
        Button button = (Button) findViewById(R.id.date_naissance);
        button.setText(DateNaissanceFormat.format(new Date(dateNaissance.getTimeInMillis())));
    }

    protected void showDatePickerDialog() {
        if(dateNaissance == null) { dateNaissance = Calendar.getInstance(); }


        DatePickerDialog picker = new DatePickerDialog(Eleve_Activity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                updateDateNaissance(year, month, day);
            }
        }, dateNaissance.get(Calendar.YEAR), dateNaissance.get(Calendar.MONTH), dateNaissance.get(Calendar.DAY_OF_MONTH));
        picker.show();
    }

    /**
     * Permet la prise d'une photo
     */
    protected void takePhoto()
    {
        // on check si on a une caméra
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            openPhoto();
            return;
        }

        Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePic.resolveActivity(getPackageManager()) == null) {
            openPhoto();
            return;
        }

        File photoFile = null;
        try {
            photoFile = getUniqueSaveFile();
            if(!photoFile.createNewFile()) {
                Toast.makeText(this.getApplicationContext(), "Erreur lors de la création du fichier de sauvegarde de la photo", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (IOException ex) {
            Toast.makeText(this.getApplicationContext(), "Erreur lors de la création du fichier de sauvegarde de la photo", Toast.LENGTH_LONG).show();
            return;
        }

        if (photoFile != null) {
            photoPath = photoFile.getAbsolutePath();

            Uri photoURI = Uri.fromFile(photoFile);
            takePic.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePic, TakePhotoRequestCode);
        }

    }

    /**
     * Demande à l'utilisateur de choisir une photo parmi ses ficheirs
     */
    protected void openPhoto()
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("file/*");
        startActivityForResult(intent, OpenPhotoRequestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(resultCode, resultCode, data);

        if(resultCode == Activity.RESULT_CANCELED) {
            if(requestCode == TakePhotoRequestCode) {
                File photoFile = new File(photoPath);
                photoFile.delete();
                photoPath = "";
            }
            return;
        }


        if(requestCode == TakePhotoRequestCode)
        {
            if(resultCode != Activity.RESULT_OK) {
                File photoFile = new File(photoPath);
                photoFile.delete();
                photoPath = "";
                Toast.makeText(this.getApplicationContext(), "Erreur lors de la prise de la photo", Toast.LENGTH_LONG).show();
                return;
            }
        }
        else if(requestCode == OpenPhotoRequestCode)
        {
            if(resultCode != Activity.RESULT_OK) {
                Toast.makeText(this.getApplicationContext(), "Erreur lors de l'ouverture de la photo", Toast.LENGTH_LONG).show();
                return;
            }

            photoPath = data.getData().getPath();
        }

        ImageView photo = (ImageView) findViewById(R.id.photo);
        Bitmap bitmap = loadPicture();
        if(bitmap == null) {
            photo.setImageResource(R.mipmap.ic_launcher);
        } else {
            photo.setImageBitmap(bitmap);
        }

    }

    /**
     * Renvoie un nom de fichier unique pour la sauvegarde de la photo
     * @return
     */
    private File getUniqueSaveFile()
    {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        String imageFileName = timeStamp;
        if(eleveId != null)
        {
            Eleve e = MainActivity.ClassaidDatabase.getEleve(eleveId.intValue());
            imageFileName = e.getPrenom() + "_" + e.getNom() + "_" + imageFileName;
        }
        else
        {
            String nom = ((EditText) findViewById(R.id.nom)).getText().toString();
            String prenom = ((EditText) findViewById(R.id.prenom)).getText().toString();
            imageFileName = prenom + "_" + nom + "_" + imageFileName;
        }
        imageFileName = "JPEG_" + imageFileName;
        File image = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), imageFileName + ".jpg");
        return image;
    }

    /**
     * Charge la photo qui vient d'être prise
     * @return
     */
    private Bitmap loadPicture()
    {
        try {
            File f = new File(photoPath);
            if(!f.exists()) {
                return null;
            }
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            return b;
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}
