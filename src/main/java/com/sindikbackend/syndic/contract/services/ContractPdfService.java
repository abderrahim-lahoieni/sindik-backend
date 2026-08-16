package com.sindikbackend.syndic.contract.services;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.sindikbackend.admin.unit.entities.UnitType;
import com.sindikbackend.auth.exceptions.ResourceNotFoundException;
import com.sindikbackend.syndic.contract.entities.Contract;
import com.sindikbackend.syndic.contract.enums.ContractStatus;
import com.sindikbackend.syndic.contract.enums.ContractType;
import com.sindikbackend.syndic.contract.repositories.ContractRepository;
import com.sindikbackend.syndic.payment.enums.PaymentFrequency;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractPdfService {

    private final ContractRepository contractRepository;

    // Couleurs professionnelles
    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(41, 128, 185);
    private static final DeviceRgb SECONDARY_COLOR = new DeviceRgb(52, 73, 94);
    private static final DeviceRgb ACCENT_COLOR = new DeviceRgb(46, 204, 113);
    private static final DeviceRgb DARK_COLOR = new DeviceRgb(44, 62, 80);
    private static final DeviceRgb GRAY_COLOR = new DeviceRgb(127, 140, 141);
    private static final DeviceRgb LIGHT_BG = new DeviceRgb(236, 240, 241);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);
    private static final DateTimeFormatter DATE_FORMATTER_SHORT = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRENCH);

    public byte[] generateContractPdf(UUID contractUuid) throws IOException {
        log.info("Génération du PDF pour le contrat: {}", contractUuid);

        Contract contract = contractRepository.findByUuid(contractUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Contrat non trouvé avec uuid: " + contractUuid));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDoc = new PdfDocument(writer);

        // Format A4 avec marges réduites
        Document document = new Document(pdfDoc, PageSize.A4);
        document.setMargins(20, 20, 20, 20); // Réduit de 30 à 20

        // Fonts
        PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // ==================== EN-TÊTE PROFESSIONNEL ====================
        Table headerTable = new Table(2);
        headerTable.setWidth(UnitValue.createPercentValue(100));
        headerTable.setBorder(Border.NO_BORDER);
        headerTable.setMarginBottom(10); // Réduit de 15 à 10

        // Logo / Titre à gauche
        Cell titleCell = new Cell();
        titleCell.setBorder(Border.NO_BORDER);
        titleCell.setPadding(0);

        Paragraph companyName = new Paragraph("SYNDIC DE LA RÉSIDENCE")
                .setFont(boldFont)
                .setFontSize(9) // Réduit de 10 à 9
                .setFontColor(GRAY_COLOR);

        Paragraph residenceName = new Paragraph(contract.getUnit().getBuilding().getResidence().getName().toUpperCase())
                .setFont(boldFont)
                .setFontSize(12) // Réduit de 14 à 12
                .setFontColor(PRIMARY_COLOR)
                .setMarginTop(1); // Réduit de 2 à 1

        titleCell.add(companyName);
        titleCell.add(residenceName);

        // Numéro de contrat à droite
        Cell contractNumberCell = new Cell();
        contractNumberCell.setBorder(Border.NO_BORDER);
        contractNumberCell.setPadding(0);
        contractNumberCell.setTextAlignment(TextAlignment.RIGHT);

        Paragraph refLabel = new Paragraph("RÉFÉRENCE")
                .setFont(boldFont)
                .setFontSize(7) // Réduit de 8 à 7
                .setFontColor(GRAY_COLOR);

        Paragraph contractNumber = new Paragraph(contract.getContractNumber())
                .setFont(boldFont)
                .setFontSize(10) // Réduit de 12 à 10
                .setFontColor(SECONDARY_COLOR)
                .setMarginTop(1); // Réduit de 2 à 1

        contractNumberCell.add(refLabel);
        contractNumberCell.add(contractNumber);

        headerTable.addCell(titleCell);
        headerTable.addCell(contractNumberCell);

        document.add(headerTable);

        // Ligne de séparation
        Table separatorLine = new Table(1);
        separatorLine.setWidth(UnitValue.createPercentValue(100));
        separatorLine.setBorder(Border.NO_BORDER);
        separatorLine.setMarginBottom(12); // Réduit de 20 à 12

        Cell lineCell = new Cell();
        lineCell.setBorderBottom(new SolidBorder(PRIMARY_COLOR, 1.5f)); // Réduit de 2 à 1.5
        lineCell.setPadding(0);
        separatorLine.addCell(lineCell);

        document.add(separatorLine);

        // ==================== TITRE DU DOCUMENT ====================
        Paragraph title = new Paragraph("CONTRAT DE LOCATION")
                .setFont(boldFont)
                .setFontSize(16) // Réduit de 18 à 16
                .setFontColor(SECONDARY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(3); // Réduit de 5 à 3
        document.add(title);

        // Date du contrat
        Paragraph dateStr = new Paragraph("Fait à Tanger, le " + LocalDate.now().format(DATE_FORMATTER))
                .setFont(regularFont)
                .setFontSize(9) // Réduit de 10 à 9
                .setFontColor(GRAY_COLOR)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(12); // Réduit de 20 à 12
        document.add(dateStr);

        // ==================== PARTIES ====================
        Paragraph partiesTitle = new Paragraph("ENTRE LES SOUSSIGNÉS :")
                .setFont(boldFont)
                .setFontSize(10) // Réduit de 11 à 10
                .setFontColor(SECONDARY_COLOR)
                .setMarginBottom(6); // Réduit de 10 à 6
        document.add(partiesTitle);

        // Tableau des parties
        Table partiesTable = new Table(2);
        partiesTable.setWidth(UnitValue.createPercentValue(100));
        partiesTable.setBorder(Border.NO_BORDER);
        partiesTable.setMarginBottom(10); // Réduit de 15 à 10

        // Bailleur
        Cell bailleurCell = new Cell();
        bailleurCell.setBorder(Border.NO_BORDER);
        bailleurCell.setPaddingRight(8); // Réduit de 10 à 8

        Paragraph bailleurLabel = new Paragraph("LE BAILLEUR")
                .setFont(boldFont)
                .setFontSize(9) // Réduit de 10 à 9
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(3); // Réduit de 5 à 3

        Paragraph bailleurName = new Paragraph("Syndic de la résidence " + contract.getUnit().getBuilding().getResidence().getName())
                .setFont(boldFont)
                .setFontSize(9) // Réduit de 10 à 9
                .setFontColor(DARK_COLOR)
                .setMarginBottom(2); // Réduit de 3 à 2

        Paragraph bailleurAddress = new Paragraph(contract.getUnit().getBuilding().getResidence().getAddress())
                .setFont(regularFont)
                .setFontSize(8) // Réduit de 9 à 8
                .setFontColor(GRAY_COLOR)
                .setMarginBottom(1); // Réduit de 2 à 1

        bailleurCell.add(bailleurLabel);
        bailleurCell.add(bailleurName);
        bailleurCell.add(bailleurAddress);

        // Preneur
        Cell preneurCell = new Cell();
        preneurCell.setBorder(Border.NO_BORDER);
        preneurCell.setPaddingLeft(8); // Réduit de 10 à 8

        Paragraph preneurLabel = new Paragraph("LE PRENEUR")
                .setFont(boldFont)
                .setFontSize(9) // Réduit de 10 à 9
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(3); // Réduit de 5 à 3

        Paragraph preneurName = new Paragraph(contract.getResident().getFullName())
                .setFont(boldFont)
                .setFontSize(9) // Réduit de 10 à 9
                .setFontColor(DARK_COLOR)
                .setMarginBottom(2); // Réduit de 3 à 2

        Paragraph preneurPhone = new Paragraph("Tél: " + contract.getResident().getPhoneNumber())
                .setFont(regularFont)
                .setFontSize(8) // Réduit de 9 à 8
                .setFontColor(GRAY_COLOR)
                .setMarginBottom(1); // Réduit de 2 à 1

        if (contract.getResident().getEmergencyPhone() != null && !contract.getResident().getEmergencyPhone().isEmpty()) {
            Paragraph preneurEmail = new Paragraph("Email: " + contract.getResident().getEmergencyPhone())
                    .setFont(regularFont)
                    .setFontSize(8) // Réduit de 9 à 8
                    .setFontColor(GRAY_COLOR);
            preneurCell.add(preneurLabel);
            preneurCell.add(preneurName);
            preneurCell.add(preneurPhone);
            preneurCell.add(preneurEmail);
        } else {
            preneurCell.add(preneurLabel);
            preneurCell.add(preneurName);
            preneurCell.add(preneurPhone);
        }

        partiesTable.addCell(bailleurCell);
        partiesTable.addCell(preneurCell);

        document.add(partiesTable);

        // ==================== OBJET DU CONTRAT ====================
        Paragraph objetTitle = new Paragraph("OBJET DU CONTRAT")
                .setFont(boldFont)
                .setFontSize(10) // Réduit de 11 à 10
                .setFontColor(SECONDARY_COLOR)
                .setMarginBottom(6); // Réduit de 10 à 6
        document.add(objetTitle);

        // Description du bien
        Table bienTable = new Table(2);
        bienTable.setWidth(UnitValue.createPercentValue(100));
        bienTable.setBackgroundColor(LIGHT_BG);
        bienTable.setBorder(new SolidBorder(PRIMARY_COLOR, 1));
        bienTable.setMarginBottom(10); // Réduit de 15 à 10

        addBienRow(bienTable, "Résidence:", contract.getUnit().getBuilding().getResidence().getName(), boldFont, regularFont);
        addBienRow(bienTable, "Bâtiment:", contract.getUnit().getBuilding().getName(), boldFont, regularFont);
        addBienRow(bienTable, "Unité n°:", contract.getUnit().getUnitNumber(), boldFont, regularFont);
        addBienRow(bienTable, "Type:", getUnitTypeLabel(contract.getUnit().getType()), boldFont, regularFont);
        if(contract.getUnit().getArea() != null ) addBienRow(bienTable, "Surface:", contract.getUnit().getArea() + " m²", boldFont, regularFont);
        addBienRow(bienTable, "Étage:", String.valueOf(contract.getUnit().getFloorNumber()), boldFont, regularFont);

        document.add(bienTable);

        // ==================== DURÉE DU CONTRAT ====================
        Paragraph dureeTitle = new Paragraph("DURÉE DU CONTRAT")
                .setFont(boldFont)
                .setFontSize(10) // Réduit de 11 à 10
                .setFontColor(SECONDARY_COLOR)
                .setMarginBottom(6); // Réduit de 10 à 6
        document.add(dureeTitle);

        Table dureeTable = new Table(2);
        dureeTable.setWidth(UnitValue.createPercentValue(100));
        dureeTable.setBorder(new SolidBorder(PRIMARY_COLOR, 1));
        dureeTable.setMarginBottom(10); // Réduit de 15 à 10

        addDureeRow(dureeTable, "Date d'effet:", contract.getStartDate().format(DATE_FORMATTER_SHORT), boldFont, regularFont);

        String endDateStr = contract.getEndDate() != null
                ? contract.getEndDate().format(DATE_FORMATTER_SHORT)
                : "Indéterminée";
        addDureeRow(dureeTable, "Date d'expiration:", endDateStr, boldFont, regularFont);

        document.add(dureeTable);

        // ==================== CONDITIONS FINANCIÈRES ====================
        Paragraph financieresTitle = new Paragraph("CONDITIONS FINANCIÈRES")
                .setFont(boldFont)
                .setFontSize(10) // Réduit de 11 à 10
                .setFontColor(SECONDARY_COLOR)
                .setMarginBottom(6); // Réduit de 10 à 6
        document.add(financieresTitle);

        Table financieresTable = new Table(2);
        financieresTable.setWidth(UnitValue.createPercentValue(100));
        financieresTable.setBorder(new SolidBorder(PRIMARY_COLOR, 1));
        financieresTable.setMarginBottom(10); // Réduit de 15 à 10

        addFinanciereRow(financieresTable, "Loyer mensuel:",
                String.format("%,.2f MAD", contract.getMonthlyRent()),
                boldFont, regularFont, true);

        if (contract.getDepositAmount() != null && contract.getDepositAmount().doubleValue() > 0) {
            String depositStatus = contract.getDepositPaid() ? " (Payée)" : " (À payer)";
            addFinanciereRow(financieresTable, "Caution de garantie:" + depositStatus,
                    String.format("%,.2f MAD", contract.getDepositAmount()),
                    boldFont, regularFont, false);
        }

        addFinanciereRow(financieresTable, "Fréquence de paiement:",
                getPaymentFrequencyLabel(contract.getPaymentFrequency()),
                boldFont, regularFont, false);

        if (contract.getPaymentDay() != null) {
            addFinanciereRow(financieresTable, "Jour de paiement:",
                    "Le " + contract.getPaymentDay() + " jour"   ,
                    boldFont, regularFont, false);
        }

        document.add(financieresTable);

        // ==================== CLAUSES PARTICULIÈRES ====================
        if (contract.getTerms() != null && !contract.getTerms().isEmpty()) {
            Paragraph clausesTitle = new Paragraph("CLAUSES PARTICULIÈRES")
                    .setFont(boldFont)
                    .setFontSize(10) // Réduit de 11 à 10
                    .setFontColor(SECONDARY_COLOR)
                    .setMarginBottom(6); // Réduit de 10 à 6
            document.add(clausesTitle);

            Table clausesTable = new Table(1);
            clausesTable.setWidth(UnitValue.createPercentValue(100));
            clausesTable.setBackgroundColor(LIGHT_BG);
            clausesTable.setBorder(new SolidBorder(PRIMARY_COLOR, 1));
            clausesTable.setMarginBottom(10); // Réduit de 15 à 10

            Cell clausesCell = new Cell();
            clausesCell.setBorder(Border.NO_BORDER);
            clausesCell.setPadding(8); // Réduit de 12 à 8

            Paragraph clausesText = new Paragraph(contract.getTerms())
                    .setFont(regularFont)
                    .setFontSize(8) // Réduit de 9 à 8
                    .setFontColor(DARK_COLOR)
                    .setTextAlignment(TextAlignment.JUSTIFIED);

            clausesCell.add(clausesText);
            clausesTable.addCell(clausesCell);

            document.add(clausesTable);
        }

        // ==================== SIGNATURES ====================
        Paragraph signaturesTitle = new Paragraph("SIGNATURES")
                .setFont(boldFont)
                .setFontSize(10) // Réduit de 11 à 10
                .setFontColor(SECONDARY_COLOR)
                .setMarginBottom(6); // Réduit de 10 à 6
        document.add(signaturesTitle);

        Table signaturesTable = new Table(2);
        signaturesTable.setWidth(UnitValue.createPercentValue(100));
        signaturesTable.setBorder(Border.NO_BORDER);
        signaturesTable.setMarginTop(5); // Réduit de 10 à 5

        // Signature Bailleur
        Cell signBailleurCell = new Cell();
        signBailleurCell.setBorder(Border.NO_BORDER);
        signBailleurCell.setPaddingRight(10); // Réduit de 15 à 10

        Paragraph signBailleurLabel = new Paragraph("Le Bailleur")
                .setFont(boldFont)
                .setFontSize(9) // Réduit de 10 à 9
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(15); // Réduit de 30 à 15

        Paragraph signBailleurLine = new Paragraph("Signature précédée de la mention \"Lu et approuvé\"")
                .setFont(regularFont)
                .setFontSize(7) // Réduit de 8 à 7
                .setFontColor(GRAY_COLOR)
                .setMarginBottom(3); // Réduit de 5 à 3

        Paragraph signBailleurEmpty = new Paragraph("_________________________")
                .setFont(regularFont)
                .setFontSize(10) // Réduit de 12 à 10
                .setFontColor(GRAY_COLOR);

        signBailleurCell.add(signBailleurLabel);
        signBailleurCell.add(signBailleurLine);
        signBailleurCell.add(signBailleurEmpty);

        // Signature Preneur
        Cell signPreneurCell = new Cell();
        signPreneurCell.setBorder(Border.NO_BORDER);
        signPreneurCell.setPaddingLeft(10); // Réduit de 15 à 10

        Paragraph signPreneurLabel = new Paragraph("Le Preneur")
                .setFont(boldFont)
                .setFontSize(9) // Réduit de 10 à 9
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(15); // Réduit de 30 à 15

        Paragraph signPreneurLine = new Paragraph("Signature précédée de la mention \"Lu et approuvé\"")
                .setFont(regularFont)
                .setFontSize(7) // Réduit de 8 à 7
                .setFontColor(GRAY_COLOR)
                .setMarginBottom(3); // Réduit de 5 à 3

        Paragraph signPreneurEmpty = new Paragraph("_________________________")
                .setFont(regularFont)
                .setFontSize(10) // Réduit de 12 à 10
                .setFontColor(GRAY_COLOR);

        signPreneurCell.add(signPreneurLabel);
        signPreneurCell.add(signPreneurLine);
        signPreneurCell.add(signPreneurEmpty);

        signaturesTable.addCell(signBailleurCell);
        signaturesTable.addCell(signPreneurCell);

        document.add(signaturesTable);

        // ==================== PIED DE PAGE ====================
        Table footerTable = new Table(1);
        footerTable.setWidth(UnitValue.createPercentValue(100));
        footerTable.setBorder(Border.NO_BORDER);
        footerTable.setMarginTop(10); // Réduit de 20 à 10

        Cell footerCell = new Cell();
        footerCell.setBorderTop(new SolidBorder(GRAY_COLOR, 0.5f));
        footerCell.setPaddingTop(6); // Réduit de 10 à 6
        footerCell.setBorder(Border.NO_BORDER);

        Paragraph footerText = new Paragraph("Document généré le " + LocalDate.now().format(DATE_FORMATTER) +
                " - Contrat N° " + contract.getContractNumber())
                .setFont(regularFont)
                .setFontSize(6) // Réduit de 7 à 6
                .setFontColor(GRAY_COLOR)
                .setTextAlignment(TextAlignment.CENTER);

        footerCell.add(footerText);
        footerTable.addCell(footerCell);

        document.add(footerTable);

        document.close();
        log.info("PDF généré avec succès pour le contrat: {}", contractUuid);

        return baos.toByteArray();
    }

    private void addBienRow(Table table, String label, String value, PdfFont boldFont, PdfFont regularFont) {
        Cell labelCell = new Cell();
        labelCell.add(new Paragraph(label).setFont(boldFont).setFontSize(8).setFontColor(DARK_COLOR)); // Réduit de 9 à 8
        labelCell.setBorder(Border.NO_BORDER);
        labelCell.setPadding(6); // Réduit de 8 à 6

        Cell valueCell = new Cell();
        valueCell.add(new Paragraph(value).setFont(regularFont).setFontSize(8).setFontColor(GRAY_COLOR)); // Réduit de 9 à 8
        valueCell.setBorder(Border.NO_BORDER);
        valueCell.setPadding(6); // Réduit de 8 à 6

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addDureeRow(Table table, String label, String value, PdfFont boldFont, PdfFont regularFont) {
        Cell labelCell = new Cell();
        labelCell.add(new Paragraph(label).setFont(boldFont).setFontSize(9).setFontColor(DARK_COLOR)); // Réduit de 10 à 9
        labelCell.setBorder(Border.NO_BORDER);
        labelCell.setPadding(8); // Réduit de 10 à 8

        Cell valueCell = new Cell();
        valueCell.add(new Paragraph(value).setFont(regularFont).setFontSize(9).setFontColor(GRAY_COLOR)); // Réduit de 10 à 9
        valueCell.setBorder(Border.NO_BORDER);
        valueCell.setPadding(8); // Réduit de 10 à 8

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addFinanciereRow(Table table, String label, String value,
                                  PdfFont boldFont, PdfFont regularFont, boolean highlighted) {
        Cell labelCell = new Cell();
        labelCell.add(new Paragraph(label).setFont(boldFont).setFontSize(9).setFontColor(DARK_COLOR)); // Réduit de 10 à 9
        labelCell.setBorder(Border.NO_BORDER);
        labelCell.setPadding(8); // Réduit de 10 à 8
        if (highlighted) {
            labelCell.setBackgroundColor(LIGHT_BG);
        }

        Cell valueCell = new Cell();
        valueCell.add(new Paragraph(value).setFont(boldFont).setFontSize(9).setFontColor(ACCENT_COLOR)); // Réduit de 10 à 9
        valueCell.setBorder(Border.NO_BORDER);
        valueCell.setPadding(8); // Réduit de 10 à 8
        valueCell.setTextAlignment(TextAlignment.RIGHT);
        if (highlighted) {
            valueCell.setBackgroundColor(LIGHT_BG);
        }

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    // Helper methods
    private String getUnitTypeLabel(UnitType type) {
        switch (type) {
            case APARTMENT: return "Appartement";
            case STUDIO: return "Studio";
            case COMMERCIAL: return "Local commercial";
            case PARKING: return "Parking";
            case STORAGE: return "Local de stockage";
            default: return type.name();
        }
    }

    private String getPaymentFrequencyLabel(PaymentFrequency frequency) {
        switch (frequency) {
            case MONTHLY: return "Mensuel";
            case QUARTERLY: return "Trimestriel";
            case SEMI_ANNUAL: return "Semestriel";
            case ANNUAL: return "Annuel";
            default: return frequency.name();
        }
    }
}