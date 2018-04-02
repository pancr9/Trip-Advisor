package builderspace.tripadvisor.controller.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
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

public class MainActivity extends AppCompatActivity {

    public static final String DEFAULT_PROFILE_LINK = "https://firebasestorage.googleapis.com/v0/b/tripadvisor-a0e21.appspot.com/o/profilePictures%2FDefaultProfilePic.png?alt=media&token=029bc935-013d-45eb-9e13-1f54a6ea81ea";
    public static final String DEFAULT_TRIP_LINK = "https://firebasestorage.googleapis.com/v0/b/tripadvisor-a0e21.appspot.com/o/tripPictures%2FDefaultLocationPic.png?alt=media&token=3b64f8ad-22cb-4c98-b76e-32dc4a8747e7";
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference muidUser;
    private EditText editTextUsername;
    private EditText editTextPassword;
    private GoogleApiClient mGoogleApiClient;
    private String username;
    private String password;
    private ArrayList<User> allUsers = new ArrayList<>();
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

/*        setTitle("Plan my Trip");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setLogo(ic_launcher);*/

        fAuth = FirebaseAuth.getInstance();

        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonSignUp = findViewById(R.id.buttonMainSignUp);
        SignInButton signInButton = findViewById(R.id.buttonGoogleSignIn);

        editTextPassword = findViewById(R.id.editTextLoginPassword);
        editTextUsername = findViewById(R.id.editTextLoginUserName);

        fAuth = FirebaseAuth.getInstance();
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                Intent i = new Intent(MainActivity.this, TripsActivity.class);
                startActivity(i);
                finish();
                Log.d("mAuthListener", "onAuthStateChanged:signed_in:" + user.getUid());
            } else {
                Log.d("mAuthListener", "onAuthStateChanged:signed_out");
            }
        };

        muidUser = databaseReference.child("USER");
        muidUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    allUsers.add(d.getValue(User.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("Demo", "Error in database");
            }
        });

        buttonLogin.setOnClickListener(v -> {
            if (validate()) {
                username = editTextUsername.getText().toString();
                password = editTextPassword.getText().toString();

                fAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(MainActivity.this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(MainActivity.this, TripsActivity.class);
                        startActivity(i);
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Login Unsuccessful", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });

        buttonSignUp.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(i);
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, connectionResult -> {

                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signInButton.setOnClickListener(v -> signIn());
    }

    @Override
    protected void onStart() {
        fAuth.addAuthStateListener(mAuthListener);
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (mAuthListener != null) {
            fAuth.removeAuthStateListener(mAuthListener);
        }
        super.onStop();
    }

    private boolean validate() {
        boolean valid = true;
        String email = editTextUsername.getText().toString();
        if (TextUtils.isEmpty(email)) {
            valid = false;
            editTextUsername.setError("Required");
        }
        String pwd = editTextPassword.getText().toString();
        if (TextUtils.isEmpty(pwd)) {
            valid = false;
            editTextPassword.setError("Required");
        }

        return valid;
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("DEMO", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            boolean userExists = false;
            User user = new User();

            for (User singleUser : allUsers) {
                if (acct.getEmail().equals(singleUser.getEmail())) {
                    user = singleUser;
                    userExists = true;
                    break;
                }
            }

            if (!userExists) {
                user.setEmail(acct.getEmail());
                user.setfName(acct.getGivenName());
                user.setlName(acct.getFamilyName());
                if (acct.getPhotoUrl() == null) {
                    user.setProfilePicURL(DEFAULT_PROFILE_LINK);
                } else {
                    user.setProfilePicURL(acct.getPhotoUrl().toString());
                }
                user.setKey(muidUser.push().getKey());
                muidUser.child(user.getKey()).setValue(user);
            }

            Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(MainActivity.this, TripsActivity.class);
            i.putExtra("CURRENT_USER", user);
            startActivity(i);
            finish();

        } else {
            Log.d("DEMO", "Google SignIn failed.");
        }
    }
}
