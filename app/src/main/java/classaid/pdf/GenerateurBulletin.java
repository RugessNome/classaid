package classaid.pdf;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.print.PrintAttributes;
import android.print.pdf.PrintedPdfDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import classaid.activity.MainActivity;
import classaid.activity.R;
import classaid.database.Competence;
import classaid.database.Eleve;
import classaid.database.Trimestre;

/**
 * Classe permettant la génération d'un bulletin scolaire.
 * <p>
 * Note: dimension page A4 : 21x29.7cm <-> 8.267x11.692 inch <br/>
 * 1 inch = 2.54 cm
 *
 * </p>
 * Created by Vincent on 04/12/2016.
 */

@TargetApi(Build.VERSION_CODES.KITKAT)
public class GenerateurBulletin {
    private Eleve eleve;
    private Trimestre trimestre;
    private PrintedPdfDocument pdf;
    private Context context;

    final static public int DPI = 300;
    /**
     * Marges (en cm)
     */
    final static public float TopMargin = 2.0f;
    final static public float LeftMargin = 2.0f;
    final static public float RightMargin = 2.0f;
    final static public float BottomMargin = 2.0f;

    final static public int ColorBlack = 0xFF000000;

    /**
     * Marge à droite pour le texte du taux de réussite (en pixel)
     */
    final static public int TauxReussiteMargin = 100;

    /**
     * Marge à droite pour l'icone indiquant le progrès sur la compétence
     */
    final static public int IndicateurProgresMargin = 30;
    final static public int IndicateurProgresSize = 16;

    /**
     * L'interligne en pixel
     */
    final static public int Interligne = 6;

    final public  static float IndentCompetence1 = 10.0f;
    final public  static float IndentCompetence2 = 20.0f;
    final public  static float IndentCompetence3 = 30.0f;

    final public static float BottomMarginCompetence0 = 16.0f;
    final public static float BottomMarginCompetence1 = 10.0f;
    final public static float BottomMarginCompetence2 = 10.0f;
    final public static float BottomMarginCompetence3 = 10.0f;

    final static public String Police = "Arial";

    static public Paint PinceauHeader0() {
        Paint p = new Paint();
        p.setTextSize(14);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setColor(ColorBlack);
        p.setTypeface(Typeface.create(Police, Typeface.NORMAL));
        return p;
    }

    static public Paint PinceauHeader1() {
        Paint p = new Paint();
        p.setTextSize(12);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setColor(ColorBlack);
        p.setTypeface(Typeface.create(Police, Typeface.NORMAL));
        return p;
    }

    /**
     * Renvoie le pinceau a utiliser pour écrire une compétence de
     * pronfondeur 0 (compétence mère)
     * @return
     */
    static public Paint PinceauCompetence0() {
        Paint p = new Paint();
        p.setTextSize(14);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setColor(ColorBlack);
        p.setTypeface(Typeface.create(Police, Typeface.BOLD));
        return p;
    }

    /**
     * Renvoie le pinceau a utiliser pour écrire une compétence de
     * pronfondeur 1 (sous-compétence)
     * @return
     */
    static public Paint PinceauCompetence1() {
        Paint p = new Paint();
        p.setTextSize(12);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setColor(ColorBlack);
        p.setTypeface(Typeface.create(Police, Typeface.BOLD));
        return p;
    }

    /**
     * Renvoie le pinceau a utiliser pour écrire une compétence de
     * pronfondeur 2 (sous-sous-compétence)
     * @return
     */
    static public Paint PinceauCompetence2() {
        Paint p = new Paint();
        p.setTextSize(9);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setTypeface(Typeface.create(Police, Typeface.BOLD));
        p.setColor(ColorBlack);
        return p;
    }

    /**
     * Renvoie le pinceau a utiliser pour écrire une compétence de
     * pronfondeur 3 (sous-sous-compétence)
     * @return
     */
    static public Paint PinceauCompetence3() {
        Paint p = new Paint();
        p.setTextSize(8);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setTypeface(Typeface.create(Police, Typeface.NORMAL));
        p.setColor(ColorBlack);
        return p;
    }

    static public Paint PinceauFooter() {
        Paint p = new Paint();
        p.setTextSize(12);
        p.setStyle(Paint.Style.FILL_AND_STROKE);
        p.setTypeface(Typeface.create(Police, Typeface.NORMAL));
        p.setColor(ColorBlack);
        return p;
    }

    static public Paint PinceauRect1()
    {
        Paint p = new Paint();
        p.setStyle(Paint.Style.STROKE);
        p.setColor(ColorBlack);
        p.setStrokeWidth(1.15f);
        return p;
    }

