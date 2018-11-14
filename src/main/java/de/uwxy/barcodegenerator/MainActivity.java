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

package de.uwxy.barcodegenerator;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final String PREFS_FILENAME = "BARCODE_PREFS";
    private static final String PREFS_USERNAME = "BARCODE_PREFS_username";
    private static final String PREFS_WORK = "BARCODE_PREFS_work";
    private static final String PREFS_KOMMISSION = "BARCODE_PREFS_kommission";
    public static final String PREFS_FIRST_RUN_FLAG = "BARCODE_PREFS_datenschutz";
    private static final String TAG = "MainActivity";
    public static final String FILE_PATH = "filepath";
    public static final String NOTIFICATION = "de.uwxy.barcode.ACTION_ITEXT";
    public static final String RESULT = "result";
    public static final String NAME = "name";
    public static final String KOMMISSION = "kommission";
    public static final String ARBEITSGANG = "arbeitsgang";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText nameEdit = findViewById(R.id.name);
        final EditText kommissionEdit = findViewById(R.id.kommission);
        final EditText arbeisgangEdit = findViewById(R.id.arbeitsgang);
        final Context context = getApplication();

        final String savedName = getPrefs(context, PREFS_USERNAME);
        final String savedArbeitsgang = getPrefs(context, PREFS_WORK);
        final String savedKommission = getPrefs(context, PREFS_KOMMISSION);

        final String fileName = getResources().getString(R.string.fileName);
        final String filePath = getApplicationContext().getFilesDir().getAbsolutePath() + fileName;

        nameEdit.setText(savedName);
        arbeisgangEdit.setText(savedArbeitsgang);
        kommissionEdit.setText(savedKommission);

        Button btn = findViewById(R.id.doItbutton);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameEdit.getText().toString();
                String kommission = kommissionEdit.getText().toString();
                String arbeitsgang = arbeisgangEdit.getText().toString();

                if (!savedName.equals(name)) {
                    savePrefs(context, PREFS_USERNAME, name);
                }

                if (!savedArbeitsgang.equals(arbeitsgang)) {
                    savePrefs(context, PREFS_WORK, arbeitsgang);
                }

                if (!savedKommission.equals(kommission)) {
                    savePrefs(context, PREFS_KOMMISSION, kommission);
                }

                startItextService(name, kommission, arbeitsgang, filePath);
            }
        });

        if (isFirstRun(context)) {
            startActivity(new Intent(context, ShowFirstActivity.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(NOTIFICATION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    protected void savePrefs(Context context, String key, String text) {
        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE);
        editor = settings.edit();
        editor.putString(key, text);
        editor.apply();
    }

    protected String getPrefs(Context context, String key) {
        SharedPreferences settings;
        String text;
        settings = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE);
        text = settings.getString(key, "");
        return text;
    }

    protected boolean isFirstRun(Context context) {
        SharedPreferences settings;
        boolean isFirstRun;
        settings = context.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE);
        isFirstRun = settings.getBoolean(PREFS_FIRST_RUN_FLAG, true);
        return isFirstRun;
    }

    protected void openPdf(String filepath) {
        Intent intent = new Intent(getApplicationContext(), ShowPdfActivity.class);
        intent.putExtra(FILE_PATH, filepath);
        startActivity(intent);
    }

    protected void startItextService(String name, String kommission, String arbeitsgang, String filePath) {
        Intent intent = new Intent(this, ItextService.class);
        intent.putExtra(NAME, name);
        intent.putExtra(KOMMISSION, kommission);
        intent.putExtra(ARBEITSGANG, arbeitsgang);
        intent.putExtra(FILE_PATH, filePath);
        startService(intent);
    }

    protected final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                if (bundle.getInt(RESULT) == RESULT_OK) {
                    openPdf(intent.getStringExtra(FILE_PATH));
                } else {
                    Toast.makeText(MainActivity.this, "creating PDF failed",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    };
}
