package com.rahtech.ideashub;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ContentLoadingProgressBar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AdditionalUserInfo;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignIn Activity" ;
    SignInButton googleSignInbutton;
    public  GoogleSignInClient googleSignInClient;
    private static final int G_SIGN_In=1001;
    private FirebaseAuth firebaseAuth=FirebaseAuth.getInstance();
    private FirebaseUser currentUser=firebaseAuth.getCurrentUser();
    private DatabaseReference firebaseReference=FirebaseDatabase.getInstance().getReference();
    private DatabaseReference userDatabaseReference= firebaseReference.child("user_details");
    ProgressBar loginProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        googleSignInbutton=findViewById(R.id.google_sign_in);
        loginProgressBar=findViewById(R.id.loginProgressBar);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        googleSignInClient= GoogleSignIn.getClient(this,gso);

        googleSignInbutton.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, G_SIGN_In);
            }
        });

    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Log.d(TAG, "signInWithCredential:success");
                            checkUserDataExistence();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignInActivity.this, "SignIn Failed.. try Again", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void checkUserDataExistence(){
        FirebaseUser user = firebaseAuth.getCurrentUser();
        userDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loginProgressBar.setVisibility(View.GONE);
                if (user!=null && snapshot.hasChild(user.getUid())) {
                    Intent mainActivityIntent=new Intent(SignInActivity.this,MainActivity.class);
                    startActivity(mainActivityIntent);
                    finish();
                }
                else {
                    Intent profileIntent = new Intent(SignInActivity.this, ProfileActivity.class);
                    startActivity(profileIntent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        loginProgressBar.setVisibility(View.VISIBLE);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == G_SIGN_In) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId()+" "+account.getDisplayName()
                        +"given name" +account.getGivenName()+"family name" +account.getFamilyName());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }


}