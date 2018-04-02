package builderspace.tripadvisor.controller.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import builderspace.tripadvisor.R;
import builderspace.tripadvisor.model.Location;
import builderspace.tripadvisor.model.Message;
import builderspace.tripadvisor.model.Trip;
import builderspace.tripadvisor.model.User;
import builderspace.tripadvisor.view.CustomChatAdapter;

public class ChatActivity extends AppCompatActivity implements CustomChatAdapter.ChatOptions {

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference muidTrip;
    private ListView listView;
    private EditText editTextMessage;
    private Location location;
    private ArrayList<Message> messages = new ArrayList<>();
    private CustomChatAdapter customChatAdapter;
    private FirebaseUser user;
    private User currentUser = new User();
    private Trip currentTrip = new Trip();
    private Trip currentTripClone = new Trip();
    private HashMap<String, String> userKeyAndName = new HashMap<>();
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        TextView textViewTripTitle = findViewById(R.id.textViewName);
        ImageView imageViewSend = findViewById(R.id.imageViewSend);
        ImageView imageViewAddPhoto = findViewById(R.id.imageViewImage);
        ImageView imageViewDeleteLocation = findViewById(R.id.imageViewDelLocation);
        editTextMessage = findViewById(R.id.editTextMsg);
        listView = findViewById(R.id.listView1);

