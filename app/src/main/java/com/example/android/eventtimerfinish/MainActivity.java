package com.example.android.eventtimerfinish;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import org.eclipse.paho.client.mqttv3.MqttException;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        for(int i = 0; i < 13; i++){
            btn[i].setOnClickListener(this);
        }

        setupDivisionSpinner();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
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

        divisionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
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

    public void clearNumber(EditText input){
        int sLen = input.length();

        if(sLen > 0) {
            String selection = input.getText().toString();
            String result = input.getText().toString().replace(selection, "");
            input.setText(result);
            input.setSelection(input.getText().length());
            userInput = null;
        }
    }

    public void goBackAChar(EditText input) {
        int sLen = input.length();

        if(sLen > 0) {
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
        showTimeNumber(input, now);
        Rider rider = saveRiderData(input, finishTime);
        insertRider(rider);
        //TODO: Encrypt data
        MqttHelper mqttHelper = new MqttHelper(context);
        String msg = createMessageString(rider);
        mqttHelper.connect(msg);
    }

    public void showTimeNumber(String number, Calendar now){
        Context context = getApplicationContext();
        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss:SS", Locale.getDefault());
        Date finishTime = now.getTime();
        CharSequence text = "Rider: " + number + " Finish Time: " + format.format(finishTime);
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public Rider saveRiderData (String number, long finishTime){
        int num = Integer.parseInt(number);
        return new Rider(num, division, 99, 0, finishTime, null);
    }

    private void insertRider(Rider rider){

        ContentValues values = new ContentValues();
        values.put(RiderContract.RiderEntry.COLUMN_RIDER_NUM, rider.getRiderNumber());
        values.put(RiderContract.RiderEntry.COLUMN_DIVISION, rider.getDivision());
        values.put(RiderContract.RiderEntry.COLUMN_FENCE_NUM, 99);
        values.put(RiderContract.RiderEntry.COLUMN_RIDER_START, 0);
        values.put(RiderContract.RiderEntry.COLUMN_RIDER_FINISH, rider.getFinishTime());

        Uri newUri = getContentResolver().insert(RiderContract.RiderEntry.CONTENT_URI,values);
        Log.v("MainActivity", newUri + " value of newUri");
    }

    public void showNumberErrorDialog(final Calendar now, final long finishTime){
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

    private String createMessageString (Rider rider) {

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

    private void uninstallApp() {}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_view_all_enteries:
                Intent catalogIntent = new Intent(this, CatalogActivity.class);
                startActivity(catalogIntent);
                return true;
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_uninstall:
                uninstallApp();
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
}
