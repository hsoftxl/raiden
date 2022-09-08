package com.google.play.games;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.SnapshotsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.cocos2dx.cpp.AppActivity;
import org.sean.util.LogUtil;

public class GameServiceActivity extends AppActivity {
    private static final String TAG = GameServiceActivity.class.getName();

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;

    // Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build();

    // Client used to sign in with Google APIs
    private GoogleSignInClient mGoogleSignInClient;

    // The currently signed in account, used to check the account has changed outside of this activity when resuming.
    GoogleSignInAccount mSignedInAccount = null;

    // Client used to interact with Google Snapshots.
    private SnapshotsClient mSnapshotsClient = null;

    private void isGooglePlayServicesAvailable(Context context) {
        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the client used to sign in.
        mGoogleSignInClient = GoogleSignIn.getClient(this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                        // Since we are using SavedGames, we need to add the SCOPE_APPFOLDER to access Google Drive.
                        .requestScopes(Drive.SCOPE_APPFOLDER)
                        .build());

//        startSignInIntent();
    }

    /**
     * Start a sign in activity.  To properly handle the result, call tryHandleSignInResult from
     * your Activity's onActivityResult function
     */
    public void startSignInIntent() {
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        // Since the state of the signed in user can change when the activity is not active
        // it is recommended to try and sign in silently from when the app resumes.
//        signInSilently();
    }

    public void signInSilently() {
        LogUtil.d(TAG, "signInSilently()");

        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInSilently(): success");
                            onConnected(task.getResult());
                        } else {
                            Log.d(TAG, "signInSilently(): failure", task.getException());
                            onDisconnected();
                        }
                    }
                });
    }

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        Log.d(TAG, "onConnected(): connected to Google APIs");
        if (mSignedInAccount != googleSignInAccount) {

            mSignedInAccount = googleSignInAccount;

            onAccountChanged(googleSignInAccount);
        } else {
            Log.d(TAG, "updateUi");
        }
    }

    private void onDisconnected() {

        Log.d(TAG, "onDisconnected()");

        mSnapshotsClient = null;
    }

    private void onAccountChanged(GoogleSignInAccount googleSignInAccount) {
        mSnapshotsClient = Games.getSnapshotsClient(this, googleSignInAccount);

        // Sign-in worked!
        LogUtil.iTag(TAG, "Sign-in successful! Loading game state from cloud.");

    }

    /**
     * You can capture the Snapshot selection intent in the onActivityResult method. The result
     * either indicates a new Snapshot was created (EXTRA_SNAPSHOT_NEW) or was selected.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                onConnected(account);
            } catch (ApiException apiException) {
                apiException.printStackTrace();
                String message = apiException.getMessage();
                if (message == null || message.isEmpty()) {
                    message = "登录失败";
                }

                onDisconnected();

//                new AlertDialog.Builder(this)
//                        .setMessage(message)
//                        .setNeutralButton(android.R.string.ok, null)
//                        .show();
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }
}
