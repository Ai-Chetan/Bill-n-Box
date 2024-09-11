package com.example.billnbox;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Font;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.PdfPTable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CreatePDF {

    private static final String PDF_FILEPATH = "C:/Users/Kishor/IdeaProjects/billnbox/Generated PDFs/";
    private static final String PDF_NAME = "Bill!.pdf";

    public static void main(String[] args) {

        System.out.println("Generating PDF");
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(new File(PDF_FILEPATH + PDF_NAME)));
            document.open();

            // Add company information
            Paragraph companyInfo = new Paragraph("Company Name\nAddress Line 1\nAddress Line 2\nContact: (123) 456-7890\n\n",
                    new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD));
            companyInfo.setAlignment(Element.ALIGN_CENTER);
            document.add(companyInfo);

            // Add receipt title
            Paragraph title = new Paragraph("Receipt", new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Add date
            Paragraph date = new Paragraph("Date: " + java.time.LocalDate.now() + "\n\n",
                    new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL));
            document.add(date);

            // Add customer information
            Paragraph customerInfo = new Paragraph("Customer Name: John Doe\nAddress: 123 Customer St\nContact: (987) 654-3210\n\n",
                    new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL));
            document.add(customerInfo);

            // Add itemized list
            PdfPTable table = new PdfPTable(3);
            table.addCell("Item");
            table.addCell("Quantity");
            table.addCell("Price");

            // Example items
            table.addCell("Product 1");
            table.addCell("2");
            table.addCell("$10.00");

            table.addCell("Product 2");
            table.addCell("1");
            table.addCell("$15.00");

            table.addCell("Product 3");
            table.addCell("3");
            table.addCell("$5.00");

            // Add table to document
            document.add(table);

            // Add total amount
            Paragraph total = new Paragraph("Total Amount: $40.00\n\n",
                    new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD));
            document.add(total);

            // Add footer
            Paragraph footer = new Paragraph("Thank you for your purchase!\nPlease visit us again.",
                    new Font(Font.FontFamily.HELVETICA, 12, Font.ITALIC));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            System.out.println("Successfully Generated PDF");
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        } finally {
            document.close();
        }
    }
}
