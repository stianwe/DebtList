package android.debtlistandroid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import logic.User;

import session.Session;
import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class FriendViewActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friend_view);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		// Display friends
		List<String> friends = new ArrayList<String>();
		for (int i = 0; i < Session.session.getUser().getNumberOfFriends(); i++) {
			friends.add(Session.session.getUser().getFriend(i).getUsername());
		}
		Collections.sort(friends);
		((ListView) findViewById(R.id.friend_view_list)).setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, friends));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_friend_view, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
