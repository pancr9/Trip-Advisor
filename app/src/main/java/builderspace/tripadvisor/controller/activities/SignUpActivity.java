package builderspace.tripadvisor.controller.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Date;

import builderspace.tripadvisor.R;
import builderspace.tripadvisor.model.User;

public class SignUpActivity extends AppCompatActivity {

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference muid;
    private FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private FirebaseUser user;
    private StorageReference storageReference;
    private String fName;
    private String lName;
    private String pwd;
    private String repeatPwd;
    private String email;
    private ImageView profilePic;
    private EditText editTextFName;
    private EditText editTextLName;
    private EditText editTextEmail;
    private EditText editTextPwd;
    private EditText editTextRepeatPwd;
    private Spinner gender;
    private ProgressDialog pd;
    private Uri filepath;
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        profilePic = findViewById(R.id.imageViewProfilePic);
        Button buttonSignUp = findViewById(R.id.buttonSignUp);
        Button buttonCancel = findViewById(R.id.buttonCancel);
        editTextEmail = findViewById(R.id.editTextSignUpEmail);
        editTextFName = findViewById(R.id.editTextSignUpFirstName);
        editTextLName = findViewById(R.id.editTextSignUpLastName);
        editTextPwd = findViewById(R.id.editTextSignUpPassword);
        editTextRepeatPwd = findViewById(R.id.editTextSignUpRepeatPassword);
        gender = findViewById(R.id.spinnerGender);
        pd = new ProgressDialog(SignUpActivity.this);
        pd.setCancelable(false);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.gender, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(adapter);

        fAuth = FirebaseAuth.getInstance();
        muid = databaseReference.child("USER");

        buttonCancel.setOnClickListener(v -> finish());

        profilePic.setOnClickListener(v -> {

            Intent i = new Intent();
            i.setType("image/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(i, "Select Picture"), 1);
        });

        buttonSignUp.setOnClickListener(v -> {
            if (validate()) {
                email = editTextEmail.getText().toString();
                pwd = editTextPwd.getText().toString();
                repeatPwd = editTextRepeatPwd.getText().toString();
                fName = editTextFName.getText().toString();
                lName = editTextLName.getText().toString();

                pd.show();
                fAuth.createUserWithEmailAndPassword(email, pwd).addOnCompleteListener(SignUpActivity.this, task -> {
                    if (task.isSuccessful()) {
                        user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            final User u = new User();
                            u.setEmail(editTextEmail.getText().toString());
                            u.setfName(editTextFName.getText().toString());
                            u.setlName(editTextLName.getText().toString());
                            u.setGender(gender.getSelectedItem().toString());
                            u.setKey(muid.push().getKey());
                            u.setCreationDate(new Date());

                            if (filepath == null) {
                                u.setProfilePicURL(MainActivity.DEFAULT_PROFILE_LINK);
                                Log.d("DEMO", u.toString());
                                createUser(u);

                            } else {
                                storageReference = firebaseStorage.getReference("profilePictures/" + user.getEmail());
                                UploadTask task2 = storageReference.putFile(filepath);
                                task2.addOnSuccessListener(SignUpActivity.this, taskSnapshot -> {

                                    @SuppressWarnings("VisibleForTests") Uri s = taskSnapshot.getDownloadUrl();

                                    u.setProfilePicURL(s.toString());
                                    Log.d("DEMO", u.toString());
                                    createUser(u);
                                });
                            }
                        }


                    } else {
                        Toast.makeText(SignUpActivity.this, "Registration Unsuccessful", Toast.LENGTH_SHORT).show();
                        pd.dismiss();
                    }
                });
            }
        });
    }

    private void createUser(User u) {
        muid.child(u.getKey()).setValue(u);

        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(editTextFName.getText() + " " + editTextLName.getText())
                .setPhotoUri(user.getPhotoUrl())
                .build();

        user.updateProfile(profileChangeRequest).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(SignUpActivity.this, "User Created", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(i);
                finish();
                pd.dismiss();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filepath = data.getData();
            Picasso.with(SignUpActivity.this).load(data.getData()).noPlaceholder().centerCrop().fit()
                    .into(profilePic);
        }
    }

    private boolean validate() {
        if (editTextPwd.length() != 0 && editTextRepeatPwd.length() != 0 && editTextFName.length() != 0 && editTextEmail.length() != 0 && editTextLName.length() != 0) {
            return editTextPwd.getText().toString().equals(editTextRepeatPwd.getText().toString());
        } else
            return false;
    }
}
