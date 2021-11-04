package vn.edu.usth.dbxdemo;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import com.dropbox.core.android.Auth;
import com.dropbox.core.json.JsonReadException;
import com.dropbox.core.oauth.DbxCredential;

import java.util.List;

public abstract class DropboxActivity extends AppCompatActivity {
    private final static boolean USE_TOKEN = true;

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences preferences = getSharedPreferences("dropbox", MODE_PRIVATE);

        if (USE_TOKEN) {
            String serailizedCredental = preferences.getString("credential", null);

            if (serailizedCredental == null) {
                DbxCredential credential = Auth.getDbxCredential();

                if (credential != null) {
                    preferences.edit().putString("credential", credential.toString()).apply();
                    initAndLoadData(credential);
                }
            } else {
                try {
                    DbxCredential credential = DbxCredential.Reader.readFully(serailizedCredental);
                    initAndLoadData(credential);
                } catch (JsonReadException e) {
                    throw new IllegalStateException("Credential data corrupted: " + e.getMessage());
                }
            }

        } else {
            String accessToken = preferences.getString("access-token", null);
            if (accessToken == null) {
                accessToken = Auth.getOAuth2Token();
                if (accessToken != null) {
                    preferences.edit().putString("access-token", accessToken).apply();
                    initAndLoadData(accessToken);
                }
            } else {
                initAndLoadData(accessToken);
            }
        }

        String uid = Auth.getUid();
        String storedUid = preferences.getString("user-id", null);
        if (uid != null && !uid.equals(storedUid)) {
            preferences.edit().putString("user-id", uid).apply();
        }
    }

    private void initAndLoadData(String accessToken) {
        DropboxClientFactory.init(accessToken);
        PicassoClient.init(getApplicationContext(), DropboxClientFactory.getClient());
        loadData();
    }

    private void initAndLoadData(DbxCredential dbxCredential) {
        DropboxClientFactory.init(dbxCredential);
        PicassoClient.init(getApplicationContext(), DropboxClientFactory.getClient());
        loadData();
    }

    protected abstract void loadData();

    protected boolean hasToken() {
        SharedPreferences prefs = getSharedPreferences("dropbox", MODE_PRIVATE);
        if (USE_TOKEN) {
            return prefs.getString("credential", null) != null;
        } else {
            String accessToken = prefs.getString("access-token", null);
            return accessToken != null;
        }
    }

    public static void startOAuth2Authentication(Context context, String app_key, List<String> scope) {
        if (USE_TOKEN) {
            Auth.startOAuth2PKCE(context, app_key, DbxRequestConfigFactory.getRequestConfig(), scope);
        } else {
            Auth.startOAuth2Authentication(context, app_key);
        }
    }
}
