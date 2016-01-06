package ca.cs_102947463.whatshouldieat;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SearchView;

public class SearchActivity extends Activity implements View.OnClickListener, SearchView.OnQueryTextListener {
	public static final String[] SEARCH_DATABASE_URL = {"http://charlop.myweb.cs.uwindsor.ca/420/remote_search"};
	public static final String[] POPULAR_LIST_URL = {"http://charlop.myweb.cs.uwindsor.ca/420/remote_popular"};
	
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] menu_titles;
    
    private SearchView mSearchView;
    private String[] mSearchArray;
    private ListView mSearchListView;
    
    private Button popularButton1, popularButton2, popularButton3;
    
    TreeMap<String, String> popularMap, searchMap;
    private DatabaseTable db;
    private static String itemSelectedValue, itemSelectedKey;
    private static Bitmap resultImage;
    public static String resultImageUrl;
    
    ArrayAdapter<String> searchAdapter;
    
	public void onClick(View view) {
		if(view.getId() == R.id.searchSuggestion1 || view.getId() == R.id.searchSuggestion2 ||
				view.getId() == R.id.searchSuggestion3)
		{
		    resultImage = null;
			Button chosenButton = (Button) view;
			itemSelectedKey = chosenButton.getText().toString();
	        itemSelectedValue = searchMap.get(itemSelectedKey);
			loadResult();
		}
		else if(view.getId() == R.id.like || view.getId() == R.id.dislike)
		{
		    resultImage = null;
			Random r = new Random();
			
			itemSelectedKey = mSearchArray[r.nextInt(mSearchArray.length)];
			itemSelectedValue = searchMap.get(itemSelectedKey);
			
			loadResult();
		} else if(view.getId() == R.id.more_info) {
           Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
           intent.putExtra(SearchManager.QUERY, itemSelectedKey);
           // catch event that there's no activity to handle intent
           if (intent.resolveActivity(getPackageManager()) != null) {
               startActivity(intent);
           } else {
               Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
           }
		}

	}
	
	protected void loadResult()
	{

        DatabaseOpenHelper.RESULT_IMAGE_URL = itemSelectedValue;
        
	    LoadRemoteImageAsync imageTask = new LoadRemoteImageAsync();
	    resultImageUrl = DatabaseOpenHelper.RESULT_IMAGE_URL;
        
        Fragment fragment = new ResultFragment();
        
        imageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[] {null});
        
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
	}
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
        Fragment fragment = new SearchFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        
        db = new DatabaseTable(this);
        db.open();
		
		LoadSearchDatabaseAsync task = new LoadSearchDatabaseAsync();
		task.execute(SEARCH_DATABASE_URL);
		
    	mTitle = mDrawerTitle = getTitle();
    	menu_titles = getResources().getStringArray(R.array.menu_array);
    	mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    	mDrawerList = (ListView) findViewById(R.id.left_drawer);
    	mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
    	mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, menu_titles));
    	mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
		LoadSearchDatabaseAsync popular_task = new LoadSearchDatabaseAsync();
		popular_task.execute(POPULAR_LIST_URL);
	}
	protected void onResume()
	{
		db.open();
		super.onResume();
	}
	protected void onPause()
	{
		db.close();
		super.onPause();
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        return super.onPrepareOptionsMenu(menu);
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
       if (mDrawerToggle.onOptionsItemSelected(item)) {
           return true;
       }
       // Handle action buttons
       switch(item.getItemId()) {
       case R.id.action_websearch:
           // create intent to perform web search for this planet

           return true;
       default:
           return super.onOptionsItemSelected(item);
       }
   }
   public DrawerItemClickListener getDrawerItemClickListener()
   {
	   return new DrawerItemClickListener();
   }
    
    /* The click listner for ListView in the navigation drawer */
   private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
   }
   private void selectItem(int position) {
        // update the main content by replacing fragments
    	Intent intent;
    	switch(position)
    	{
    	case 0: mDrawerLayout.closeDrawer(mDrawerList);
    			if(DatabaseOpenHelper.RESULT_FOUND)
    			{
    				DatabaseOpenHelper.RESULT_FOUND = false;
    				recreate();
    			}
    			intent = null; 
        		break;
    	case 1: intent = new Intent(this, AboutActivity.class); break;
    	case 2: intent = new Intent(this, ChangePreferencesActivity.class); break; 
    	case 3: intent = new Intent(this, SuggestNewActivity.class); break; 
    	case 4: intent = new Intent(this, ResetActivity.class); break;
    	default: intent = null; 
        		mDrawerLayout.closeDrawer(mDrawerList);
        		break;
    	}

    	if(intent != null) startActivity(intent);
    }
   
   /* The click listener for ListView in the navigation drawer */
   @Override
   public void setTitle(CharSequence title) {
       mTitle = title;
       getActionBar().setTitle(mTitle);
   }

   @Override
   protected void onPostCreate(Bundle savedInstanceState) {
       super.onPostCreate(savedInstanceState);
       mSearchView = (SearchView) findViewById(R.id.main_search_view);
       mSearchView.setOnQueryTextListener(this);
       
       mSearchListView = (ListView) findViewById(R.id.mainSearchResults);
       mSearchListView.setTextFilterEnabled(true);

       // Sync the toggle state after onRestoreInstanceState has occurred.
       mDrawerToggle.syncState();
       mDrawerLayout.closeDrawer(mDrawerList);
   }

   @Override
   public void onConfigurationChanged(Configuration newConfig) {
       super.onConfigurationChanged(newConfig);
       
       // Checks the orientation of the screen
       if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
    	   if(DatabaseOpenHelper.RESULT_FOUND)
    	   {
				Fragment fragment = new ResultFragment();
				FragmentManager fragmentManager = getFragmentManager();
				fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    	   }
       } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
    	   if(DatabaseOpenHelper.RESULT_FOUND)
    	   {
				Fragment fragment = new ResultFragment();
				FragmentManager fragmentManager = getFragmentManager();
				fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    	   }
       }
       
       // Pass any configuration change to the drawer toggls
       mDrawerToggle.onConfigurationChanged(newConfig);
   }
   @Override
   public boolean onQueryTextChange(String newText)
   {
       if (TextUtils.isEmpty(newText)) {
           mSearchListView.clearTextFilter();
       } else {
           mSearchListView.setFilterText(newText.toString());
       }
       return true;
   }
   public boolean onQueryTextSubmit(String query) {
       return false;
   }
   
   public void populateSearchList()
   {
	   int i = 0;
	   mSearchArray = new String[searchMap.size()];
	   for(String key : searchMap.keySet())
	   {
		   	mSearchArray[i++] = key;
	   		db.addRow(searchMap.get(key), key);
	   }
	   
	   searchAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mSearchArray);
       mSearchListView.setAdapter(searchAdapter);
       
       mSearchListView.setOnItemClickListener(new OnItemClickListener(){
    	   @Override
    	   public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
    		   itemSelectedKey = searchAdapter.getItem(position);
    		   
    	        itemSelectedValue = searchMap.get(itemSelectedKey);
    	        DatabaseOpenHelper.RESULT_IMAGE_URL = itemSelectedValue;
    	        
    	        Fragment fragment = new ResultFragment();
    	        FragmentManager fragmentManager = getFragmentManager();
    	        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
           }
       });
   }
   public void initializeButtons()
   {
		popularButton1 = (Button) findViewById(R.id.searchSuggestion1);
		popularButton1.setOnClickListener(this);
		popularButton2 = (Button) findViewById(R.id.searchSuggestion2);
		popularButton2.setOnClickListener(this);
		popularButton3 = (Button) findViewById(R.id.searchSuggestion3);
		popularButton3.setOnClickListener(this);
		
		String[] buttonText = new String[3];
		int i = 0;
		for(String key : popularMap.keySet())
		{
			if(i==3) break;		//just in case. should not be happening
			buttonText[i++] = key;
		}

		popularButton1.setText(buttonText[0]);
		popularButton2.setText(buttonText[1]);
		popularButton3.setText(buttonText[2]);   
   }

   public static class SearchFragment extends Fragment 
   {
	   public SearchFragment() {}
	   
	   @Override
	   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	   {
		   View rootView = inflater.inflate(R.layout.search_layout, container, false);
		   DatabaseOpenHelper.RESULT_FOUND = false;
		   return rootView;
	   }
   }
   public static class ResultFragment extends Fragment
   {
	   public ResultFragment() {}
	   
	   @Override
	   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	   {
		   	View rootView = inflater.inflate(R.layout.search_selection, container, false);
			((TextView) rootView.findViewById(R.id.resultTitleText)).setText(itemSelectedKey);
			if(resultImage != null)
			{
				((ImageView) rootView.findViewById(R.id.suggestionImage)).setImageBitmap(resultImage);
			}
			DatabaseOpenHelper.RESULT_FOUND = true;
			return rootView;
	   }
   }
   private class LoadRemoteImageAsync extends AsyncTask<Void, Void, Bitmap>
   {
	   private ProgressDialog dialog = new ProgressDialog(SearchActivity.this);
	   
	    @Override
	    protected void onPreExecute() {
	    	if(isOnline())
	    	{
	    		this.dialog.setMessage("Please wait");
	    		this.dialog.show();
	    	}
	    	else {
	    		Toast.makeText(getApplicationContext(), "No Internet Connection...", Toast.LENGTH_LONG).show();
	    	}
	    }
	    
	   protected Bitmap doInBackground(Void... v)
	   {
		   if(isOnline())
		   {
				try {
					URL resultUrl = new URL(resultImageUrl);
			        final URLConnection conn = resultUrl.openConnection();
					conn.connect();
			        BufferedInputStream bis;
					bis = new BufferedInputStream(conn.getInputStream());
			        final Bitmap bm = BitmapFactory.decodeStream(bis);
					bis.close();
			        return bm;
				} catch (MalformedURLException e) {
					return null;
				} catch (IOException e) {
					return null;
				}
		   } else {
			   Toast.makeText(getApplicationContext(), "No Internet Connection...", Toast.LENGTH_LONG).show();
			   return null;
		   }
	   }
	   protected void onPostExecute(Bitmap result)
	   {
			resultImage = result;
			
	        if (dialog.isShowing()) {
	            dialog.dismiss();
	        }
	        if(result != null)
	        	((ImageView) findViewById(R.id.suggestionImage)).setImageBitmap(result);
	   }
	   
   }
   private class LoadSearchDatabaseAsync extends AsyncTask<String, Void, String>
   {
	   @Override
	   protected String doInBackground(String... urls)
	   {
		   if(urls[0].equals(POPULAR_LIST_URL[0]))
		   {
			   popularMap = new TreeMap<String, String>();
		   }
		   else {
			   searchMap = new TreeMap<String, String>();
		   }
		   if(isOnline())
		   {
			   try
			   {
				   URL url = new URL(urls[0]);
				   BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
				   String str;

					   	if(urls[0].equals(SEARCH_DATABASE_URL[0]))
					   	{
					   		while((str = br.readLine()) != null)
					   		{
							   	String[] splitStr = str.split(",");
							   	
							   	if(splitStr.length != 2)
							   		break;
							
							   	searchMap.put(splitStr[1], splitStr[0]);
					   		}
					   	} else {
					   		while((str = br.readLine()) != null)
					   		{
					   			String[] splitStr = str.split(",");
					   			popularMap.put(splitStr[1], splitStr[0]);
					   		}
					   	}
				   br.close();
				   //return url for matching
				   return urls[0];
			   } catch (MalformedURLException e) {
				   if(urls[0].equals(POPULAR_LIST_URL[0]))
					   popularMap.put("ERROR", "Unable to  Contact Server...");
				   else
					   searchMap.put("ERROR", "Unable to  Contact Server...");
				   return urls[0];
				   //return contentStored;
			   } catch (IOException e) {
				   if(urls[0].equals(POPULAR_LIST_URL[0]))
					   popularMap.put("ERROR", "Error Retrieving Content...");
				   else
					   searchMap.put("ERROR", "Error Retrieving Content...");
				   //return contentStored;
				   return urls[0];
			   }
		   } else {
			   if(urls[0].equals(POPULAR_LIST_URL[0]))
			   {
				   popularMap.put("ERROR", "No Internet Connection...");
			   }
			   else {
				   searchMap.put("ERROR", "Error Retrieving Content...");
			   }
			   //return url requested
			   return urls[0];
		   }
	   }
	   protected void onPostExecute(String outcome) {
		   try {
			   if(outcome.equals(POPULAR_LIST_URL[0]))
			   {
				   try {
					   if(popularMap.containsKey("ERROR")) {
						   IllegalAccessException e = new IllegalAccessException();
						   throw e;
					   }
				   } catch(NullPointerException e) {
					   //continue
				   }
			   } else {
				   try {
					   if(searchMap.containsKey("ERROR")) {
						   IllegalAccessException e = new IllegalAccessException();
						   throw e;
					   }
				   } catch(NullPointerException e) {
					   //continue
				   }
			   }
			   if(outcome.equals(POPULAR_LIST_URL[0]))
			   {
				   initializeButtons();
			   } else if(outcome.equals(SEARCH_DATABASE_URL[0]))
			   {
				   populateSearchList();
			   }
		   } catch (IllegalAccessException e) {
			   if(outcome.equals(SEARCH_DATABASE_URL[0]))
				   Toast.makeText(getApplicationContext(), popularMap.get("ERROR"), Toast.LENGTH_LONG).show();
			   else
				   Toast.makeText(getApplicationContext(), searchMap.get("ERROR"), Toast.LENGTH_LONG).show();
		   }
	   }
   }
   public boolean isOnline() {
	    ConnectivityManager cm =
	        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo netInfo = cm.getActiveNetworkInfo();
	    if (netInfo != null && netInfo.isConnectedOrConnecting()) {
	        return true;
	    }
	    return false;
	}
}
