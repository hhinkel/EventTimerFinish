package com.example.android.eventtimerfinish;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //Global Variables
    Button[] btn = new Button[13];
    EditText userInput;

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
        btn[12] = findViewById(R.id.buttonStart);

        //Setup on click listener
        for(int i = 0; i < 13; i++){
            btn[i].setOnClickListener(this);
        }
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
            case R.id.buttonStart:
                try {
                    enterNumber(userInput);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                clearNumber(userInput);
                break;
            case R.id.buttonBack:
                goBackAChar(userInput);
                break;
        }
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
            userInput = input;
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
        long startTime = now.getTimeInMillis();
        Context context = getApplicationContext();
        if(input.length() > 0) {

            showTimeNumber(input.getText().toString(), now);
            Rider rider = saveRiderData(input.getText().toString(), startTime);
            insertRider(rider);
            //TODO: Encrypt data
            MqttHelper mqttHelper = new MqttHelper(context);
            String msg = createMessageString(rider);
            mqttHelper.connect(mqttHelper, msg);
        } else {
            numberError();
        }
    }

    public void showTimeNumber(String number, Calendar now){
        Context context = getApplicationContext();
        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss:SS", Locale.getDefault());
        Date startTime = now.getTime();
        CharSequence text = "Rider: " + number + " Start Time: " + format.format(startTime);
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public Rider saveRiderData (String number, long startTime){
        int num = Integer.parseInt(number);
        return new Rider(num, 0, startTime, 0);
    }

    private void insertRider(Rider rider){

        //RiderDbHelper mDbHelper = new RiderDbHelper(this);
        //SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RiderContract.RiderEntry.COLUMN_RIDER_NUM, rider.getRiderNumber());
        values.put(RiderContract.RiderEntry.COLUMN_FENCE_NUM, 0);
        values.put(RiderContract.RiderEntry.COLUMN_RIDER_START, rider.getStartTime());
        values.put(RiderContract.RiderEntry.COLUMN_RIDER_FINISH, 0);

        //long newRowId = db.insert(RiderContract.RiderEntry.TABLE_NAME, null, values);
        //if(newRowId == -1) {
        //    Toast.makeText(this, "Error Saving Rider Data", Toast.LENGTH_SHORT).show();
        //}

        Uri newUri = getContentResolver().insert(RiderContract.RiderEntry.CONTENT_URI,values);
        Log.v("MainActivity", newUri + " value of newUri");
        //    Toast.makeText(this,"Value of newUri: " + newUri, Toast.LENGTH_SHORT).show();
    }

    public void numberError(){
        // this does not work I am going to have to do this another way.
        // pop up a window to enter a rider number using the internal number pad
        Context context = getApplicationContext();
        CharSequence text = "Please enter a rider number";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private String createMessageString (Rider rider) {
        return rider.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void deleteAllRides() {
        int rowsDeleted = getContentResolver().delete(RiderContract.RiderEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from rider database");
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
                deleteAllRides();
                return true;
            case R.id.action_uninstall:
                uninstallApp();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
