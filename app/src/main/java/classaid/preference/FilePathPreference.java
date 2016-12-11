package classaid.preference;

import android.content.Context;
import android.os.Environment;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import classaid.activity.R;

/**
 * Created by Vincent on 10/12/2016.
 */

public class FilePathPreference extends DialogPreference {

    private File currentDir;

    private class FileAdapter extends ArrayAdapter<File> {

        public FileAdapter(Context context, int textViewResourceId, List<File> objects) {
            super(context, textViewResourceId, objects);
            mInflater = LayoutInflater.from(context);
        }

        private LayoutInflater mInflater = null;


        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textview = null;
            if(convertView != null) {
                textview = (TextView) convertView;
            } else {
                textview = (TextView) mInflater.inflate(R.layout.textview_list_item, null);
            }

            File item = getItem(position);
            if(item.isDirectory()) {
                textview.setText("Dossier : " + item.getName());
            }
            else {
                textview.setText(item.getName());
            }

            return textview;
        }

    }

    private FileAdapter files;
    private View view;
    private String initialValue;


    public FilePathPreference(Context con, AttributeSet attrs) {
        super(con, attrs);

        setDialogLayoutResource(R.layout.filepathpreference_layout);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
        setDialogIcon(null);
    }

    @Override
    public void onBindDialogView(View v)
    {
        super.onBindDialogView(v);
        view = v;
        TextView path_textview = (TextView) view.findViewById(R.id.path_textview);
        path_textview.setText(initialValue);
        ListView list = (ListView) v.findViewById(R.id.file_listview);
        currentDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        List<File> fileList = new ArrayList<File>();
        for(File f : currentDir.listFiles())
        {
            fileList.add(f);
        }
        files = new FileAdapter(v.getContext(), R.layout.textview_list_item, fileList);
        list.setAdapter(files);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View item_view, int position, long id) {
                File f = files.getItem(position);
                if(f.isDirectory()) {
                    cd(f);
                }
                TextView pathview = (TextView) view.findViewById(R.id.path_textview);
                pathview.setText(f.getAbsolutePath());
            }
        });

        Button up = (Button) view.findViewById(R.id.up_button);
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cd(currentDir.getParentFile());
            }
        });
    }

    public void cd(File dir) {
        if(dir == null || !dir.exists()) {
            return;
        }

        currentDir = dir;
        files.clear();

        File[] fichiers = dir.listFiles();

        if(fichiers != null) {
            for(File f : fichiers) {
                files.add(f);
            }
        }

    }


    @Override
    protected void onDialogClosed(boolean positiveResult) {
        TextView path_textview = (TextView) view.findViewById(R.id.path_textview);
        String path = path_textview.getText().toString();
        if (positiveResult) {
            persistString(path);
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            initialValue = this.getPersistedString("");
        } else {
            // Set default state from the XML attribute
            initialValue = (String) defaultValue;
            persistString(initialValue);
        }
    }


}
