package builderspace.tripadvisor.controller.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import builderspace.tripadvisor.R;
import builderspace.tripadvisor.model.Trip;
import builderspace.tripadvisor.model.User;
import builderspace.tripadvisor.view.RecyclerMyTripsAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyTrips extends Fragment {

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user;
    private User currentUser;
    private RecyclerView recyclerViewMyTrips;
    private RecyclerMyTripsAdapter recyclerMyTripsAdapter;
    private View rootView;
    private ArrayList<User> allUsers = new ArrayList<>();
    private ArrayList<Trip> allTrips = new ArrayList<>();
    private HashMap<String, String> userKeyAndName = new HashMap<>();
    private ArrayList<Trip> myTrips = new ArrayList<>();
    private ArrayList<Trip> friendsTrips = new ArrayList<>();
    private DatabaseReference muidTrip;

    public MyTrips() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_my_trips, container, false);


        user = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference muidUser = databaseReference.child("USER");
        muidUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    if (currentUser == null && user.getEmail().equals((d.getValue(User.class)).getEmail())) {
                        currentUser = d.getValue(User.class);
                    }
                    allUsers.add(d.getValue(User.class));
                    userKeyAndName.put(d.getValue(User.class).getKey(), d.getValue(User.class).getfName() + " " + d.getValue(User.class).getlName());
                }

                muidTrip = databaseReference.child("TRIP");
                muidTrip.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        allTrips.clear();
                        myTrips.clear();
                        friendsTrips.clear();

                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                            allTrips.add(d.getValue(Trip.class));
                            if (d.getValue(Trip.class).getUsersJoinTime().containsKey(currentUser.getKey())) {
                                myTrips.add(d.getValue(Trip.class));
                            }
                        }

                        ArrayList<String> myFriends = currentUser.getFriends();
                        for (String myFriend : myFriends) {
                            for (Trip trip : allTrips) {
                                if (trip.getOwner().equals(myFriend)) {
                                    boolean alreadyContains = false;
                                    for (Trip friendsTrip : friendsTrips) {
                                        if (friendsTrip.getKey().equals(trip.getKey())) {
                                            alreadyContains = true;
                                        }
                                    }
                                    if (!alreadyContains) {
                                        friendsTrips.add(trip);
                                    }
                                }
                            }
                        }

                        for (Trip myTrip : myTrips) {
                            for (Trip friendTrip : friendsTrips) {
                                if (myTrip.getKey().equals(friendTrip.getKey())) {
                                    friendsTrips.remove(friendTrip);
                                    break;
                                }
                            }
                        }

                        recyclerViewMyTrips = rootView.findViewById(R.id.recyclerViewMyTrips);
                        recyclerMyTripsAdapter = new RecyclerMyTripsAdapter(myTrips, currentUser, userKeyAndName);
                        recyclerViewMyTrips.setLayoutManager(new LinearLayoutManager(getContext()));
                        recyclerViewMyTrips.setAdapter(recyclerMyTripsAdapter);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("Demo", "Error in database");
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return rootView;
    }

}
