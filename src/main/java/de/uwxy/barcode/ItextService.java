/*
 * Copyright (c) 2018.
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License version 3 as published by the Free Software Foundation with
 * the addition of the following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY ITEXT GROUP NV, ITEXT GROUP
 * DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details. You should have received a copy of
 * the GNU Affero General Public License along with this program; if not, see http://www.gnu.org/licenses/
 * or write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA, 02110-1301 USA,
 * or download the license from the following URL: http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions of this program must
 * display Appropriate Legal Notices, as required under Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License, you must retain the
 * producer line in every PDF that is created or manipulated using iText.
 *
 * You can be released from the requirements of the license by purchasing a commercial license.
 * Buying such a license is mandatory as soon as you develop commercial activities involving the
 * iText software without disclosing the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP, serving PDFs on the
 * fly in a web application, shipping iText with a closed source product.
 */

package de.uwxy.barcode;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;


public class ItextService extends IntentService {

    private int result = Activity.RESULT_OK;
    private String name;
    private String arbeitsgang;


    public ItextService() {
        super("ItextService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        name = intent.getStringExtra(getString(R.string.name));
        String kommission = intent.getStringExtra(getString(R.string.kommission));
        arbeitsgang = intent.getStringExtra(getString(R.string.arbeitsgang));
        String filePath = intent.getStringExtra(getString(R.string.filepath));
        Document document = new Document(PageSize.A4, 40, 40, 0, 0);
        //document.setMargins(0f,0f,40,0);

        PdfWriter writer = null;
        try {
            writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));
        } catch (DocumentException | FileNotFoundException e) {
            e.printStackTrace();
        }
        document.open();

        PdfContentByte cb = writer.getDirectContent();
        PdfPTable table = new PdfPTable(1);
        PdfPTable table_2 = new PdfPTable(3);
        try {
            table_2.setWidths(new int[]{1, 8, 1});
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        PdfPCell cell2 = new PdfPCell(Phrase.getInstance(""));
        cell2.setBorder(Rectangle.NO_BORDER);
        cell2.setBorderWidth(0);
        table_2.addCell(cell2);

        PdfPCell cell = createCell(name, createImage(name, cb));
        table.addCell(cell);
        table.addCell(cell);

        cell = createCell(kommission, createImage(kommission, cb));

        table.addCell(cell);

        cell = createCell(arbeitsgang, createImage(arbeitsgang, cb));

        table_2.addCell(cell);
        table_2.addCell(cell2);

        try {
            document.add(table);
            document.add(table_2);
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        document.close();
        publishResults(filePath, result);
    }

    private void publishResults(String outputPath, int result) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplication());
        Intent intent = new Intent(getString(R.string.intent_notification));
        intent.putExtra(getString(R.string.filepath), outputPath);
        intent.putExtra(getString(R.string.result), result);
        localBroadcastManager.sendBroadcast(intent);
    }

    private Image createImage(String text, PdfContentByte cb) {
        Barcode128 code128 = new Barcode128();
        code128.setFont(null);
        code128.setCode(text);
        code128.setCodeType(Barcode128.CODE128);
        return code128.createImageWithBarcode(cb, null, null);
    }

    private PdfPCell createCell(String text, Image image) {
        PdfPCell cell = new PdfPCell(image);
        cell.setBorder(Rectangle.NO_BORDER);
        //cell.setBorderWidth(0);
        if (!text.equals(name)) {
            cell.addElement(new Phrase(" \n\n\n\n\n"));
            cell.addElement(new Phrase("" + text, FontFactory.getFont(FontFactory.HELVETICA, 26, Font.BOLD, BaseColor.BLACK)));
        }
        cell.addElement(image);
        if (text.equals(arbeitsgang)) {
            cell.addElement(new Phrase("           itextpdf.com", FontFactory.getFont(FontFactory.HELVETICA, 28, Font.BOLD, BaseColor.BLACK)));
        }
        return cell;
    }
}
