package com.example.android.eventtimerfinish;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;



public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_RIDER_LOADER = 0;
    private Uri mCurrentRiderUri;
    private EditText mNumberEditText;
    private Spinner mDivisionEditSpinner;
    private String mNumber;
    private String mOldNumber;
    private String mDivision;
    private int mFenceNum;
    private long mStartTime;
    private long mFinishTime;

    private boolean mRiderHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mRiderHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_item);

        Intent intent = getIntent();
        mCurrentRiderUri = intent.getData();

        setTitle(getString(R.string.editor_activity_title_edit_rider));
        getSupportLoaderManager().initLoader(EXISTING_RIDER_LOADER, null, this);

        mNumberEditText = findViewById(R.id.edit_rider_number);
        mDivisionEditSpinner = findViewById(R.id.edit_spinner_division);

        mNumberEditText.setOnTouchListener(mTouchListener);
        mDivisionEditSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    private void setupSpinner() {
        ArrayAdapter divisionSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_division_options, android.R.layout.simple_spinner_item);
        divisionSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mDivisionEditSpinner.setAdapter(divisionSpinnerAdapter);

        mDivisionEditSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    mDivision = selection;
                } else {
                    mDivision = "Division Unknown";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mDivision = "Division Unknown";
            }
        });
    }

    private void saveRider() {
        mNumber = mNumberEditText.getText().toString().trim();

        if (mCurrentRiderUri == null &&
                TextUtils.isEmpty(mNumber)) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(RiderContract.RiderEntry.COLUMN_RIDER_NUM, mNumber);
        values.put(RiderContract.RiderEntry.COLUMN_DIVISION, mDivision);

        if (mCurrentRiderUri == null) {
            Uri newUri = getContentResolver().insert(RiderContract.RiderEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_rider_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_rider_success), Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentRiderUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_rider_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_rider_success), Toast.LENGTH_SHORT).show();
            }
        }
        Context context = getApplicationContext();
        MqttHelper mqttHelper = new MqttHelper(context);
        Rider rider = new Rider(Integer.parseInt(mNumber), mDivision, mFenceNum, mStartTime, mFinishTime);
        String msg = createMessageString(rider);
        mqttHelper.connect(msg);
    }

    private String createMessageString(Rider rider) {
        return rider.toString();
    }

    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentRiderUri == null) {
            MenuItem menuitem = menu.findItem(R.id.action_delete);
            menuitem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveRider();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if(!mRiderHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mRiderHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                RiderContract.RiderEntry._ID,
                RiderContract.RiderEntry.COLUMN_RIDER_NUM,
                RiderContract.RiderEntry.COLUMN_DIVISION,
                RiderContract.RiderEntry.COLUMN_FENCE_NUM,
                RiderContract.RiderEntry.COLUMN_RIDER_START,
                RiderContract.RiderEntry.COLUMN_RIDER_FINISH };

        return new CursorLoader(this,
                mCurrentRiderUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int riderColumnIndex = cursor.getColumnIndex(RiderContract.RiderEntry.COLUMN_RIDER_NUM);
            int divisionColumnIndex = cursor.getColumnIndex(RiderContract.RiderEntry.COLUMN_DIVISION);

            mNumber = cursor.getString(riderColumnIndex);
            String division = cursor.getString(divisionColumnIndex);

            mNumberEditText.setText(mNumber);
            switch (division) {
                case "Advanced":
                    mDivisionEditSpinner.setSelection(0);
                    break;
                case "Intermediate":
                    mDivisionEditSpinner.setSelection(1);
                    break;
                case "Preliminary":
                    mDivisionEditSpinner.setSelection(2);
                    break;
                case "Modified":
                    mDivisionEditSpinner.setSelection(3);
                    break;
                case "Training":
                    mDivisionEditSpinner.setSelection(4);
                    break;
                case "Novice":
                    mDivisionEditSpinner.setSelection(5);
                    break;
                case "Beginner Novice":
                    mDivisionEditSpinner.setSelection(6);
                    break;
                case "Starter":
                    mDivisionEditSpinner.setSelection(7);
                    break;
                case "Division Unknown":
                    mDivisionEditSpinner.setSelection(8);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mNumberEditText.setText("");
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id) {
                if(dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteRider();
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

    private void deleteRider() {
        if (mCurrentRiderUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentRiderUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_rider_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_rider_success), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
}
