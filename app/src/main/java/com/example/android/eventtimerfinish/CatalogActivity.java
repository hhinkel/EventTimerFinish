package com.example.android.eventtimerfinish;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //Identifier for the data loader
    private static final int RIDER_LOADER = 0;

    RiderCursorAdapter mCursorAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        Intent intent = getIntent();
        // Uri currentRiderUri = intent.getData();

        ListView riderListView = findViewById(R.id.list);

        View emptyView = findViewById(R.id.empty_view);
        riderListView.setEmptyView(emptyView);


        mCursorAdapter = new RiderCursorAdapter(this, null);
        riderListView.setAdapter(mCursorAdapter);

        riderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                Uri currentRiderUri = ContentUris.withAppendedId(RiderContract.RiderEntry.CONTENT_URI, id);

                intent.setData(currentRiderUri);

                startActivity(intent);

            }
        });

        getSupportLoaderManager().initLoader(RIDER_LOADER, null, this);
    }

    private void deleteAllRiders() {
        int rowsDeleted = getContentResolver().delete(RiderContract.RiderEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from rider database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
            case R.id.edit_uninstall:
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

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                RiderContract.RiderEntry._ID,
                RiderContract.RiderEntry.COLUMN_RIDER_NUM,
                RiderContract.RiderEntry.COLUMN_DIVISION,
                RiderContract.RiderEntry.COLUMN_RIDER_FINISH,
                RiderContract.RiderEntry.COLUMN_EDIT};

        return new CursorLoader(this, RiderContract.RiderEntry.CONTENT_URI, projection, null, null,null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader loader) {
        mCursorAdapter.swapCursor(null);
    }
}
