package itas.shayne.afinal;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Simple example for Lab 7 using SQLite to save a name and comment to a database.
 * croftd: added Firebase Firestore support for Part B of the lab to demonstrate
 * basic usage of Firestore to add and remove data.
 *
 * NOTE: this version does not support name and comment for SQLite - the SQLite code
 * still only supports a single comments column - adding name is Part A of Lab 7.
 */
public class MainActivity extends AppCompatActivity {

    public static String DEBUG_TAG = "itas274final";
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;

    // croftd - here is the cloud database reference
    private FirebaseFirestore db;

    // EDIT HERE FOR NAME
    private String[] allColumns = {
            MySQLiteHelper.COLUMN_ID,
            // ADD NAME HERE
            MySQLiteHelper.COLUMN_COMMENT};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // db stuff
        dbHelper = new MySQLiteHelper(getApplicationContext());
        // make the call to open a writeable database
        open();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Log.d(DEBUG_TAG, "Complete onCreate() method, connection to Firestore is: " + db);
    }

    /**
     * Establish a connection with the database.
     *
     * @throws SQLException
     */
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    /**
     * Method to add a Comment to the DB based upon what the user put in the EditText
     * This method handles inserting into the database.
     */
    public void addComment(View view) {

        // EDIT HERE - First you need to grab the EditText for the Name

        // Next, here we grab the String for the actual comment
        EditText editText = (EditText) findViewById(R.id.commentText);
        String commentText = editText.getText().toString();

        ContentValues values = new ContentValues();

        // Dave: don't hard-code names of columns, reference the variable in MySQLiteHelper
        //values.put("comment", commentTextFromEditText);

        values.put(MySQLiteHelper.COLUMN_COMMENT, commentText);

        // EDIT HERE - do another put statement for name

        long insertId = database.insert(MySQLiteHelper.TABLE_COMMENTS, null,
                values);
        Log.d(DEBUG_TAG, "Inserted new comment with id: " + insertId);

    }

    /**
     * This method demonstrates the equivalent of select * and uses the Cursor object to
     * iterate through all the results and Log them
     */
    public void logComments(View view) {

        Cursor cursor = database.query(MySQLiteHelper.TABLE_COMMENTS,
                allColumns, null, null, null, null, null);

        // move to the start of the result set
        cursor.moveToFirst();
        // keep looping while there are still results
        while (!cursor.isAfterLast()) {

            // get the data from the two columns in the next row
            long id = cursor.getLong(0);

            // EDIT HERE - Grab the name field

            // Next grab the actual comment
            String comment = cursor.getString(1);

            // EDIT HERE - Include the name with the Log message
            Log.d(DEBUG_TAG, "Comment[" + id + "]: " + comment);

            // go to the next row in the results
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
    }

    /**
     * Method to add comments to the Firestore database. Added a time field to be able
     * to sort comments based on a time stamp.
     */
    public void addCommentFirestore(View view) {

        Log.d(DEBUG_TAG, "Trying to add a new comment to Firestore");

        EditText nameText = (EditText) findViewById(R.id.nameText);
        String nameTextS = nameText.getText().toString();

        EditText editText = (EditText) findViewById(R.id.commentText);
        String commentTextS = editText.getText().toString();


        Map<String, Object> doc = new HashMap<>();
        doc.put("name", nameTextS);

        doc.put("comment", commentTextS);

        // DC Example: Simple conversion to pig latin
        // String pigComment = convertPigLatin(commentTextS);
        // doc.put("comment", pigComment);

        Long time = System.currentTimeMillis();
        doc.put("time", time);

        db.collection("comments")
                .add(doc)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(DEBUG_TAG, "Document Snapshot added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(DEBUG_TAG, "Error adding new comment: ", e);
                    }
                });

    }

    /**
     * Method to retrieve name/comment/time from Firestore and display these in a TextView
     * at the bottom of the UI.
     *
     * @param view
     */
    public void showCommentsFirestore(View view) {

        // get all comments, and sort by the time field
        db.collection("comments").orderBy("time", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // counter to show a counter for each comment
                            int i = 1;
                            StringBuffer messages = new StringBuffer();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                int id = i;
                                // we want to format the name and message from
                                // getData() which is a map
                                Map<String, Object> data = document.getData();
                                String name = (String) data.get("name");
                                String comment = (String) data.get("comment");

                                messages.append("ID: " + id + "\t- " + name + ": " + comment + "\n");

                                Log.d(DEBUG_TAG, document.getId() + " => " + document.getData());
                                i++;
                            }

                            TextView messageView = (TextView) findViewById(R.id.messageView);
                            messageView.setText(messages.toString());

                        } else {
                            Log.w(DEBUG_TAG, "Error getting documents from Firestore: ", task.getException());
                        }
                    }
                });
    }



    // croftd: Pig latin example for encrypt from:
    // https://www.javatpoint.com/pig-latin-program-in-java
    //
    // and:
    // https://ispycode.com/Blog/java/2017-02/Piglatin-Translator

    public String convertPigLatin(String comment) {

        StringTokenizer tokenizer = new StringTokenizer(comment);

        String output = "";
        while (tokenizer.hasMoreTokens()) {
            output = output + pigLatinWord(tokenizer.nextToken()) + " ";
        }
        return output;
    }

    /**
     * Private pigLatin conversion method
     */
    private String pigLatinWord(String word) {

        String pig;
        boolean cap = false;

        char first = word.charAt(0);
        if ('A' <= first && first <= 'Z') {
            first = Character.toLowerCase(first);
            cap = true;
        }

        if (first == 'a' || first == 'e' || first == 'i' || first == 'o' || first == 'u') {
            pig = word + "hay";
        } else {
            if (cap) {
                pig = "" + Character.toUpperCase(word.charAt(1));
                pig = pig + word.substring(2) + first + "ay";
            } else {
                pig = word.substring(1) + first + "ay";
            }
        }
        return pig;
    }

    /**
     * croftd: very simple example shifting letters to encrypt and decrypt
     * Using a single number value for a key to determine how much to shift letters
     * You can think of the key as the password you would use
     */
    private void encryptCustom(String input) {

        // key is a number between 1 and 5
        // key is 2
        // shift letters based on the key

        // INPUT:
        // cab1

        // encryption will be as simple as shifting every letter one up
        // so we want
        // ecd3

        // decrypt - just move all the letters back one
        // to decrypt properly - you need to know the key, in this case 2
        // cab1
    }
}