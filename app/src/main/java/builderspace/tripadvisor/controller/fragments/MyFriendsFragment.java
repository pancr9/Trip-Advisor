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
import builderspace.tripadvisor.view.RecyclerMyFriendsAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyFriendsFragment extends Fragment {

    private ArrayList<User> friends = new ArrayList<>();
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user;
    private User currentUser;

    private ArrayList<User> allUsers = new ArrayList<>();

    public MyFriendsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_my_friends, container, false);
        user = FirebaseAuth.getInstance().getCurrentUser();
        currentUser = (User) (getActivity()).getIntent().getSerializableExtra("CURRENT_USER");

        RecyclerView recyclerViewMyFriends = view.findViewById(R.id.recyclerViewMyFriends);
        RecyclerMyFriendsAdapter recyclerMyFriendsAdapter = new RecyclerMyFriendsAdapter(friends, currentUser);
        recyclerViewMyFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewMyFriends.setAdapter(recyclerMyFriendsAdapter);

        DatabaseReference muid = databaseReference.child("USER");
        muid.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                allUsers.clear();
                friends.clear();


                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    if (currentUser == null && user.getEmail().equals((d.getValue(User.class)).getEmail())) {
                        currentUser = d.getValue(User.class);
                    }
                    allUsers.add(d.getValue(User.class));
                }


                ArrayList<String> friendsString = currentUser.getFriends();
                for (String friendString : friendsString) {
                    for (User user : allUsers) {
                        if (friendString.equals(user.getKey())) {
                            friends.add(user);
                            break;
                        }
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
