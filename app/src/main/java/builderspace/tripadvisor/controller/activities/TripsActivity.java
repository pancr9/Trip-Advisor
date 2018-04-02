package builderspace.tripadvisor.controller.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;

import builderspace.tripadvisor.R;
import builderspace.tripadvisor.model.User;
import builderspace.tripadvisor.view.TripsPagerAdapter;

public class TripsActivity extends AppCompatActivity {

    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentUser = (User) getIntent().getSerializableExtra("CURRENT_USER");

        FloatingActionButton floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(v -> {
            Intent i = new Intent(this, CreateTripActivity.class);
            i.putExtra("CURRENT_USER", currentUser);
            startActivity(i);
            finish();
        });


        // Find the view pager that will allow the user to swipe between fragments
        ViewPager viewPager = findViewById(R.id.viewpager);

        // Create an adapter that knows which fragment should be shown on each page
        TripsPagerAdapter adapter = new TripsPagerAdapter(this, getSupportFragmentManager());

        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.friendRequests:
                Intent iFriends = new Intent(TripsActivity.this, FriendsActivity.class);
                iFriends.putExtra("CURRENT_USER", currentUser);
                startActivity(iFriends);
                finish();
                return true;

            case R.id.editProfile:
                Intent iEditProfile = new Intent(TripsActivity.this, ProfileActivity.class);
                iEditProfile.putExtra("CURRENT_USER", currentUser);
                startActivity(iEditProfile);
                return true;

            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                Intent iLogout = new Intent(TripsActivity.this, MainActivity.class);
                startActivity(iLogout);
                finish();
                return true;
        }
        return false;
    }
}