    static public Paint PinceauLine1()
    {
        Paint p = new Paint();
        p.setStyle(Paint.Style.STROKE);
        p.setColor(ColorBlack);
        p.setStrokeWidth(1.15f);
        return p;
    }

    static public Paint PinceauLine2()
    {
        Paint p = new Paint();
        p.setStyle(Paint.Style.STROKE);
        p.setColor(ColorBlack);
        p.setStrokeWidth(1.10f);
        return p;
    }

    public String getCycleString()
    {
        return "Cycle 2";
    }

    public String getCoursString()
    {
        return "CP - Cours préparatoire";
    }




    /**
     * La page en cours d'impression
     */
    private PdfDocument.Page page;
    private Canvas canvas;
    /**
     * Le nombre de page du bulletin
     */
    private int pagecount;
    /**
     * L'offset d'écriture sur la page courante
     */
    private int offset;
    /**
     * La taille du bas de page.
     */
    private int footerheight;



    public GenerateurBulletin(Context con, Eleve e, Trimestre t)
    {
        eleve = e;
        trimestre = t;
        context = con;

        PrintAttributes.Builder builder = new PrintAttributes.Builder();
        builder.setResolution(new PrintAttributes.Resolution("bulletin", "bulletin", DPI, DPI));
        builder.setMinMargins(new PrintAttributes.Margins(Math.round(cmToInch(LeftMargin) * 1000),
                        Math.round(cmToInch(TopMargin) * 1000),
                        Math.round(cmToInch(RightMargin) * 1000),
                        Math.round(cmToInch(BottomMargin) * 1000)));
        builder.setMediaSize(PrintAttributes.MediaSize.ISO_A4);
        builder.setColorMode(PrintAttributes.COLOR_MODE_COLOR);
        PrintAttributes pattr = builder.build();

        pdf = new PrintedPdfDocument(con, pattr);
    }

