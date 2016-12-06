package classaid.pdf;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
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



    public GenerateurBulletin(Context con, Eleve e, Trimestre t)
    {
        eleve = e;
        trimestre = t;

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
            File file = new File(filename);
            if(!file.exists()) {
                file.createNewFile();
            }
            output = new FileOutputStream(file);
        } catch (Exception e) {
            pdf.close();
            return;
        }

        try {
            pdf.writeTo(output);
        } catch(Exception e) {

        }

        pdf.close();
    }

    /**
     * Ouvre une nouvelle page pour 'limpression
     */
    private void startNewPage()
    {
        if(page != null) {
            pdf.finishPage(page);
        }
        page = pdf.startPage(pagecount);
        canvas = page.getCanvas();
        offset = 0;
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

    public int cmToPixel(float cm)
    {
        return Math.round(cmToInch(cm) / DPI);
    }

    private void printHeaders()
    {
        final String Str_Cycle2 = "Cycle 2";
        final String Str_CpCoursPreparatoire = "CP - Cours préparatoire";

        Paint painter = new Paint();
        painter.setColor(ColorBlack);
        Rect rect =  new Rect(0, 0, canvas.getWidth(), cmToPixel(1.f));
        canvas.drawRect(rect, painter);
        canvas.drawText(Str_Cycle2, 0.f, 0.f, painter);
        canvas.drawText(Str_CpCoursPreparatoire, canvas.getWidth() - painter.measureText(Str_CpCoursPreparatoire), 0.f, painter);

        offset += rect.height() + Interligne;
    }

    /**
     * Renvoie true s'il reste assez de place sur la page pour un bloc de hauteur h
     * @param h
     * @return
     */
    private boolean hasEnoughSpace(int h) {
        return canvas.getHeight() - offset >= h;
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
            canvas.drawText(name, 0, offset + RectPadding / 2, p);
            //  et enfin le taux de réussite
            canvas.drawText(tauxReussite, canvas.getWidth() - TauxReussiteMargin, offset + RectPadding / 2, p);

            // on avance dans la page
            offset += black_rect.height() + Interligne;

        }
        else if(c.depth() == 1)
        {
            String name = c.getNom();
            Paint p = PinceauCompetence1();
            Rect textRect = new Rect();
            p.getTextBounds(name, 0, name.length(), textRect);
            String tauxReussite = String.format("%.2f", eleve.calculTauxReussite(c, trimestre)) + "%";

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
            String tauxReussite = String.format("%.2f", eleve.calculTauxReussite(c, trimestre)) + "%";

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
        for(Competence sc : c.getSousCompetences()) {
            printCompetence(sc);
        }
    }


}
