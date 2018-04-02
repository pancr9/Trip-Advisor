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
import java.util.Date;
import java.util.HashMap;

import builderspace.tripadvisor.R;
import builderspace.tripadvisor.model.Trip;
import builderspace.tripadvisor.model.User;

public class RecyclerFriendsTripsAdapter extends RecyclerView.Adapter<RecyclerFriendsTripsAdapter.ViewHolder> {

    private ArrayList<Trip> mDataset;
    private Context mContext;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference muid;
    private User currentUser;

    public RecyclerFriendsTripsAdapter(ArrayList<Trip> mDataset, User currentUser) {
        this.mDataset = mDataset;
        this.currentUser = currentUser;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_friends_trips_row, parent, false);
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

        holder.joinTrip.setOnClickListener(v -> {
            Trip tripToJoin = mDataset.get(position);
            HashMap<String, Date> userAndTime = tripToJoin.getUsersJoinTime();
            userAndTime.put(currentUser.getKey(), new Date());
            tripToJoin.setUsersJoinTime(userAndTime);

            muid.child(tripToJoin.getKey()).removeValue();
            muid.child(tripToJoin.getKey()).setValue(tripToJoin);

            Toast.makeText(mContext, "Joined Trip.", Toast.LENGTH_SHORT).show();
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
        ImageView joinTrip;


        ViewHolder(View v) {
            super(v);
            mCv = v.findViewById(R.id.cardView);
            title = v.findViewById(R.id.textViewTripName);
            location = v.findViewById(R.id.textViewLocation);
            tripPicture = v.findViewById(R.id.imageViewTripPicture);
            joinTrip = v.findViewById(R.id.imageViewJoinTrip);
        }
    }

}
