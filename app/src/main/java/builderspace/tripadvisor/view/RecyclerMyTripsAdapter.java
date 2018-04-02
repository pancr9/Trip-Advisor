package builderspace.tripadvisor.view;

import android.content.Context;
import android.content.Intent;
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
import java.util.Date;
import java.util.HashMap;

import builderspace.tripadvisor.R;
import builderspace.tripadvisor.controller.activities.ChatActivity;
import builderspace.tripadvisor.model.Trip;
import builderspace.tripadvisor.model.User;

public class RecyclerMyTripsAdapter extends RecyclerView.Adapter<RecyclerMyTripsAdapter.ViewHolder> {

    private ArrayList<Trip> mDataset;
    private HashMap<String, String> userKeyAndName;
    private User currentUser;
    private Context mContext;

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference muid;

    public RecyclerMyTripsAdapter(ArrayList<Trip> mDataset, User currentUser, HashMap<String, String> userKeyAndName) {
        this.mDataset = mDataset;
        this.currentUser = currentUser;
        this.userKeyAndName = userKeyAndName;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_my_trips_row, parent, false);
        ViewHolder vh = new ViewHolder(v);
        mContext = parent.getContext();
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        muid = databaseReference.child("TRIP");

        holder.title.setText(mDataset.get(position).getTitle());
        holder.location.setText(mDataset.get(position).getLocations().get(0).getName());
        Picasso.with(mContext).load(mDataset.get(position).getImgUrl()).into(holder.tripPicture);

        holder.leaveTrip.setOnClickListener(v -> {
            Trip tripToLeave = mDataset.get(position);
            HashMap<String, Date> userAndTime = tripToLeave.getUsersJoinTime();
            userAndTime.remove(currentUser.getKey());
            tripToLeave.setUsersJoinTime(userAndTime);

            muid.child(tripToLeave.getKey()).removeValue();
            muid.child(tripToLeave.getKey()).setValue(tripToLeave);

            Toast.makeText(mContext, "Left Trip.", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        CardView mCv;
        TextView title;
        TextView location;
        ImageView tripPicture;
        ImageView leaveTrip;


        ViewHolder(View v) {
            super(v);
            mCv = v.findViewById(R.id.cardView);
            title = v.findViewById(R.id.textViewTripName);
            location = v.findViewById(R.id.textViewLocation);
            tripPicture = v.findViewById(R.id.imageViewTripPicture);
            leaveTrip = v.findViewById(R.id.imageViewLeaveTrip);

            v.setOnClickListener(v1 -> {
                Intent i = new Intent(v1.getContext(), ChatActivity.class);
                i.putExtra("TRIP", mDataset.get(getAdapterPosition()));
                i.putExtra("CURRENT_USER", currentUser);
                i.putExtra("ALL_USERS_MAP", userKeyAndName);
                v1.getContext().startActivity(i);
            });
        }
    }

}
