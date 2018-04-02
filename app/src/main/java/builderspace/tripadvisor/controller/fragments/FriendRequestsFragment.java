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

import builderspace.tripadvisor.R;
import builderspace.tripadvisor.model.User;
import builderspace.tripadvisor.view.RecyclerFriendRequestAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendRequestsFragment extends Fragment {

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user;
    private User currentUser;
    private ArrayList<User> friendRequests = new ArrayList<>();
    private ArrayList<User> allUsers = new ArrayList<>();
    private ArrayList<User> notFriends = new ArrayList<>();

    public FriendRequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_friend_requests, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();

        RecyclerView recyclerViewMyFriendRequests = view.findViewById(R.id.recyclerViewMyFriendRequests);
        RecyclerFriendRequestAdapter recyclerFriendRequestAdapter = new RecyclerFriendRequestAdapter(friendRequests, currentUser);
        recyclerViewMyFriendRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewMyFriendRequests.setAdapter(recyclerFriendRequestAdapter);


        DatabaseReference muid = databaseReference.child("USER");
        muid.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                allUsers.clear();
                friendRequests.clear();


                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    if (currentUser == null && user.getEmail().equals((d.getValue(User.class)).getEmail())) {
                        currentUser = d.getValue(User.class);
                    }
                    allUsers.add(d.getValue(User.class));
                }


                ArrayList<String> friendRequestsString = currentUser.getFriendRequests();
                for (String friendRequestString : friendRequestsString) {
                    for (User user : allUsers) {
                        if (friendRequestString.equals(user.getKey())) {
                            friendRequests.add(user);
                            break;
                        }
                    }
                }

                for (User friendRequest : friendRequests) {
                    if (notFriends.contains(friendRequest)) {
                        notFriends.remove(friendRequest);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Demo", "Error in database");
            }
        });

        return view;
    }

}
