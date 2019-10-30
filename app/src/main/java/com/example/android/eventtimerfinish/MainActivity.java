package com.example.android.eventtimerfinish;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //Global Variables
    Button[] btn = new Button[13];
    EditText userInput;
    ConstraintLayout relativeLayout;

    private Spinner divisionSpinner;
    private String division;

    private static final int PERMISSION_REQUEST_CODE = 100;
    private boolean permissionGranted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            String number = savedInstanceState.getString("number");
            division = savedInstanceState.getString("division");
            userInput = findViewById(R.id.numberEntered);
            userInput.setText(number);
        }

        //Register the buttons
        btn[0] = findViewById(R.id.button0);
        btn[1] = findViewById(R.id.button1);
        btn[2] = findViewById(R.id.button2);
        btn[3] = findViewById(R.id.button3);
        btn[4] = findViewById(R.id.button4);
        btn[5] = findViewById(R.id.button5);
        btn[6] = findViewById(R.id.button6);
        btn[7] = findViewById(R.id.button7);
        btn[8] = findViewById(R.id.button8);
        btn[9] = findViewById(R.id.button9);
        btn[10] = findViewById(R.id.buttonBack);
        btn[11] = findViewById(R.id.buttonClear);
        btn[12] = findViewById(R.id.buttonFinish);

        divisionSpinner = findViewById(R.id.spinner_division);
        relativeLayout = findViewById(R.id.relative_layout);

        //Setup on click listener
        for (int i = 0; i < 13; i++) {
            btn[i].setOnClickListener(this);
        }

        setupDivisionSpinner();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button0:
                addToArray("0");
                break;
            case R.id.button1:
                addToArray("1");
                break;
            case R.id.button2:
                addToArray("2");
                break;
            case R.id.button3:
                addToArray("3");
                break;
            case R.id.button4:
                addToArray("4");
                break;
            case R.id.button5:
                addToArray("5");
                break;
            case R.id.button6:
                addToArray("6");
                break;
            case R.id.button7:
                addToArray("7");
                break;
            case R.id.button8:
                addToArray("8");
                break;
            case R.id.button9:
                addToArray("9");
                break;
            case R.id.buttonClear:
                clearNumber(userInput);
                break;
            case R.id.buttonFinish:
                try {
                    enterNumber(userInput);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.buttonBack:
                goBackAChar(userInput);
                break;
        }
    }

    private void setupDivisionSpinner() {
        ArrayAdapter divisionSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_division_options, android.R.layout.simple_spinner_item);

        divisionSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        divisionSpinner.setAdapter(divisionSpinnerAdapter);

        divisionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    division = selection;
                } else {
                    division = "Division Unknown";
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
                division = "Division Unknown";
            }
        });
    }

    public void addToArray(String number) {
        userInput = findViewById(R.id.numberEntered);
        userInput.append(number);
    }

    public void clearNumber(EditText input) {
        int sLen = input.length();

        if (sLen > 0) {
            String selection = input.getText().toString();
            String result = input.getText().toString().replace(selection, "");
            input.setText(result);
            input.setSelection(input.getText().length());
            userInput = null;
        }
    }

    public void goBackAChar(EditText input) {
        int sLen = input.length();

        if (sLen > 0) {
            String selection = input.getText().toString().substring(sLen - 1, sLen);
            String result = input.getText().toString().replace(selection, "");
            input.setText(result);
            input.setSelection(input.getText().length());
            userInput = input;
        }
    }

    public void enterNumber(EditText input) throws MqttException, UnsupportedEncodingException {
        Calendar now = Calendar.getInstance();
        long finishTime = now.getTimeInMillis();

        if (input == null) {
            showNumberErrorDialog(now, finishTime);
        } else {
            processNumber(input.getText().toString(), now, finishTime);
            clearNumber(userInput);
        }
    }

    private void processNumber(String input, Calendar now, long finishTime) {
        Context context = getApplicationContext();
        showTimeNumber(context, input, now);
        Rider rider = saveRiderData(input, finishTime);
        insertRider(rider);
        //TODO: Encrypt data
        MqttHelper mqttHelper = new MqttHelper(context);
        String msg = createMessageString(rider);
        mqttHelper.connect(msg);
    }

    public void showTimeNumber(Context context, String number, Calendar now) {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss:SS", Locale.getDefault());
        Date finishTime = now.getTime();
        CharSequence text = "Rider: " + number + " Finish Time: " + format.format(finishTime);
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public Rider saveRiderData(String number, long finishTime) {
        int num = Integer.parseInt(number);
        return new Rider(num, division, 99, 0, finishTime, null);
    }

    private void insertRider(Rider rider) {

        ContentValues values = new ContentValues();
        values.put(RiderContract.RiderEntry.COLUMN_RIDER_NUM, rider.getRiderNumber());
        values.put(RiderContract.RiderEntry.COLUMN_DIVISION, rider.getDivision());
        values.put(RiderContract.RiderEntry.COLUMN_FENCE_NUM, 99);
        values.put(RiderContract.RiderEntry.COLUMN_RIDER_START, 0);
        values.put(RiderContract.RiderEntry.COLUMN_RIDER_FINISH, rider.getFinishTime());

        Uri newUri = getContentResolver().insert(RiderContract.RiderEntry.CONTENT_URI, values);
        Log.v("MainActivity", newUri + " value of newUri");
    }

    public void showNumberErrorDialog(final Calendar now, final long finishTime) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.layout_popup, null);
        builder.setView(dialogView);

        userInput = dialogView.findViewById(R.id.add_number);

        builder.setTitle("Please enter the Rider Number");
        builder.setMessage("Enter Number");
        builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                processNumber(userInput.getText().toString(), now, finishTime);
                clearNumber(userInput);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        showSoftNumPad(dialogView);
    }

    public void showSoftNumPad(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private String createMessageString(Rider rider) {

        return rider.getRiderNumber() + "," + rider.getDivision() + "," + rider.getFenceNumber()
                + "," + rider.getStartTime() + "," + rider.getFinishTime() + "," + rider.getEdit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void deleteAllRiders() {
        int rowsDeleted = getContentResolver().delete(RiderContract.RiderEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + " rows deleted from rider database");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_view_all_enteries:
                Intent catalogIntent = new Intent(this, CatalogActivity.class);
                startActivity(catalogIntent);
                return true;
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_download_to_computer:
                showExportConfirmationDialog();
                return true;
            case R.id.action_uninstall:
                showUnistallConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteAllRiders();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null)
                    dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showExportConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.export_all_msg);
        builder.setPositiveButton(R.string.export, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                exportData();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null)
                    dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void exportData() {

        RiderDbHelper dbHelper = new RiderDbHelper(getApplicationContext());

        String state = Environment.getExternalStorageState();
        String external = Environment.getExternalStorageDirectory().toString();
        String fileName = "99" + RiderDbHelper.DATABASE + ".csv";

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (checkPermission()) {
                    fileAsCSV(external, "CrossCountryScoring", fileName, dbHelper);
                } else {
                    requestPermission();
                }
            } else {
                fileAsCSV(external, "CrossCountryScoring", fileName, dbHelper);
            }
        }
    }

    private void fileAsCSV(String rootPath, String newFolder, String fileName, RiderDbHelper dbHelper) {

        File path = checkForDir(rootPath, newFolder);

        File csvFile = new File(path, fileName);
        if (!csvFile.exists()) {
            createCSVFile(dbHelper, csvFile);
        } else {
            if(csvFile.lastModified() < Calendar.DATE) {
                csvFile.delete();
                createCSVFile(dbHelper, csvFile);
            } else {
                showFileDeleteErrorDialog();
            }
        }
    }

    private File checkForDir(String rootPath, String addPath) {
        File newPath = new File(rootPath, addPath);
        if (!newPath.exists()) {
            newPath.mkdirs();
        }
        return newPath;
    }

    private void createCSVFile (RiderDbHelper dbHelper, File file) {
        Log.d("MainActivity.file", file.toString());
        try {
            FileOutputStream output = new FileOutputStream(file);
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            writeCSVFile(dbHelper, file, csvWrite);
            output.flush();
            output.close();
            csvWrite.close();
        } catch (Exception ex) {
            Log.e("MainActivity.file", ex.getMessage(), ex);
        }
    }

    private void writeCSVFile(RiderDbHelper dbHelper, File file, CSVWriter csvWrite) {

        try {
            file.createNewFile();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM "
            + RiderContract.RiderEntry.TABLE_NAME, null);
            csvWrite.writeNext(cursor.getColumnNames());
            while (cursor.moveToNext()) {
                String[] columnArray = {cursor.getString(0), cursor.getString(1),
                        cursor.getString(2), cursor.getString(3), cursor.getString(4),
                        cursor.getString(5), cursor.getString(6)};
                csvWrite.writeNext(columnArray);
            }
            cursor.close();
        } catch (Exception ex) {
            Log.e("MainActivity.csv", ex.getMessage(), ex);
        }
    }

    private void showUnistallConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.uninstall_msg);
        builder.setPositiveButton(R.string.uninstall, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                uninstallApp();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null)
                    dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void uninstallApp() {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:com.example.android.eventtimerfinish"));
        startActivity(intent);
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(MainActivity.this, "Write External Storage permission allows us to save files. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = true;
                } else {
                    permissionGranted = false;
                }
                break;
        }
    }

    public void showFileDeleteErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.cannot_delete_file_msg);
        builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null)
                    dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (userInput != null) {
            outState.putString("number", userInput.getText().toString());
            outState.putString("division", division);
        }
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
}
