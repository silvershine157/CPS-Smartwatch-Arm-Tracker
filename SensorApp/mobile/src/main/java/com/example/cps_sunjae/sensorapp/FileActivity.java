package com.example.cps_sunjae.sensorapp;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class FileActivity extends AppCompatActivity {

    private final static String TAG = "FILE_ACTIVITY";
    private ListView listView;
    private ArrayList<File> data;
    private ListAdapter listAdapter;

    private static final File download = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    private File current = download;

    private ArrayList<File> selectedFiles = new ArrayList<>();

    private boolean isStackFromBottom = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);

        listView = findViewById(R.id.listView);
        data = new ArrayList<>();
        listAdapter = new ListAdapter(data, R.layout.list_item);
        listView.setAdapter(listAdapter);

        File[] files = download.listFiles();
        for(File f:files) {
            data.add(f);
        }
        listAdapter.notifyDataSetChanged();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (selectedFiles.isEmpty()) {
                    onItemClickNormal(position);
                } else {
                    onItemClickLong(v, position);
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                onItemClickLong(v, position);
                return true;
            }
        });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_swap:
                Collections.reverse(data);
                listAdapter.notifyDataSetChanged();
                selectedFiles.clear();
                return true;

            case R.id.action_delete:
                boolean isDelete;
                for(File f:selectedFiles) {
                    data.remove(f);
                    isDelete = deleteRecursive(f);
                    Log.d(TAG, "isDeleted = " + isDelete);
                }
                selectedFiles.clear();
                listAdapter.notifyDataSetChanged();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean deleteRecursive(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                if (!deleteRecursive(f)) {
                    return false;
                }
            }
        }
        return file.delete();
    }

    private void onItemClickNormal(int position) {
        current = data.get(position);
        if (current.isDirectory()) {
            dataChange(current);
        } else {
            current = current.getParentFile();
        }
    }

    private void onItemClickLong(View v, int position) {
        File f = listAdapter.getItem(position);
        if (selectedFiles.contains(f)) {
            v.setBackgroundColor(0);
            selectedFiles.remove(f);
        } else {
            v.setBackgroundResource(R.color.colorTangerine);
            selectedFiles.add(f);
        }
    }

    private void dataChange(File currentDir) {
        File[] files = currentDir.listFiles();
        data.clear();
        //data.add(current);
        for (File f:files) {
            data.add(f);
        }
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if (current.getAbsolutePath().equals(download.getAbsolutePath())) {
            super.onBackPressed();
        } else if (!selectedFiles.isEmpty()) {
            selectedFiles.clear();
        } else {
            dataChange(current.getParentFile());
            current = current.getParentFile();
        }
    }

    private class ListAdapter extends BaseAdapter {
        private ArrayList<File> mData;
        private int mLayout;

        public ListAdapter(ArrayList<File> data, int layout) {
            mData = data;
            mLayout = layout;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public File getItem(int position) {
            return mData.get(position);
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(mLayout, parent, false);
            }
            ((TextView) convertView.findViewById(R.id.fileName)).setText(getItem(position).getName());
            ImageView imageView = convertView.findViewById(R.id.imageView);
            convertView.setBackgroundColor(0);
            if (getItem(position).isDirectory()) {
                imageView.setImageResource(R.drawable.folder);
            } else {
                imageView.setImageResource(R.drawable.file);
            }
            return convertView;
        }
    }
}
