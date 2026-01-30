package com.pizzamaestro.service;

import com.pizzamaestro.model.Recipe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

/**
 * Serwis do generowania PDF z przepisami na pizzę.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PdfExportService {

    private static final float MARGIN = 50;
    private static final float FONT_SIZE_TITLE = 24;
    private static final float FONT_SIZE_HEADER = 14;
    private static final float FONT_SIZE_NORMAL = 11;
    private static final float LINE_HEIGHT = 16;

    /**
     * Generuje PDF z przepisem na pizzę.
     */
    public byte[] generateRecipePdf(Recipe recipe) throws IOException {
        log.info("📄 Generowanie PDF dla przepisu: {}", recipe.getName());

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float yPosition = page.getMediaBox().getHeight() - MARGIN;
                float width = page.getMediaBox().getWidth() - 2 * MARGIN;

                // Tytuł
                yPosition = drawTitle(content, recipe.getName(), yPosition);
                
                // Podtytuł ze stylem
                if (recipe.getPizzaStyle() != null) {
                    yPosition = drawSubtitle(content, "Styl: " + recipe.getPizzaStyle().getDisplayName(), yPosition);
                }
                
                // Linia oddzielająca
                yPosition -= 10;
                drawLine(content, MARGIN, yPosition, MARGIN + width, yPosition);
                yPosition -= 20;

                // Informacje podstawowe
                yPosition = drawHeader(content, "Podstawowe informacje", yPosition);
                yPosition = drawText(content, "Liczba pizz: " + recipe.getNumberOfPizzas(), yPosition);
                yPosition = drawText(content, "Waga kulki: " + recipe.getBallWeight() + " g", yPosition);
                yPosition = drawText(content, "Nawodnienie: " + recipe.getHydration() + "%", yPosition);
                if (recipe.getFermentationMethod() != null) {
                    yPosition = drawText(content, "Metoda fermentacji: " + recipe.getFermentationMethod(), yPosition);
                }
                yPosition -= 15;

                // Składniki
                if (recipe.getCalculatedRecipe() != null) {
                    yPosition = drawHeader(content, "Składniki", yPosition);
                    Recipe.CalculatedRecipe calc = recipe.getCalculatedRecipe();
                    
                    if (calc.getFlourGrams() > 0) {
                        yPosition = drawIngredient(content, "Mąka", calc.getFlourGrams(), "g", yPosition);
                    }
                    if (calc.getWaterGrams() > 0) {
                        yPosition = drawIngredient(content, "Woda", calc.getWaterGrams(), "g", yPosition);
                    }
                    if (calc.getSaltGrams() > 0) {
                        yPosition = drawIngredient(content, "Sól", calc.getSaltGrams(), "g", yPosition);
                    }
                    if (calc.getYeastGrams() > 0) {
                        yPosition = drawIngredient(content, "Drożdże", calc.getYeastGrams(), "g", yPosition);
                    }
                    if (calc.getOilGrams() > 0) {
                        yPosition = drawIngredient(content, "Oliwa", calc.getOilGrams(), "g", yPosition);
                    }
                    if (calc.getSugarGrams() > 0) {
                        yPosition = drawIngredient(content, "Cukier", calc.getSugarGrams(), "g", yPosition);
                    }
                    if (calc.getTotalDoughWeight() > 0) {
                        yPosition -= 5;
                        yPosition = drawText(content, "Całkowita waga ciasta: " + calc.getTotalDoughWeight() + " g", yPosition);
                    }
                    yPosition -= 15;
                }

                // Harmonogram (jeśli jest)
                if (recipe.getFermentationSteps() != null && !recipe.getFermentationSteps().isEmpty()) {
                    yPosition = drawHeader(content, "Harmonogram", yPosition);
                    
                    DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
                    
                    for (Recipe.FermentationStep step : recipe.getFermentationSteps()) {
                        String timeStr = step.getScheduledTime() != null 
                                ? step.getScheduledTime().format(timeFormat) + " - " 
                                : "";
                        yPosition = drawText(content, step.getStepNumber() + ". " + timeStr + step.getTitle(), yPosition);
                        
                        if (step.getDescription() != null && yPosition > MARGIN + 50) {
                            yPosition = drawSmallText(content, "   " + truncate(step.getDescription(), 80), yPosition);
                        }
                        
                        // Sprawdź czy jest miejsce na stronie
                        if (yPosition < MARGIN + 100) {
                            // Nowa strona
                            content.close();
                            PDPage newPage = new PDPage(PDRectangle.A4);
                            document.addPage(newPage);
                            PDPageContentStream newContent = new PDPageContentStream(document, newPage);
                            yPosition = newPage.getMediaBox().getHeight() - MARGIN;
                            // Kontynuuj na nowej stronie
                        }
                    }
                    yPosition -= 15;
                }

                // Notatki
                if (recipe.getNotes() != null && !recipe.getNotes().isEmpty()) {
                    yPosition = drawHeader(content, "Notatki", yPosition);
                    yPosition = drawText(content, recipe.getNotes(), yPosition);
                    yPosition -= 15;
                }

                // Stopka
                yPosition = MARGIN + 30;
                drawLine(content, MARGIN, yPosition, MARGIN + width, yPosition);
                yPosition -= 15;
                drawSmallText(content, "Wygenerowano przez PizzaMaestro | pizzamaestro.pl", yPosition);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            
            log.info("✅ PDF wygenerowany pomyślnie ({} bajtów)", baos.size());
            return baos.toByteArray();
        }
    }

    private float drawTitle(PDPageContentStream content, String text, float y) throws IOException {
        content.beginText();
        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), FONT_SIZE_TITLE);
        content.newLineAtOffset(MARGIN, y);
        content.showText(sanitizeText(text));
        content.endText();
        return y - FONT_SIZE_TITLE - 10;
    }

    private float drawSubtitle(PDPageContentStream content, String text, float y) throws IOException {
        content.beginText();
        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE), FONT_SIZE_HEADER);
        content.newLineAtOffset(MARGIN, y);
        content.showText(sanitizeText(text));
        content.endText();
        return y - FONT_SIZE_HEADER - 10;
    }

    private float drawHeader(PDPageContentStream content, String text, float y) throws IOException {
        content.beginText();
        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), FONT_SIZE_HEADER);
        content.newLineAtOffset(MARGIN, y);
        content.showText(sanitizeText(text));
        content.endText();
        return y - LINE_HEIGHT - 5;
    }

    private float drawText(PDPageContentStream content, String text, float y) throws IOException {
        content.beginText();
        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), FONT_SIZE_NORMAL);
        content.newLineAtOffset(MARGIN + 10, y);
        content.showText(sanitizeText(truncate(text, 90)));
        content.endText();
        return y - LINE_HEIGHT;
    }

    private float drawSmallText(PDPageContentStream content, String text, float y) throws IOException {
        content.beginText();
        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
        content.newLineAtOffset(MARGIN + 10, y);
        content.showText(sanitizeText(truncate(text, 100)));
        content.endText();
        return y - 12;
    }

    private float drawIngredient(PDPageContentStream content, String name, Number amount, String unit, float y) throws IOException {
        String text = String.format("• %s: %.1f %s", name, amount.doubleValue(), unit);
        return drawText(content, text, y);
    }

    private void drawLine(PDPageContentStream content, float x1, float y1, float x2, float y2) throws IOException {
        content.moveTo(x1, y1);
        content.lineTo(x2, y2);
        content.stroke();
    }

    /**
     * Sanityzuje tekst dla PDF (usuwa znaki specjalne nieobsługiwane przez czcionkę).
     */
    private String sanitizeText(String text) {
        if (text == null) return "";
        // Zamień polskie znaki na ASCII (PDType1Font nie obsługuje polskich znaków)
        return text
                .replace("ą", "a").replace("ć", "c").replace("ę", "e")
                .replace("ł", "l").replace("ń", "n").replace("ó", "o")
                .replace("ś", "s").replace("ź", "z").replace("ż", "z")
                .replace("Ą", "A").replace("Ć", "C").replace("Ę", "E")
                .replace("Ł", "L").replace("Ń", "N").replace("Ó", "O")
                .replace("Ś", "S").replace("Ź", "Z").replace("Ż", "Z");
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
