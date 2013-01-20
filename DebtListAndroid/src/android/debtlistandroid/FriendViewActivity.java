package android.debtlistandroid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import logic.User;

import requests.FriendRequest;
import requests.FriendRequest.FriendRequestStatus;
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
		
		List<String> incoming = new ArrayList<String>();
		List<String> outgoing = new ArrayList<String>();
		for (int i = 0; i < Session.session.getUser().getNumberOfFriendRequests(); i++) {
			FriendRequest r = Session.session.getUser().getFriendRequest(i);
			if(r.getStatus() == FriendRequestStatus.PENDING) {
				if(r.getFriendUsername().equals(Session.session.getUser().getUsername())) 
					incoming.add(r.getFromUser().getUsername());
				else if(r.getFromUser().getUsername().equals(Session.session.getUser().getUsername()))
					outgoing.add(r.getFriendUsername());
			}
		}
		// Display incoming friend requests
		displayFriends(R.id.friend_view_incoming_separator, R.id.friend_view_list_incoming, incoming);
		
		// Display confirmed friends
		List<String> friends = new ArrayList<String>();
		for (int i = 0; i < Session.session.getUser().getNumberOfFriends(); i++) {
			friends.add(Session.session.getUser().getFriend(i).getUsername());
		}
		displayFriends(R.id.friend_view_confirmed_separator, R.id.friend_view_list, friends);
		
		// Display outgoing friend requests
		displayFriends(R.id.friend_view_outgoing_separator, R.id.friend_view_list_outgoing, outgoing);
	}

	/**
	 * Displays the given separator and list with the strings given as parameter sorted, if the list is not empty
	 * 
	 * @param separatorId	The separator's id
	 * @param listViewId	The ListView's id
	 * @param friends		The strings to display in the ListView
	 */
	private void displayFriends(int separatorId, int listViewId, List<String> friends) {
		// Check if the list is empty
		if(friends.isEmpty()) {
			// Hide separator
			((TextView) findViewById(separatorId)).setVisibility(View.GONE);
		} else {
			Collections.sort(friends);
			((ListView) findViewById(listViewId)).setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, friends));
		}
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
