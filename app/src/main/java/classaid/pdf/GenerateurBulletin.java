package classaid.pdf;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.print.PrintAttributes;
import android.print.pdf.PrintedPdfDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

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
     * L'interligne en pixel
     */
    final static public int Interligne = 15;

    /**
     * Renvoie le pinceau a utiliser pour écrire une compétence de
     * pronfondeur 0 (compétence mère)
     * @return
     */
    static public Paint PinceauCompetence0() {
        Paint p = new Paint();
        p.setTextSize(14);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(ColorBlack);
        p.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
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
        p.setStyle(Paint.Style.STROKE);
        p.setColor(ColorBlack);
        p.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        return p;
    }

    /**
     * Renvoie le pinceau a utiliser pour écrire une compétence de
     * pronfondeur 2 (sous-sous-compétence)
     * @return
     */
    static public Paint PinceauCompetence2() {
        Paint p = new Paint();
        p.setTextSize(12);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(ColorBlack);
        return p;
    }

    static public Paint PinceauFooter() {
        Paint p = new Paint();
        p.setTextSize(12);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(ColorBlack);
        return p;
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

    /**
     * Génère et enregistre le bulletin au format PDF dans un fichier.
     */
    public void generePdf(String filename)
    {
        pagecount = 0;
        startNewPage();

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
        canvas = page.getCanvas();
        offset = 0;
        footerheight = 0;
        printFooter();
        pagecount++;
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
     * Ecrit l'entête du bulletin sur la page courante.
     */
    private void printHeaders()
    {
        final String Str_Cycle2 = "Cycle 2";
        final String Str_CpCoursPreparatoire = "CP - Cours préparatoire";
        final int RectMargin = 10;
        final int Spacing1 = 16;
        final int Spacing2 = 16;

        Paint painter = new Paint();
        painter.setColor(ColorBlack);
        painter.setTextSize(14.f);
        painter.setStyle(Paint.Style.STROKE);

        Rect rect = new Rect();
        painter.getTextBounds(Str_CpCoursPreparatoire, 0, Str_CpCoursPreparatoire.length(), rect);

        canvas.drawRect(new Rect(0, offset, canvas.getWidth(), offset + rect.height() + RectMargin), painter);
        canvas.drawText(Str_Cycle2, 0.f, offset + painter.getFontMetrics().descent + (rect.height() + RectMargin) / 2, painter);
        canvas.drawText(Str_CpCoursPreparatoire, canvas.getWidth() - rect.width() - RectMargin, offset + painter.getFontMetrics().descent + (rect.height() + RectMargin) / 2, painter);

        offset += rect.height() + RectMargin;

        offset += Spacing1;

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final String ecole = pref.getString("pref_nom_ecole", "");
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

        painter.setTextSize(12.f);

        painter.getTextBounds(ecole, 0, ecole.length(), rect);
        canvas.drawText(ecole, leftmargin, offset + rect.height() / 2, painter);
        offset += rect.height() + Interligne;

        painter.getTextBounds(classe, 0, ecole.length(), rect);
        canvas.drawText(classe, leftmargin, offset + rect.height() / 2, painter);
        offset += rect.height() + Interligne;

        painter.getTextBounds(annee_scol, 0, ecole.length(), rect);
        canvas.drawText(annee_scol, leftmargin, offset + rect.height() / 2, painter);
        offset += rect.height() + Interligne;

        painter.getTextBounds(trimestre_str, 0, ecole.length(), rect);
        canvas.drawText(trimestre_str, leftmargin, offset + rect.height() / 2, painter);
        offset += rect.height() + Interligne;

        painter.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
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

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String enseignant = pref.getString("pref_nom_enseignant", "");
        String elv = eleve.getNom() + " " + eleve.getPrenom();

        Paint p = PinceauFooter();
        Rect enseignant_rect = new Rect();
        p.getTextBounds(enseignant, 0, enseignant.length(), enseignant_rect);
        Rect eleve_rect = new Rect();
        p.getTextBounds(elv, 0, elv.length(), eleve_rect);
        int h = Math.max(eleve_rect.height(), enseignant_rect.height());
        canvas.drawLine(0, canvas.getHeight() - h, canvas.getWidth(), canvas.getHeight() - h, p);
        canvas.drawText(enseignant, 0, canvas.getHeight() - h/2, p);
        canvas.drawText(elv, canvas.getWidth() - eleve_rect.width(), canvas.getHeight() - h/2, p);

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

            String name = c.getNom();
            Paint p = PinceauCompetence0();
            Rect textRect = new Rect();
            p.getTextBounds(name, 0, name.length(), textRect);
            String tauxReussite = String.format("%.2f", eleve.calculTauxReussite(c, trimestre)) + "%";
            Rect black_rect = new Rect(0, offset, canvas.getWidth(), offset + textRect.height() + RectPadding);

            if(!hasEnoughSpace(black_rect.height())) {
                startNewPage();
            }

            // on dessine le rectanle
            canvas.drawRect(black_rect, p);
            // puis le nom de la compétence
            canvas.drawText(name, 0, offset + black_rect.height() / 2 + RectPadding / 2 , p);
            //  et enfin le taux de réussite
            canvas.drawText(tauxReussite, canvas.getWidth() - TauxReussiteMargin, offset + black_rect.height() / 2 +  RectPadding / 2, p);

            // on avance dans la page
            offset += black_rect.height() + Interligne;

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
            canvas.drawText(name, 0, offset, p);
            //  et le taux de réussite
            canvas.drawText(tauxReussite, canvas.getWidth() - TauxReussiteMargin, offset, p);
            // avant de souligner le tout
            canvas.drawLine(0, offset + p.getFontMetrics().descent, canvas.getWidth(), offset + p.getFontMetrics().descent, p);

            // on avance dans la page
            offset += textRect.height() + Interligne;
        }
        else // c.depth() == 2
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
            canvas.drawText(name, 0, offset, p);
            //  et le taux de réussite
            canvas.drawText(tauxReussite, canvas.getWidth() - TauxReussiteMargin, offset, p);

            // on avance dans la page
            offset += textRect.height() + Interligne;
        }

        // enfin, on imprime les sous-compétences
        for(Competence sc : eleve.getSousCompetencesNotees(c, trimestre)) {
            printCompetence(sc);
        }
    }


}
