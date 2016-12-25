package com.example.user.myapplication;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//**************************************************
// Class        : ListFileActivity
// Description  : This class display the root and directory of the file
//**************************************************

public class ListFileActivity extends ListActivity
{
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_file);

        // Use the current directory as title
        path = "/";
        if (getIntent().hasExtra("path")) {
            path = getIntent().getStringExtra("path");
        }
        setTitle(path);

        // Read all files sorted into the values-array
        List values = new ArrayList();
        File dir = new File(path);
        if (!dir.canRead()) {
            setTitle(getTitle() + " (inaccessible)");
        }
        String[] list = dir.list();
        if (list != null) {
            for (String file : list) {
                if (!file.startsWith(".")) {
                    values.add(file);
                }
            }
        }
        Collections.sort(values);

        // Put the data into the list
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_2, android.R.id.text1, values);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        String filename = (String) getListAdapter().getItem(position);
        if (path.endsWith(File.separator))
        {
            filename = path + filename;
        }
        else
        {
            filename = path + File.separator + filename;
        }

        if (new File(filename).isDirectory())
        {
            Intent intent = new Intent(this, ListFileActivity.class);
            intent.putExtra("path", filename);
            startActivity(intent);
        }
        else
        {
            //We get a file, Pass the filename (path) to next activity
            Toast.makeText(this, filename + " is not a directory", Toast.LENGTH_LONG).show();

            //Pass the filename back to the main Activity
            Intent intent = new Intent();
            intent.putExtra("FilePath",filename);
            setResult(RESULT_OK,intent);
            finish();
        }
    }
}
