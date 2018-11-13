package de.uwxy.barcodegenerator;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class ShowPdfActivity extends AppCompatActivity {

    private ImageView pdfView;
    private String targetPdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_pdf);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            targetPdf = extras.getString(MainActivity.FILE_PATH);

        }
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        //myAction
        pdfView = findViewById(R.id.pdfview2);
        try {
            openPDF();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Something Wrong: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        //myAction
    }
    private void openPDF() throws IOException {
        File file = new File(targetPdf);

        ParcelFileDescriptor fileDescriptor;
        fileDescriptor = ParcelFileDescriptor.open(
                file, ParcelFileDescriptor.MODE_READ_ONLY);

        //min. API Level 21
        PdfRenderer pdfRenderer;
        pdfRenderer = new PdfRenderer(fileDescriptor);

        //Display page 0
        PdfRenderer.Page rendererPage = pdfRenderer.openPage(0);
        int rendererPageWidth = rendererPage.getWidth();
        int rendererPageHeight = rendererPage.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(
                rendererPageWidth,
                rendererPageHeight,
                Bitmap.Config.ARGB_8888);
        rendererPage.render(bitmap, null, null,
                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        pdfView.setImageBitmap(bitmap);
        rendererPage.close();

        pdfRenderer.close();
        fileDescriptor.close();
    }

}
