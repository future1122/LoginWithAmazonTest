package com.example.damon.loginwithamazontest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.api.Listener;
import com.amazon.identity.auth.device.api.authorization.AuthCancellation;
import com.amazon.identity.auth.device.api.authorization.AuthorizationManager;
import com.amazon.identity.auth.device.api.authorization.AuthorizeListener;
import com.amazon.identity.auth.device.api.authorization.AuthorizeRequest;
import com.amazon.identity.auth.device.api.authorization.AuthorizeResult;
import com.amazon.identity.auth.device.api.authorization.ProfileScope;
import com.amazon.identity.auth.device.api.authorization.Scope;
import com.amazon.identity.auth.device.api.authorization.User;
import com.amazon.identity.auth.device.api.workflow.RequestContext;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "Damon";
    RequestContext requestContext;
    Button mAVSLoginBtn;
    Button mAVSLogoutBtn;
    TextView mInfoTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestContext = RequestContext.create(this);
        requestContext.registerListener(new AuthorizeListener() {
            @Override
            public void onSuccess(AuthorizeResult authorizeResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fetchUserProfile();
                        setLoginsuccess();
                    }
                });
                Log.i(TAG,"Login success!");
            }

            @Override
            public void onError(AuthError authError) {
                Log.i(TAG,"login error :"+authError.getMessage());
            }

            @Override
            public void onCancel(AuthCancellation authCancellation) {
                Log.i(TAG,"login cancel: "+authCancellation.toString());
            }
        });
        initUI();
    }

    private void fetchUserProfile(){
        User.fetch(this, new Listener<User, AuthError>() {
            @Override
            public void onSuccess(User user) {
                Log.i(TAG,"use fetch success");
                final String name = user.getUserName();
                final String email = user.getUserEmail();
                final String account = user.getUserId();
                final String zipcode  = user.getUserPostalCode();
                Log.i(TAG,"name :" +name);
                Log.i(TAG,"email :" + email);
                Log.i(TAG,"account :" + account);
                Log.i(TAG,"zipcode :" + zipcode);

            }

            @Override
            public void onError(AuthError authError) {
                Log.i(TAG,"user fetch error: " +authError.getMessage());
            }
        });
    }

    private void initUI() {
        mAVSLoginBtn = (Button) findViewById(R.id.avs_login_btn);
        mAVSLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthorizationManager.authorize(new AuthorizeRequest
                        .Builder(requestContext)
                        .addScopes(ProfileScope.profile(),ProfileScope.postalCode())
                        .build()
                );
            }
        });
        mAVSLogoutBtn = (Button) findViewById(R.id.avs_logout_btn);
        mAVSLogoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthorizationManager.signOut(MainActivity.this, new Listener<Void, AuthError>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setLogout();
                            }
                        });
                        Log.i(TAG,"Log out success!");
                    }

                    @Override
                    public void onError(AuthError authError) {
                        Log.i(TAG,"log out error :"+authError.getMessage());
                    }
                });
            }
        });
        mInfoTextView = (TextView) findViewById(R.id.info_tv);
    }

    private void setLoginsuccess() {
        mAVSLoginBtn.setVisibility(View.INVISIBLE);
        mAVSLogoutBtn.setVisibility(View.VISIBLE);
        mInfoTextView.setText("Login success");
    }

    private void setLogout(){
        mAVSLoginBtn.setVisibility(View.VISIBLE);
        mAVSLogoutBtn.setVisibility(View.INVISIBLE);
        mInfoTextView.setText("please login avs.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestContext.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Scope[] scopes = new Scope[]{ProfileScope.profile(),ProfileScope.postalCode()};
        AuthorizationManager.getToken(this, scopes, new Listener<AuthorizeResult, AuthError>() {
            @Override
            public void onSuccess(AuthorizeResult authorizeResult) {
                if(authorizeResult.getAccessToken()!=null){
                    Log.i(TAG,"get token success!");
                    setLoginsuccess();
                    fetchUserProfile();
                }else{
                    Log.i(TAG,"get token fail : token is null");
                }
            }

            @Override
            public void onError(AuthError authError) {
                Log.i(TAG,"get token error :"+authError.getMessage());
            }
        });
    }
}
