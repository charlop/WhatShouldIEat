package ca.cs_102947463.whatshouldieat;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "search.db";
	private static final int DATABASE_VERSION = 3;
	
	// Database table
	public static final String TABLE_SEARCH = "SEARCH";
	
	//The columns we'll include in the dictionary table
	public static final String COL_WORD = "KEY";
	public static final String COL_DEFINITION = "VALUE";
	
	public static String RESULT_TITLE;
	public static String RESULT_IMAGE_URL;
	public static boolean RESULT_FOUND;
	
	private static final String DATABASE_CREATE =
	        "CREATE TABLE " + TABLE_SEARCH +
	        "( " + COL_WORD + " text, " +
	        COL_DEFINITION + ", text);";

    DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEARCH);
        onCreate(db);
    }
}