    public Bitmap getLogoBulletin()
    {
        try {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            File f = new File(pref.getString("pref_logo_bulletin", ""));
            if(!f.exists()) {
                return null;
            }
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
            return b;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    /**
     * Génère et enregistre le bulletin au format PDF dans un fichier.
     */
    public void generePdf(String filename)
    {
        pagecount = 0;
        startNewPage();

        /// test
        //testCanvasDrawText();
        /// test

        System.out.println("GenerateurBulletin.generePdf() : canvas.getHeight() = " + canvas.getHeight());
        System.out.println(page.getInfo().getContentRect().toString());

        printHeaders();

        List<Competence> list_comp = eleve.getCompetencesNotees(trimestre);
        for(Competence c : list_comp)
        {
            printCompetence(c);
        }

        if(page != null) {
            pdf.finishPage(page);
        }

        FileOutputStream output = null;
        try {
            //File file = new File(context.getFilesDir(), filename);
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
            if(!file.exists()) {
                file.createNewFile();
            }
            output = new FileOutputStream(file);
        } catch (Exception e) {
            pdf.close();
            System.out.println("GenerateurBulletin.generePdf() : erreur lors de l'ouverture du fichier de destination (" + e.getClass().getName() + " : " + e.getMessage() + ")");
            return;
        }

        try {
            pdf.writeTo(output);
        } catch(Exception e) {
            System.out.println("GenerateurBulletin.generePdf() : erreur lors de l'écriture du pdf (" + e.getClass().getName() + " : " + e.getMessage() + ")");
        }

        pdf.close();
        try {
            output.close();
        } catch (Exception e) {
            System.out.println("GenerateurBulletin.generePdf() : erreur lors de la fermeture du fichier (" + e.getClass().getName() + " : " + e.getMessage() + ")");
        }
    }

    /**
     * Ouvre une nouvelle page pour 'limpression.
     * <p>
     * La fonction écrit également le footer avec printFooter()
     * </p>
     */
    private void startNewPage()
    {
        if(page != null) {
            pdf.finishPage(page);
        }
        page = pdf.startPage(pagecount);
        pagecount++;
        canvas = page.getCanvas();
        offset = 0;
        footerheight = 0;
        printFooter();
    }


    static public float inchToCm(float v)
    {
        return v * 2.54f;
    }

    static public float cmToInch(float v)
    {
        return v / 2.54f;
    }

    /**
     * Convertie une distance en cm en un nombre de points sur le canvas.
     * <p>
     * Il est préférable d'eviter d'utiliser cette fonction car la précision du
     * résultat n'est pas garantie (problème de documentation de l'API Android).
     * </p>
     * @param cm
     * @return
     */
    public int cmToPoint(float cm)
    {
        return Math.round(cm * ((float) canvas.getHeight()) / (29.7f - TopMargin - BottomMargin));
    }


    /**
     * Parce que la peinture ça ne s'improvise pas !
     */
    private void testCanvasDrawText()
    {
        Paint painter = new Paint();
        painter.setColor(ColorBlack);
        painter.setTextSize(18.f);
        //painter.setStyle(Paint.Style.STROKE);
        painter.setTypeface(Typeface.create("Arial", Typeface.NORMAL));

        final int RectMargin = 20;


        Rect rect = new Rect();
        String str = "Bob l'Eponge carré (et Patrick) !";
        painter.getTextBounds(str, 0, str.length(), rect);

        painter.setStyle(Paint.Style.STROKE);
        canvas.drawRect(new Rect(0, offset, rect.width(), offset+rect.height()+RectMargin), painter);
        painter.setStyle(Paint.Style.FILL_AND_STROKE);

        Paint.FontMetricsInt metrics = painter.getFontMetricsInt();

        //int y = offset + (rect.height()+RectMargin) - metrics.bottom - RectMargin/2;
        int y = offset + (rect.height()+RectMargin)/2 + metrics.bottom;
        painter.setStrokeWidth(1.25f);
        painter.setColor(Color.RED);
        canvas.drawLine(0, y + metrics.descent, rect.width(), y + metrics.descent, painter);
        painter.setColor(Color.GREEN);
        canvas.drawLine(0, y + metrics.ascent, rect.width(), y + metrics.ascent, painter);
        painter.setColor(Color.YELLOW);
        canvas.drawLine(0, y + metrics.bottom, rect.width(), y + metrics.bottom, painter);
        painter.setColor(Color.MAGENTA);
        canvas.drawLine(0, y + metrics.top, rect.width(), y + metrics.top, painter);
        painter.setStrokeWidth(1.f);

        painter.setColor(ColorBlack);
        canvas.drawText(str, 0, y, painter);

        offset += rect.height() + RectMargin;

    }

    /**
     * Ecrit l'entête du bulletin sur la page courante.
     */
    private void printHeaders()
    {
        final String Str_Cycle = getCycleString();
        final String Str_CpCoursPreparatoire = getCoursString();
        final int RectMargin = 6;
        final int Spacing1 = 26;
        final int Spacing2 = 14;

        Paint painter = PinceauHeader0();

        Rect rect = new Rect();
        painter.getTextBounds(Str_CpCoursPreparatoire, 0, Str_CpCoursPreparatoire.length(), rect);

        canvas.drawRect(new Rect(0, offset, canvas.getWidth(), offset + rect.height() + RectMargin), PinceauRect1());
        canvas.drawText(Str_Cycle, RectMargin, offset + (rect.height()+RectMargin)/2 + painter.getFontMetricsInt().bottom, painter);
        canvas.drawText(Str_CpCoursPreparatoire, canvas.getWidth() - painter.measureText(Str_CpCoursPreparatoire) - RectMargin, offset + (rect.height()+RectMargin)/2 + painter.getFontMetricsInt().bottom, painter);

        offset += rect.height() + RectMargin;

        offset += Spacing1;


        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final String ecole = pref.getString("pref_nom_ecole", "");
        final String siteweb = pref.getString("pref_siteweb_ecole", "");
        final String classe = "Classe de " + pref.getString("pref_nom_classe", "");
        final int annee = Integer.parseInt(pref.getString("pref_annee_scolaire", "-1"));
        final String annee_scol = "Année scolaire " + annee  + "-" + (annee+1);
        String trimestre_str = "" + trimestre.id();
        if(trimestre.id() == 1) {
            trimestre_str += "er ";
        } else {
            trimestre_str += "èmé ";
        }
        trimestre_str += "trimestre";
        String elv = "Elève : " + eleve.getNom() + " " + eleve.getPrenom();
        final int leftmargin = canvas.getWidth() / 4;

        painter = PinceauHeader1();

        Bitmap logo = getLogoBulletin();
        if(logo != null) {
            float logo_width = logo.getWidth();
            float logo_height = logo.getHeight();
            float scaleFactor = logo_width / leftmargin;
            float scaledWidth = logo.getWidth() / scaleFactor;
            float scaledHeight = logo.getHeight() / scaleFactor;
            //logo = Bitmap.createScaledBitmap(logo, Math.round(logo.getWidth() / scaleFactor), Math.round(logo.getHeight() / scaleFactor), false);
            canvas.drawBitmap(logo, null, new RectF(0, offset, scaledWidth, offset+scaledHeight), painter);
        }

        painter.getTextBounds(ecole, 0, ecole.length(), rect);
        canvas.drawText(ecole, leftmargin, offset + rect.height() / 2, painter);
        offset += rect.height() + Interligne;

        if(!siteweb.isEmpty()) {
            painter.getTextBounds(siteweb, 0, siteweb.length(), rect);
            canvas.drawText(siteweb, leftmargin, offset + rect.height() / 2, painter);
            offset += rect.height() + Interligne;
        }

        painter.getTextBounds(classe, 0, classe.length(), rect);
        canvas.drawText(classe, leftmargin, offset + rect.height() / 2, painter);
        offset += rect.height() + Interligne;

        painter.getTextBounds(annee_scol, 0, annee_scol.length(), rect);
        canvas.drawText(annee_scol, leftmargin, offset + rect.height() / 2, painter);
        offset += rect.height() + Interligne;

        painter.getTextBounds(trimestre_str, 0, trimestre_str.length(), rect);
        canvas.drawText(trimestre_str, leftmargin, offset + rect.height() / 2, painter);
        offset += rect.height() + Interligne;


        painter.setTypeface(Typeface.create(Police, Typeface.BOLD));
        painter.getTextBounds(elv, 0, ecole.length(), rect);
        canvas.drawText(elv, leftmargin, offset + rect.height() / 2, painter);
        offset += rect.height() + Interligne;

        offset += Spacing2;
    }

    /**
     * Ecrit sur la page courante le bas de page.
     */
    private void printFooter()
    {
        /**
         * Marge entre le bas de page et le contenu de la page (en points du Canvas)
         */
        final int footermargin = 5;
        final float stroke_width = 1.15f;

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String enseignant = pref.getString("pref_nom_enseignant", "");
        String elv = eleve.getNom() + " " + eleve.getPrenom();

        Paint p = PinceauFooter();
        Rect enseignant_rect = new Rect();
        p.getTextBounds(enseignant, 0, enseignant.length(), enseignant_rect);
        Rect eleve_rect = new Rect();
        p.getTextBounds(elv, 0, elv.length(), eleve_rect);
        int h = Math.max(eleve_rect.height(), enseignant_rect.height());
        canvas.drawText(enseignant, 0, canvas.getHeight() - h/2 + p.getFontMetricsInt().bottom, p);
        canvas.drawText(elv, canvas.getWidth() - p.measureText(elv), canvas.getHeight() - h/2 + p.getFontMetricsInt().bottom, p);
        canvas.drawLine(0, canvas.getHeight() - h, canvas.getWidth(), canvas.getHeight() - h, PinceauLine1());


        footerheight = h + footermargin;
    }

    /**
     * Renvoie true s'il reste assez de place sur la page pour un bloc de hauteur h
     * @param h
     * @return
     */
    private boolean hasEnoughSpace(int h) {
        return canvas.getHeight() - footerheight - offset >= h;
    }


    /**
     * Imprime dans le bulletin la partie lié à la compétence c.
     * La fonction traite n'importe quelle profondeur de compétence
     * @param c
     */
    private void printCompetence(Competence c) {
        if(c.depth() == 0)  {
            final int RectPadding = 10;
            final int RectLeftPadding = 6;

            String name = c.getNom();
            Paint p = PinceauCompetence0();
            Rect textRect = new Rect();
            p.getTextBounds(name, 0, name.length(), textRect);
            float tauxReussiteFloat = eleve.calculTauxReussite(c, trimestre);
            String tauxReussite = String.format("%.2f", tauxReussiteFloat) + "%";

            if(!hasEnoughSpace(textRect.height() + RectPadding)) {
                startNewPage();
            }

            Rect black_rect = new Rect(0, offset, canvas.getWidth(), offset + textRect.height() + RectPadding);

            // on dessine le rectanle
            canvas.drawRect(black_rect, PinceauRect1());
            // puis le nom de la compétence
            canvas.drawText(name, RectLeftPadding, offset + black_rect.height() / 2 + p.getFontMetricsInt().bottom , p);
            //  et enfin le taux de réussite
            canvas.drawText(tauxReussite, canvas.getWidth() - TauxReussiteMargin, offset + black_rect.height() / 2 + p.getFontMetricsInt().bottom, p);

            printProgres(c, tauxReussiteFloat, p, offset + black_rect.height() / 2 - IndicateurProgresSize / 2);

            // on avance dans la page
            offset += black_rect.height() + BottomMarginCompetence0;

        }
        else if(c.depth() == 1)
        {
            String name = c.getNom();
            Paint p = PinceauCompetence1();
            Rect textRect = new Rect();
            p.getTextBounds(name, 0, name.length(), textRect);
            float taux_reussite = eleve.calculTauxReussite(c, trimestre);
            String tauxReussite = String.format("%.2f", taux_reussite) + "%";

            if(!hasEnoughSpace(textRect.height())) {
                startNewPage();
            }

            // on dessine le nom de la compétence
            canvas.drawText(name, IndentCompetence1, offset, p);
            //  et le taux de réussite
            canvas.drawText(tauxReussite, canvas.getWidth() - TauxReussiteMargin, offset, p);
            // avant de souligner le tout
            canvas.drawLine(IndentCompetence1, offset + p.getFontMetrics().bottom, canvas.getWidth(), offset + p.getFontMetrics().bottom, PinceauLine1());

            printProgres(c, taux_reussite, p, offset - IndicateurProgresSize / 2  - p.getFontMetricsInt().bottom);

            // on avance dans la page
            offset += textRect.height() + BottomMarginCompetence1;
        }
        else if(c.depth() == 2)
        {
            String name = c.getNom();
            Paint p = PinceauCompetence2();
            Rect textRect = new Rect();
            p.getTextBounds(name, 0, name.length(), textRect);
            float taux_reussite = eleve.calculTauxReussite(c, trimestre);
            String tauxReussite = String.format("%.2f", taux_reussite) + "%";

            if(!hasEnoughSpace(textRect.height())) {
                startNewPage();
            }

            // on dessine le nom de la compétence
            canvas.drawText(name, IndentCompetence2, offset, p);
            //  et le taux de réussite
            canvas.drawText(tauxReussite, canvas.getWidth() - TauxReussiteMargin, offset, p);
            // avant de souligner le tout
            canvas.drawLine(IndentCompetence2, offset + p.getFontMetrics().bottom, canvas.getWidth(), offset + p.getFontMetrics().bottom, PinceauLine2());


            printProgres(c, taux_reussite, p, offset - IndicateurProgresSize / 2  - p.getFontMetricsInt().bottom);

            // on avance dans la page
            offset += textRect.height() + BottomMarginCompetence2;
        }
        else if(c.depth() == 3)
        {
            String name = c.getNom();
            Paint p = PinceauCompetence3();
            Rect textRect = new Rect();
            p.getTextBounds(name, 0, name.length(), textRect);
            float taux_reussite = eleve.calculTauxReussite(c, trimestre);
            String tauxReussite = String.format("%.2f", taux_reussite) + "%";

            if(!hasEnoughSpace(textRect.height())) {
                startNewPage();
            }

            // on dessine le nom de la compétence
            canvas.drawText(name, IndentCompetence3, offset, p);
            //  et le taux de réussite
            canvas.drawText(tauxReussite, canvas.getWidth() - TauxReussiteMargin, offset, p);

            printProgres(c, taux_reussite, p, offset - IndicateurProgresSize / 2 - p.getFontMetricsInt().bottom);

            // on avance dans la page
            offset += textRect.height() + BottomMarginCompetence3;
        }



        // enfin, on imprime les sous-compétences
        for(Competence sc : eleve.getSousCompetencesNotees(c, trimestre)) {
            printCompetence(sc);
        }
    }

    /**
     * Imprime le progrès par rapport au trimestre précédent.
     * @param c La compétence
     * @param tauxReussiteActuel le taux de réussite pour ce trimestre
     * @param painter le pinceau a utiliser
     * @param y la coordonnée ou dessiner
     */
    private void printProgres(Competence c, float tauxReussiteActuel, Paint painter, int y)
    {
        if(trimestre.id() == 1)
        {
            return;
        }

        float ancien_taux_reussite = -1.0f;
        try {
            ancien_taux_reussite = eleve.calculTauxReussite(c, MainActivity.ClassaidDatabase.getTrimestre(trimestre.id() - 1));
        } catch (RuntimeException e) {
            // le calcul du taux de réussite a échouer,
            // ca veut dire que la compétence n'était pas noté au trimestre précédent
            return;
        }

        int resid = (ancien_taux_reussite == tauxReussiteActuel ? R.drawable.icon_equals :
                (ancien_taux_reussite > tauxReussiteActuel ? R.drawable.ic_arrow_bottomright : R.drawable.ic_arrow_topright));
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resid);
        canvas.drawBitmap(bitmap, null, new Rect(canvas.getWidth() - IndicateurProgresMargin, y, canvas.getWidth() - IndicateurProgresMargin + IndicateurProgresSize, y + IndicateurProgresSize), painter);
    }


}
