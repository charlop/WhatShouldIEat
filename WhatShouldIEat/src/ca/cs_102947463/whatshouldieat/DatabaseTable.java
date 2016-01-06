package ca.cs_102947463.whatshouldieat;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseTable {
	
	// Database fields
	private DatabaseOpenHelper dbHelper;
	private String[] SEARCH_TABLE_COLUMNS = { DatabaseOpenHelper.COL_WORD, DatabaseOpenHelper.COL_DEFINITION };
	private SQLiteDatabase database;

	public DatabaseTable(Context context) {
		dbHelper = new DatabaseOpenHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public void addRow(String key, String value) {

		ContentValues values = new ContentValues();

		values.put(DatabaseOpenHelper.COL_WORD, key);
		values.put(DatabaseOpenHelper.COL_DEFINITION, value);

		database.insert(DatabaseOpenHelper.TABLE_SEARCH, null, values);
	}


	public ArrayList<String> getAllValues() {
		ArrayList<String> searchContents = new ArrayList<String>();

		Cursor cursor = database.query(DatabaseOpenHelper.TABLE_SEARCH,
				new String[] {DatabaseOpenHelper.COL_DEFINITION}, null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			String val = cursor.getString(0);
			searchContents.add(val);
			cursor.moveToNext();
		}

		cursor.close();
		return searchContents;
	}
	
	public String getKey(String value) {
		// now that the student is created return it ...
		Cursor cursor = database.query(DatabaseOpenHelper.TABLE_SEARCH,
				new String[] {DatabaseOpenHelper.COL_WORD}, DatabaseOpenHelper.COL_DEFINITION + " = "
						+ value, null, null, null, null);

		cursor.moveToFirst();

		String key = cursor.getString(0);
		cursor.close();
		return key;
	}
	
	public boolean resetTable() {
        database.execSQL("DROP TABLE IF EXISTS " + DatabaseOpenHelper.TABLE_SEARCH);
        dbHelper.onCreate(database);
        return true;
	}
}