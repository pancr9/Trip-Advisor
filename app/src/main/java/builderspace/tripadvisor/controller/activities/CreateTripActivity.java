package builderspace.tripadvisor.controller.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import builderspace.tripadvisor.R;
import builderspace.tripadvisor.model.Location;
import builderspace.tripadvisor.model.Trip;
import builderspace.tripadvisor.model.User;

public class CreateTripActivity extends AppCompatActivity {

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference muidTrip;
    private TextView tripName;
    private ImageView tripImage;
    private ProgressDialog pd;
    private Location location;
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private StorageReference storageReference;

    private FirebaseUser user;
    private User currentUser;
    private ArrayList<User> allUsers = new ArrayList<>();
    private ArrayList<User> friendsToAdd = new ArrayList<>();

    private Uri filepath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_trip);

        currentUser = (User) getIntent().getSerializableExtra("CURRENT_USER");
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.add_a_trip));

        muidTrip = databaseReference.child("TRIP");

        tripName = (EditText) findViewById(R.id.editTextTripName);
        tripImage = findViewById(R.id.imageViewTripPhoto);
        ImageView addMembers = findViewById(R.id.imageViewAddMembers);
        Button createButton = findViewById(R.id.buttonCreateTrip);

        location = new Location();

        pd = new ProgressDialog(this);
        pd.setCancelable(false);

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                location.setName(place.getName().toString());
                location.setLatitude(place.getLatLng().latitude);
                location.setLongitude(place.getLatLng().longitude);
            }

            @Override
            public void onError(Status status) {
                Log.i("DEMO", "An error occurred: " + status);
            }
        });

        user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference muidUser = databaseReference.child("USER");
        muidUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    if (currentUser == null && user.getEmail().equals((d.getValue(User.class)).getEmail())) {
                        currentUser = d.getValue(User.class);
                    }
                    allUsers.add(d.getValue(User.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Demo", "Error in database");
            }
        });

        createButton.setOnClickListener(v -> {
            if (validate()) {
                pd.show();

                ArrayList<Location> locations = new ArrayList<>();
                locations.add(location);
                final Trip trip = new Trip();
                trip.setTitle(tripName.getText().toString());
                trip.setLocations(locations);
                trip.setKey(muidTrip.push().getKey());
                trip.setTime(new Date());
                trip.setOwner(currentUser.getKey());

                HashMap<String, Date> userAndTime = trip.getUsersJoinTime();
                userAndTime.put(currentUser.getKey(), new Date());
                for (User friendToAdd : friendsToAdd) {
                    userAndTime.put(friendToAdd.getKey(), new Date());
                }
                trip.setUsersJoinTime(userAndTime);

                if (filepath == null) {
                    trip.setImgUrl(MainActivity.DEFAULT_TRIP_LINK);

                    muidTrip.child(trip.getKey()).setValue(trip);
                    pd.dismiss();
                    Intent i = new Intent(CreateTripActivity.this, TripsActivity.class);
                    startActivity(i);
                    finish();

                } else {
                    storageReference = firebaseStorage.getReference("tripPictures/" + tripName.getText().toString());
                    UploadTask task2 = storageReference.putFile(filepath);

                    task2.addOnSuccessListener(CreateTripActivity.this, taskSnapshot -> {

                        @SuppressWarnings("VisibleForTests") Uri s = taskSnapshot.getDownloadUrl();

                        trip.setImgUrl(s.toString());

                        muidTrip.child(trip.getKey()).setValue(trip);
                        pd.dismiss();
                        Intent i = new Intent(CreateTripActivity.this, TripsActivity.class);
                        startActivity(i);
                        finish();
                    });
                }
            }
        });

        tripImage.setOnClickListener(v -> {
            Intent i = new Intent();
            i.setType("image/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(i, getString(R.string.select_picture)), 1);
        });

        addMembers.setOnClickListener(v -> {
            final ArrayList<User> friends = new ArrayList<>();
            final ArrayList<String> friends2 = new ArrayList<>();
            friendsToAdd = new ArrayList<>();

            final ArrayList<String> friendsString = currentUser.getFriends();
            for (String friendString : friendsString) {
                for (User oneUser : allUsers) {
                    if (oneUser.getKey().equals(friendString)) {
                        friends.add(oneUser);
                        friends2.add(oneUser.getfName() + " " + oneUser.getlName());
                        break;
                    }
                }
            }

            if (friends.size() > 0) {
                AlertDialog dialog = new AlertDialog.Builder(CreateTripActivity.this)
                        .setTitle(R.string.select_friends_to_add)
                        .setMultiChoiceItems(friends2.toArray(new CharSequence[friends2.size()]), null, (dialog13, which, isChecked) -> {
                            if (isChecked) {
                                friendsToAdd.add(friends.get(which));
                            } else if (friendsToAdd.contains(friends.get(which))) {
                                friendsToAdd.remove(friends.get(which));
                            }
                        }).setPositiveButton(getString(R.string.ok), (dialog12, id) -> {

                        }).setNegativeButton(getString(R.string.cancel), (dialog1, id) -> {
                            friendsToAdd.clear();
                            dialog1.dismiss();
                        }).create();
                dialog.show();
            } else {
                Toast.makeText(CreateTripActivity.this, getString(R.string.no_friends_to_add), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filepath = data.getData();
            Picasso.with(CreateTripActivity.this).load(data.getData()).noPlaceholder().centerCrop().fit()
                    .into(tripImage);
        }
    }

    private boolean validate() {
        boolean valid = true;
        String email = tripName.getText().toString();
        if (TextUtils.isEmpty(email)) {
            valid = false;
            tripName.setError(getString(R.string.required));
        }
        if (location.getName() == null || "".equals(location.getName())) {
            Toast.makeText(this, getString(R.string.no_location_selected), Toast.LENGTH_SHORT).show();
            valid = false;
        }
        return valid;
    }
}
