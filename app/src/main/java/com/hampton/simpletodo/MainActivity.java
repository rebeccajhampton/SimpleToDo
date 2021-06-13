package com.hampton.simpletodo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_ITEM_TEXT = "item_text";
    public static final String KEY_ITEM_POSITION = "item_position";
    public static final int EDIT_TEXT_CODE = 20;

    List<String> items;

    Button btnAdd;
    EditText etItem;
    RecyclerView rvItems;
    ItemsAdapter itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAdd = findViewById(R.id.btnAdd);
        etItem = findViewById(R.id.etItem);
        rvItems = findViewById(R.id.rvItems);

        loadItems();

        ItemsAdapter.OnLongClickListener onLongClickListener = new ItemsAdapter.OnLongClickListener(){
            @Override
            public void onItemLongClicked(int position) {
            // 1. Delete the item from the model
            items.remove(position);
            // 2. Notify the adapter
            itemsAdapter.notifyItemRemoved(position);
            Toast.makeText(getApplicationContext(), "Item was removed", Toast.LENGTH_SHORT).show();
            saveItems();
            }
        };

        ItemsAdapter.OnClickListener onClickListener = new ItemsAdapter.OnClickListener() {
            @Override
            public void onItemClicked(int position) {
                Log.d("Main Activity", "single click at position" + position);
                // Create the new activity
                Intent i = new Intent(MainActivity.this, EditActivity.class);
                // Pass the data being edited
                i.putExtra(KEY_ITEM_TEXT, items.get(position));
                i.putExtra(KEY_ITEM_POSITION, position);
                // Display the activity
                startActivityForResult(i, EDIT_TEXT_CODE);
            }
        };
        itemsAdapter = new ItemsAdapter(items, onLongClickListener, onClickListener);
        rvItems.setAdapter(itemsAdapter);
        rvItems.setLayoutManager(new LinearLayoutManager(this));

        btnAdd.setOnClickListener(new View.OnClickListener() { // this is entered when someone clicks the button
            @Override
            public void onClick(View v) {
                String todoItem = etItem.getText().toString();
                // 1. Add item to model
                items.add(todoItem);
                // 2. Notify adapter that an item is inserted
                itemsAdapter.notifyItemInserted(items.size()-1);
                etItem.setText("");

                Toast.makeText(getApplicationContext(), "Item was added", Toast.LENGTH_SHORT).show();
                saveItems();
            }
        });
    }

    // handle the result of the edit activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == EDIT_TEXT_CODE) {
            // Retrieve updated text
            String itemText = data.getStringExtra(KEY_ITEM_TEXT);
            // Extract original position of the edited item from the position key
            int position = data.getExtras().getInt(KEY_ITEM_POSITION);
            // Update the model at the right position w new item text
            items.set(position, itemText);
            // Notify the adapter
            itemsAdapter.notifyItemChanged(position);
            // Persist Changes
            saveItems();
            Toast.makeText(getApplicationContext(), "Item updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Log.w("MainActivity", "Unknown call to onActivityResult");
        }
    }

    // Returns the file in which we'll store the list of to do items
    private File getDataFile() {
        return new File(getFilesDir(), "data.txt"); // 1st param: directory of the app; 2nd param: file name
    }

    // Loads items by reading every line of the data file
    private void loadItems() { // only called when the app starts up
        try {
            items = new ArrayList<>(FileUtils.readLines(getDataFile(), Charset.defaultCharset()));
        } catch (IOException e) { // catches input output exceptions
            Log.e("MainActivity", "Error reading items", e);
            items = new ArrayList<>();
        }
    }

    // Saves items by writing them into the data file
    private void saveItems() { // should be called whenever we make a change to the to do items (when adding and when removing)
        try {
            FileUtils.writeLines(getDataFile(), items);
        } catch (IOException e) {
            Log.e("MainActivity", "Error writing items", e);
        }
    }
}