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

public class RecyclerPeopleYouMayKnowAdapter extends RecyclerView.Adapter<RecyclerPeopleYouMayKnowAdapter.ViewHolder> {

    private ArrayList<User> mDataset;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private Context mContext;
    private DatabaseReference muid;
    private User currentUser;
    private User userToSendRequest;

    public RecyclerPeopleYouMayKnowAdapter(ArrayList<User> mDataset, User currentUser) {
        this.mDataset = mDataset;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_people_you_may_know_row, parent, false);
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

        holder.sendRequest.setOnClickListener(v -> {
            userToSendRequest = mDataset.get(position);
            ArrayList<String> friendRequests = userToSendRequest.getFriendRequests();
            friendRequests.add(currentUser.getKey());

            userToSendRequest.setFriendRequests(friendRequests);

            muid.child(userToSendRequest.getKey()).removeValue();
            muid.child(userToSendRequest.getKey()).setValue(userToSendRequest);

            Toast.makeText(mContext, "Friend Request Sent.", Toast.LENGTH_SHORT).show();
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
        ImageView sendRequest;


        ViewHolder(View v) {
            super(v);
            mCv = v.findViewById(R.id.cardView);
            fName = v.findViewById(R.id.person_fname1);
            lName = v.findViewById(R.id.person_lname1);
            profilePicture = v.findViewById(R.id.person_photo1);
            sendRequest = v.findViewById(R.id.imageViewClick);
        }
    }

}
