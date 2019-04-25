package example.com.googlesigninexample;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    GoogleSignInOptions googleSignInOptions;
    Button signIn,signOut;
    GoogleSignInClient mGoogleSignInClient;
    private static final int SIGN_IN_ID = 1112;
    private FirebaseAuth mAuth;
    TextView email,userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signIn = findViewById(R.id.signIn);
        signOut = findViewById(R.id.signOut);
        email = findViewById(R.id.email);
        userId = findViewById(R.id.userId);

        signIn.setOnClickListener(this);
        signOut.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();

        googleSignInOptions = new GoogleSignInOptions.Builder()
                                    .requestIdToken(getString(R.string.webclient_id))
                                    .requestEmail()
                                    .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,googleSignInOptions);

    }


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser currentUser) {

        if (currentUser != null) {
            email.setText(currentUser.getEmail());
            userId.setText(currentUser.getUid());

            signIn.setVisibility(View.GONE);
            signOut.setVisibility(View.VISIBLE);
        } else {

            email.setText("user Email");
            userId.setText("user Id");
            signIn.setVisibility(View.VISIBLE);
            signOut.setVisibility(View.GONE);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.signIn:
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent,SIGN_IN_ID);
                break;

            case R.id.signOut:

                signOutUser();

                break;
        }
    }

    private void signOutUser() {

        mAuth.signOut();

        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                updateUI(null);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_ID){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {

                GoogleSignInAccount account = task.getResult(ApiException.class);
                fireBaseAuthWithGoogle(account);

            }catch (ApiException e){
                Log.e("Exception",e.getMessage());
                if (e.getStatusCode() == 12500){
                    // show your own AlertDialog for example:
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    // set the message
                    builder.setMessage("This app use google play services only for optional features")
                            .setTitle("Do you want to update?"); // set a title

                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked OK button

                            //final String appPackageName = "com.google.android.gms";
                            final String appPackageName = "com.google.android.gms&hl=en_IN";
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                            }catch (android.content.ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                            }
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        }
    }

    private void fireBaseAuthWithGoogle(GoogleSignInAccount account) {

        Log.e("FireBaseAuthGoogle",account.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){

                                FirebaseUser user = mAuth.getCurrentUser();
                            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(MainActivity.this);
                            if (acct != null) {
                                String personName = acct.getDisplayName();
                                String personGivenName = acct.getGivenName();
                                String personFamilyName = acct.getFamilyName();
                                String personEmail = acct.getEmail();
                                String personId = acct.getId();
                                Uri personPhoto = acct.getPhotoUrl();
                            }
                                updateUI(user);

                        }else {

                            updateUI(null);

                        }

                    }
                });

    }
}