        location = new Location();
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_chat);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                location.setName(place.getName().toString());
                location.setLatitude(place.getLatLng().latitude);
                location.setLongitude(place.getLatLng().longitude);

                ArrayList<Location> locations = currentTrip.getLocations();
                locations.add(location);
                currentTrip.setLocations(locations);
                muidTrip.child(currentTrip.getKey()).setValue(currentTrip);

                Toast.makeText(ChatActivity.this, "Location Added.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Status status) {
                Log.i("DEMO", "An error occurred: " + status);
            }
        });

        imageViewDeleteLocation.setOnClickListener(v -> {
            final ArrayList<Location> locations = currentTrip.getLocations();
            final ArrayList<String> locationsString = new ArrayList<>();
            final ArrayList<Location> locationsToRemove = new ArrayList<>();


            for (Location loc : locations) {
                locationsString.add(loc.getName());
            }

            AlertDialog dialog = new AlertDialog.Builder(ChatActivity.this)
                    .setTitle("Select Locations To Remove")
                    .setMultiChoiceItems(locationsString.toArray(new CharSequence[locationsString.size()]), null, (dialog13, which, isChecked) -> {
                        if (isChecked) {
                            locationsToRemove.add(locations.get(which));
                        } else if (locationsToRemove.contains(locations.get(which))) {
                            locationsToRemove.remove(locations.get(which));
                        }
                    }).setPositiveButton("OK", (dialog12, id) -> {
                        if (locations.size() == locationsToRemove.size()) {
                            Toast.makeText(ChatActivity.this, getString(R.string.cannot_remove_all_locations), Toast.LENGTH_SHORT).show();
                        } else {
                            for (Location locationToRemove : locationsToRemove) {
                                locations.remove(locationToRemove);
                                currentTrip.setLocations(locations);
                                muidTrip.child(currentTrip.getKey()).setValue(currentTrip);


                                Toast.makeText(ChatActivity.this, "Location(s) removed.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).setNegativeButton("Cancel", (dialog1, id) -> {
                        locationsToRemove.clear();
                        dialog1.dismiss();
                    }).create();
            dialog.show();
        });

        currentTrip = (Trip) getIntent().getSerializableExtra("TRIP");
        currentTripClone = (Trip) getIntent().getSerializableExtra("TRIP");
        currentUser = (User) getIntent().getSerializableExtra("CURRENT_USER");
        userKeyAndName = (HashMap<String, String>) getIntent().getSerializableExtra("ALL_USERS_MAP");
        textViewTripTitle.setText(currentTrip.getTitle());

        user = FirebaseAuth.getInstance().getCurrentUser();

        muidTrip = databaseReference.child("TRIP");

        imageViewSend.setOnClickListener(v -> {
            ArrayList<Message> newMessages = currentTrip.getMessages();

            if (editTextMessage.getText().toString().equals(""))
                Toast.makeText(ChatActivity.this, "Enter something to send", Toast.LENGTH_SHORT).show();
            else {
                Message message = new Message();
                message.setName(currentUser.getKey());
                message.setText(editTextMessage.getText().toString());
                message.setTime(new Date());
                message.setImgUrl("");
                newMessages.add(message);
                currentTripClone.setMessages(newMessages);
                currentTrip.setMessages(newMessages);
                muidTrip.child(currentTrip.getKey()).setValue(currentTrip);

                editTextMessage.setText("");
            }
        });

        imageViewAddPhoto.setOnClickListener(v -> {
            Intent i = new Intent();
            i.setType("image/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(i, "Select Picture"), 1);
        });

        DatabaseReference muidUser = databaseReference.child("USER");
        muidUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    if (currentUser == null && user.getEmail().equals((d.getValue(User.class)).getEmail())) {
                        currentUser = d.getValue(User.class);
                    }

                    muidTrip.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            messages.clear();

                            for (DataSnapshot d : dataSnapshot.getChildren()) {
                                Trip trip = d.getValue(Trip.class);
                                if (currentTrip.getKey().equals(trip.getKey())) {
                                    currentTrip = trip;
                                    currentTripClone = trip;
                                    messages = currentTrip.getMessages();
                                    break;
                                }
                            }

                            Iterator<Message> iter = messages.iterator();
                            while (iter.hasNext()) {
                                Message message = iter.next();
                                ArrayList<String> usersWhoDeletedMessage = message.getDeletedByUsers();
                                for (String userWhoDeletedMessage : usersWhoDeletedMessage) {
                                    if (currentUser.getEmail().equals(userWhoDeletedMessage)) {
                                        iter.remove();
                                        break;
                                    }
                                }
                            }

                            Date currentUserJoinDate = currentTrip.getUsersJoinTime().get(currentUser.getKey());
                            if (currentUserJoinDate != null) {
                                Iterator<Message> iter2 = messages.iterator();
                                while (iter2.hasNext()) {
                                    Message message = iter2.next();
                                    if (message.getTime().before(currentUserJoinDate)) {
                                        iter2.remove();
                                    }
                                }
                            }

                            customChatAdapter = new CustomChatAdapter(ChatActivity.this, R.layout.listview_chat_message_row, messages, ChatActivity.this::deleteChat, userKeyAndName, currentUser);
                            customChatAdapter.setNotifyOnChange(true);
                            listView.setAdapter(customChatAdapter);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(ChatActivity.this, "Hey There", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Demo", "Error in database");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filepath = data.getData();
            StorageReference storageReference = firebaseStorage.getReference("images/" + filepath.getLastPathSegment());
            UploadTask task = storageReference.putFile(filepath);
            task.addOnSuccessListener(ChatActivity.this, taskSnapshot -> {
                ArrayList<Message> messages = currentTrip.getMessages();

                Message message = new Message();
                @SuppressWarnings("VisibleForTests") String s = taskSnapshot.getDownloadUrl().toString();
                message.setImgUrl(s);
                message.setName(currentUser.getfName() + " " + currentUser.getlName());
                message.setText("");
                message.setTime(new Date());
                messages.add(message);
                currentTrip.setMessages(messages);
                muidTrip.child(currentTrip.getKey()).setValue(currentTrip);
            });
        }
    }

    @Override
    public void deleteChat(int position) {
        ArrayList<String> usersWhoDeleted = messages.get(position).getDeletedByUsers();
        usersWhoDeleted.add(currentUser.getEmail());
        ArrayList<Message> allMessages = currentTripClone.getMessages();

        for (int i = 0; i < allMessages.size(); i++) {
            if (allMessages.get(i).toString().equals(messages.get(position).toString())) {
                allMessages.get(i).setDeletedByUsers(usersWhoDeleted);
                currentTrip.setMessages(allMessages);
                muidTrip.child(currentTrip.getKey()).setValue(currentTrip);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.embededMap:
                Intent i = new Intent(ChatActivity.this, MapsActivity.class);
                i.putExtra("CURRENT_TRIP", currentTrip);
                startActivity(i);
                return true;

            case R.id.goToGoogleMaps:
                ArrayList<Location> locations = currentTrip.getLocations();
                ArrayList<LatLng> latLngs = new ArrayList<>();

                for (Location locn : locations) {
                    latLngs.add(new LatLng(locn.getLatitude(), locn.getLongitude()));
                }

                String uri = "";
                for (LatLng latLng : latLngs) {
                    if (TextUtils.isEmpty(uri)) {
                        uri = String.format(
                                "http://maps.google.com/maps?saddr=%s, %s",
                                String.valueOf(latLng.latitude).replace(",", "."),
                                String.valueOf(latLng.longitude).replace(",", ".")
                        );
                    } else {
                        if (!uri.contains("&daddr")) {
                            uri += String.format(
                                    "&daddr=%s, %s", formatValue(latLng.latitude), formatValue(latLng.longitude));
                        } else {
                            uri += String.format(
                                    "+to:%s, %s", formatValue(latLng.latitude), formatValue(latLng.longitude));
                        }
                    }
                }
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);

                return true;
        }
        return false;
    }

    private String formatValue(Double aDouble) {
        return String.valueOf(aDouble).replace(",", ".");
    }
}
