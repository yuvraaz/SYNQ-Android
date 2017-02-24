package fm.synq.synqhttplib;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Kjartan on 24.02.2017.
 */

public class SynqHttpClient {
    private static final String BASE_URL = "http://192.168.1.100:8080/";
    private static final String PREFERENCES_KEY = "fm.synq.synqhttplib.PREFERENCES_FILE_KEY";
    private String currentUserID;

    public SynqHttpClient() {

    }

    /**
     *  Create a new user
     *
     */
    public void createUser(
            String userName,
            String userPassword,
            final SynqResponseHandler handler,
            final Context context
    ) {
        Ion.with(context)
                .load("POST", BASE_URL + "users/")
                .setMultipartParameter("username", userName)
                .setMultipartParameter("password", userPassword)
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> result) {
                        Log.e("f", "Res: " + result.getHeaders().message());

                        if(e != null){
                            handler.onFailure(e);
                        }
                        else if(result.getHeaders().code() == 400 || result.getHeaders().code() == 500){
                            // a HTTP 400 or 500 was returned -- extract error message
                            handler.onError(result.getResult().get("message").getAsString());
                        }
                        else {

                            // Get userID
                            if (result.getResult().get("user") != null) {
                                currentUserID = result.getResult().get("user").getAsString();

                                // Store userID to be available when logging in user next time
                                storeUserIdInPreferences(context);
                            }
                            else {
                                Log.e("f", "No UserID returned");
                                handler.onError("Error: No userID returned");
                                return;
                            }

                            handler.onSuccess(result.getResult());
                        }
                    }
                });
    }


    public void getUsers(
            final SynqResponseHandler handler,
            Context context
    ) {
        Ion.with(context)
                .load("GET", BASE_URL + "users/")
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> result) {
                        Log.e("f", "Res: " + result.getHeaders().message());

                        if(e != null){
                            handler.onFailure(e);
                        }
                        else if(result.getHeaders().code() == 400){
                            // a HTTP 400 was returned -- extract error
                            handler.onError(result.getResult().get("message").getAsString());
                        }
                        else {
                            handler.onSuccess(result.getResult());
                        }
                    }
                });
    }


    public void loginUser(
            String userName,
            String userPassword,
            final SynqResponseHandler handler,
            Context context
    ) {
        Ion.with(context)
                .load("POST", BASE_URL + "login/")
                .setMultipartParameter("username", userName)
                .setMultipartParameter("password", userPassword)
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> result) {
                        Log.e("f", "Res: " + result.getHeaders().message());

                        if(e != null){
                            handler.onFailure(e);
                        }
                        else if(result.getHeaders().code() == 401){
                            // a HTTP 401 was returned -- Unauthorized, wrong username or password
                            handler.onError("Unauthorized");
                        }
                        else {
                            handler.onSuccess(result.getResult());
                        }
                    }
                });
    }


    public void logoutUser(
            final SynqResponseHandler handler,
            Context context
    ) {
        Ion.with(context)
                .load("GET", BASE_URL + "logout/")
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> result) {
                        Log.e("f", "Res: " + result.getHeaders().message());

                        if(e != null){
                            handler.onFailure(e);
                        }
                        else if(result.getHeaders().code() == 400){
                            // a HTTP 400 was returned -- extract error
                            handler.onError(result.getResult().get("message").getAsString());
                        }
                        else {
                            handler.onSuccess(result.getResult());
                        }
                    }
                });
    }

    /**
     *  Create a new video
     *
     *  @param handler
     */
    public void createVideo(
            final SynqResponseHandler handler,
            Context context
    ) {
        // Get stored userID from SharedPreferences
        String userID = getUserIdFromPreferences(context);
        if (userID == null) {
            Log.e("f", "Error, userID is null");
            return;
        }

        // Build the request url containing the user id, like this:
        // "/users/9/videos/"
        String requestUrl = BASE_URL + "users/" + userID + "/videos/";

        Ion.with(context)
                .load("POST", requestUrl)
                .asJsonObject()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonObject>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonObject> result) {
                        Log.e("f", "Res: " + result.getHeaders().message());

                        if(e != null){
                            handler.onFailure(e);
                        }
                        else if(result.getHeaders().code() == 400){
                            // a HTTP 400 was returned -- extract error
                            handler.onError(result.getResult().get("message").getAsString());
                        }
                        else {
                            handler.onSuccess(result.getResult());
                        }
                    }
                });
    }

    private void storeUserIdInPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.user_id_key), currentUserID);
        editor.apply();
    }

    private String getUserIdFromPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getString(context.getString(R.string.user_id_key), null);
    }

}
