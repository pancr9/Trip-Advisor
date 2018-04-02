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
import builderspace.tripadvisor.view.RecyclerPeopleYouMayKnowAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class YouMayKnowFriendsFragment extends Fragment {

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private FirebaseUser user;
    private User currentUser;
    private ArrayList<User> notFriends = new ArrayList<>();
    private ArrayList<User> friends = new ArrayList<>();

    private ArrayList<User> allUsers = new ArrayList<>();

    public YouMayKnowFriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_you_may_know_friends, container, false);
        user = FirebaseAuth.getInstance().getCurrentUser();

        RecyclerView recyclerViewPeopleYouMayKnow = view.findViewById(R.id.recyclerViewPeopleYouMayKnow);
        RecyclerPeopleYouMayKnowAdapter recyclerPeopleYouMayKnowAdapter = new RecyclerPeopleYouMayKnowAdapter(notFriends, currentUser);
        recyclerViewPeopleYouMayKnow.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewPeopleYouMayKnow.setAdapter(recyclerPeopleYouMayKnowAdapter);

        DatabaseReference muid = databaseReference.child("USER");
        muid.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                allUsers.clear();
                notFriends.clear();
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

                notFriends = (ArrayList<User>) allUsers.clone();
                for (User notFriend : notFriends) {
                    if (currentUser.getEmail().equals(notFriend.getEmail())) {
                        notFriends.remove(notFriend);
                        break;
                    }
                }

                for (User friend : friends) {
                    for (User notFriend : notFriends) {
                        if (friend.getEmail().equals(notFriend.getEmail())) {
                            notFriends.remove(notFriend);
                            break;
                        }
                    }
                }

                ArrayList<User> usersToRemove = new ArrayList<>();
                for (User friend : notFriends) {
                    if (friend.getFriendRequests().contains(currentUser.getKey())) {
                        notFriends.remove(friend);
                        usersToRemove.add(friend);
                    }
                }

                for (User userToRemove : usersToRemove) {
                    notFriends.remove(userToRemove);
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
