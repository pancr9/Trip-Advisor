package builderspace.tripadvisor.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import builderspace.tripadvisor.R;
import builderspace.tripadvisor.model.User;

public class RecyclerMyFriendsAdapter extends RecyclerView.Adapter<RecyclerMyFriendsAdapter.ViewHolder> {

    private ArrayList<User> mDataset;
    private Context mContext;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference muid;
    private User currentUser;
    private User userToRemoveFromFriends;

    public RecyclerMyFriendsAdapter(ArrayList<User> mDataset, User currentUser) {
        this.mDataset = mDataset;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_remove_from_friends_row, parent, false);
        ViewHolder vh = new ViewHolder(v);
        mContext = parent.getContext();
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        muid = databaseReference.child("USER");

        holder.fName.setText(mDataset.get(position).getfName());
        holder.lName.setText(mDataset.get(position).getlName());
        Picasso.with(mContext).load(mDataset.get(position).getProfilePicURL()).into(holder.profilePicture);

        holder.removeFromFriends.setOnClickListener(v -> {
            userToRemoveFromFriends = mDataset.get(position);

            ArrayList<String> friends = currentUser.getFriends();
            friends.remove(userToRemoveFromFriends.getKey());
            currentUser.setFriends(friends);

            muid.child(currentUser.getKey()).removeValue();
            muid.child(currentUser.getKey()).setValue(currentUser);

            ArrayList<String> friends2 = userToRemoveFromFriends.getFriends();
            friends2.remove(currentUser.getKey());
            userToRemoveFromFriends.setFriends(friends2);
            muid.child(userToRemoveFromFriends.getKey()).child(("friends")).removeValue();
            muid.child(userToRemoveFromFriends.getKey()).child("friends").setValue(friends2);

            Toast.makeText(mContext, "Friend Removed.", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        CardView mCv;
        TextView fName;
        TextView lName;
        ImageView profilePicture;
        ImageView removeFromFriends;


        ViewHolder(View v) {
            super(v);
            mCv = v.findViewById(R.id.cvRemoveFriend);
            fName = v.findViewById(R.id.person_fname2);
            lName = v.findViewById(R.id.person_lname2);
            profilePicture = v.findViewById(R.id.person_photo2);
            removeFromFriends = v.findViewById(R.id.imageViewClick2);
        }
    }

}
