package org.rightnowmedia.oauth.oauthtest;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private Context context;
    private AuthPreferences authPreferences;
    private AccountManager accountManager;
    private static final int PERMISSION_REQUEST_GET_ACCOUNTS = 55;
    private static final int ACCOUNT_CODE = 44;
    private static final int AUTHORIZATION_CODE = 33;



    private final String SCOPE = "https://www.googleapis.com/auth/googletalk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        context = getApplicationContext();

        accountManager = AccountManager.get(context);
        authPreferences = new AuthPreferences(this);

        int permissionCheckGetAccounts = ContextCompat.checkSelfPermission(this,
                Manifest.permission.GET_ACCOUNTS);
        int permissionCheckAccountManager = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCOUNT_MANAGER);

        if (permissionCheckGetAccounts != PackageManager.PERMISSION_GRANTED
                || permissionCheckAccountManager != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.GET_ACCOUNTS,
                    Manifest.permission.ACCOUNT_MANAGER
            },  PERMISSION_REQUEST_GET_ACCOUNTS);
        } else {
            doTheThing();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_GET_ACCOUNTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    doTheThing();

                } else {

                    // booooooo
                }
                return;
            }
        }
    }

    private void doTheThing() {
//        if (authPreferences.getUser() != null
//                && authPreferences.getToken() != null) {
//            doCoolAuthenticatedStuff();
//        } else {

            Intent intent = AccountManager.newChooseAccountIntent(null, null,
                    new String[] { "com.google" }, false, null, null, null, null);
            startActivityForResult(intent, ACCOUNT_CODE);
 //       }
    }

    private void doCoolAuthenticatedStuff() {
        Toast.makeText(context, "YOU DID IT!", Toast.LENGTH_SHORT).show();
        AppCompatImageView star = (AppCompatImageView) findViewById(R.id.star);
        star.setVisibility(View.VISIBLE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == AUTHORIZATION_CODE) {
                requestToken();
            } else if (requestCode == ACCOUNT_CODE) {
                String accountName = data
                        .getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                authPreferences.setUser(accountName);

                // invalidate old tokens which might be cached. we want a fresh
                // one, which is guaranteed to work
                invalidateToken();

                requestToken();
            }
        }
    }

    private void invalidateToken() {
        AccountManager accountManager = AccountManager.get(this);
        accountManager.invalidateAuthToken("com.google",
                authPreferences.getToken());

        authPreferences.setToken(null);
    }

    private void requestToken() {
        Account userAccount = null;
        String user = authPreferences.getUser();
        TextView text = (TextView) findViewById(R.id.mainText);

        for (Account account : accountManager.getAccountsByType("com.google")) {
            if (account.name.equals(user)) {
                userAccount = account;
                CharSequence updateText = text.getText() + "\n" + account.name;
                text.setText(updateText);
                break;
            }
        }

        accountManager.getAuthToken(userAccount, "oauth2:" + SCOPE, null, this,
                new OnTokenAcquired(), null);
    }


    private class OnTokenAcquired implements AccountManagerCallback<Bundle> {

        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            try {
                Bundle bundle = result.getResult();

                Intent launch = (Intent) bundle.get(AccountManager.KEY_INTENT);
                if (launch != null) {
                    startActivityForResult(launch, AUTHORIZATION_CODE);
                } else {
                    String token = bundle
                            .getString(AccountManager.KEY_AUTHTOKEN);

                    authPreferences.setToken(token);

                    doCoolAuthenticatedStuff();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
