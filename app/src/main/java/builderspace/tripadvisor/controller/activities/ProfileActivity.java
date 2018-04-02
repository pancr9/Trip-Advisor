package builderspace.tripadvisor.controller.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import builderspace.tripadvisor.R;
import builderspace.tripadvisor.model.User;

public class ProfileActivity extends AppCompatActivity {

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference muid;
    private ImageView profilePic;
    private EditText editTextFName;
    private EditText editTextLName;
    private Spinner gender;
    private ProgressDialog pd;
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private StorageReference storageReference;
    private FirebaseUser user;
    private User currentUser;

    private Uri filepath;
    private boolean profilePicChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profilePic = findViewById(R.id.imageViewProfilePic);
        Button buttonSave = findViewById(R.id.buttonSave);
        Button buttonCancel = findViewById(R.id.buttonCancel);
        editTextFName = findViewById(R.id.editTextSignUpFirstName);
        editTextLName = findViewById(R.id.editTextSignUpLastName);
        gender = findViewById(R.id.spinnerGender);
        pd = new ProgressDialog(ProfileActivity.this);
        pd.setCancelable(false);
        pd.setMessage("Creating User");

        currentUser = (User) getIntent().getSerializableExtra("CURRENT_USER");

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gender, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(adapter);

        buttonCancel.setOnClickListener(v -> finish());

        user = FirebaseAuth.getInstance().getCurrentUser();
        muid = databaseReference.child("USER");
        muid.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    if (currentUser == null && user.getEmail().equals((d.getValue(User.class)).getEmail())) {
                        currentUser = d.getValue(User.class);
                    }

                    if (currentUser != null) {
                        Picasso.with(ProfileActivity.this).load(currentUser.getProfilePicURL()).into(profilePic);
                        editTextFName.setText(currentUser.getfName());
                        editTextLName.setText(currentUser.getlName());
                        gender.setSelection(getIndex(gender, currentUser.getGender()));

                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        buttonSave.setOnClickListener(v -> {
            muid.child(currentUser.getKey()).removeValue();

            currentUser.setfName(editTextFName.getText().toString());
            currentUser.setlName(editTextLName.getText().toString());
            currentUser.setGender(gender.getSelectedItem().toString());

            pd.show();
            if (profilePicChanged) {
                storageReference = firebaseStorage.getReference("profilePictures/" + user.getEmail());
                UploadTask task2 = storageReference.putFile(filepath);

                task2.addOnSuccessListener(ProfileActivity.this, taskSnapshot -> {

                    @SuppressWarnings("VisibleForTests") Uri s = taskSnapshot.getDownloadUrl();

                    currentUser.setProfilePicURL(s.toString());

                    muid.child(currentUser.getKey()).setValue(currentUser);

                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                            .setDisplayName(editTextFName.getText() + " " + editTextLName.getText())
                            .setPhotoUri(s)
                            .build();

                    user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ProfileActivity.this, "User Updated", Toast.LENGTH_SHORT).show();
                                pd.dismiss();
                                finish();
                            }
                        }
                    });
                });

            } else {
                muid.child(currentUser.getKey()).setValue(currentUser);
                pd.dismiss();
                finish();
            }

        });

        profilePic.setOnClickListener(v -> {
            Intent i = new Intent();
            i.setType("image/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(i, "Select Picture"), 1);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            profilePicChanged = true;
            filepath = data.getData();
            Picasso.with(ProfileActivity.this).load(data.getData()).noPlaceholder().centerCrop().fit()
                    .into(profilePic);
        }
    }

    private int getIndex(Spinner spinner, String myString) {
        int index = 0;

        for (int i = 0; i < spinner.getCount(); i++) {
            Log.d("demo", "Spinner : " + spinner.getItemAtPosition(i).toString());
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)) {
                index = i;
                break;
            }
        }
        Log.d("demo", "Spinner returns : " + index);
        return index;
    }
}
