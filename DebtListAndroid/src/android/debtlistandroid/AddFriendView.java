package android.debtlistandroid;

import console.Main;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class AddFriendView extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_friend_view);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_add_friend_view, menu);
		return true;
	}

	public void add_friend(View v) {
		System.out.println("add friend " + ((TextView) findViewById(R.id.add_friend_username_email)).getText().toString());
		// TODO Should really make the console VERSION's method return something telling if everything went well
		if(Main.processAddFriend("add friend " + ((TextView) findViewById(R.id.add_friend_username_email)).getText().toString())) {
			// Friend request sent, show friend view
			startActivity(new Intent(this, FriendViewActivity.class));
		} else {
			// Display error messagereq
			((TextView) findViewById(R.id.add_friend_error_message)).setVisibility(View.VISIBLE);
		}
		// 
	}
}
