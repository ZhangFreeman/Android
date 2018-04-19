package com.android.my.zhang.dribbview.dribbble.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.my.zhang.dribbview.DribbViewApplication;
import com.android.my.zhang.dribbview.model.Bucket;
import com.android.my.zhang.dribbview.model.Comment;
import com.android.my.zhang.dribbview.model.Follow;
import com.android.my.zhang.dribbview.model.Like;
import com.android.my.zhang.dribbview.model.Shot;
import com.android.my.zhang.dribbview.model.User;
import com.android.my.zhang.dribbview.utils.ModelUtils;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// for auth, request and response mainly
public class Dribbble {

    private static final String TAG = "Dribbble API";

    // Dribbble loads everything in a 12-per-page manner
    public static final int COUNT_PER_PAGE = 12;

    //API inspired and not available any more
    private static final String API_URL = "https://api.dribbble.com/v1/";

    private static final String BUCKETS_END_POINT = API_URL + "buckets";
    private static final String SHOTS_END_POINT = API_URL + "shots";
    private static final String USER_END_POINT = API_URL + "user";

    private static final String SP_AUTH = "auth";

    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_NAME = "name";
    private static final String KEY_USER = "user";
    private static final String KEY_SHOT_ID = "shot_id";

    private static final TypeToken<List<Shot>> SHOT_LIST_TYPE = new TypeToken<List<Shot>>(){};
    private static final TypeToken<Bucket> BUCKET_TYPE = new TypeToken<Bucket>(){};
    private static final TypeToken<List<Bucket>> BUCKET_LIST_TYPE = new TypeToken<List<Bucket>>(){};
    private static final TypeToken<User> USER_TYPE = new TypeToken<User>(){};
    private static final TypeToken<List<Like>> LIKE_LIST_TYPE = new TypeToken<List<Like>>(){};
    private static final TypeToken<List<Comment>> COMMENT_LIST = new TypeToken<List<Comment>>(){};
    private static final TypeToken<List<User>> USER_LIST_TYPE = new TypeToken<List<User>>(){};
    private static final TypeToken<List<Follow>> FOLLOW_LIST_TYPE = new TypeToken<List<Follow>>(){};

    //private static OkHttpClient client = new OkHttpClient();
    static int cacheSize = 10 * 1024 * 1024; // 10MB
    private static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .cache(new Cache(DribbViewApplication.getAppContext().getCacheDir(), cacheSize))
            .build();


    private static String accessToken;
    private static User user;

    private static Request.Builder authRequestBuilder(String url) {
        return new Request.Builder()
                .addHeader("Authorization", "Bearer " + accessToken)
                .url(url);
    }

    private static Response makeRequest(Request request) throws IOException {
        Response response = client.newCall(request).execute();
        if (response != null) Log.d(TAG, response.header("X-RateLimit-Remaining"));
        //if (response != null) Log.d("inside response", response.toString());
        return response;
    }

    private static Response makeGetRequest(String url) throws IOException {
        Request request = authRequestBuilder(url).build();
        return makeRequest(request);
    }

    private static <T> T parseResponse(Response response,
                                       TypeToken<T> typeToken) throws IOException, JsonSyntaxException {
        String responseString = response.body().string();
        //for long debug words
        /*
        int i = 0; final int STEP = 1024;
        while(i < responseString.length()) {
            Log.d(TAG, responseString.substring(i, responseString.length()-i > STEP ? i + STEP : responseString.length()));
            i += STEP;
        }*/
        Log.d(TAG, responseString);
        //Log.d(TAG, ModelUtils.toObject(responseString, typeToken).toString());
        return ModelUtils.toObject(responseString, typeToken);
    }

    private static Response makePostRequest(String url,
                                            RequestBody requestBody) throws IOException {
        Request request = authRequestBuilder(url)
                .post(requestBody)
                .build();
        return makeRequest(request);
    }

    private static Response makePutRequest(String url,
                                           RequestBody requestBody) throws IOException {
        Request request = authRequestBuilder(url)
                .put(requestBody)
                .build();
        return makeRequest(request);
    }

    private static Response makeDeleteRequest(String url,
                                              RequestBody requestBody) throws IOException {
        Request request = authRequestBuilder(url)
                .delete(requestBody)
                .build();
        return makeRequest(request);
    }

    public static void init(@NonNull Context context) {
        accessToken = loadAccessToken(context);
        if (accessToken != null) {
            user = loadUser(context);
        }
    }

    public static boolean isLoggedIn() {
        return accessToken != null;
    }

    public static void login(@NonNull Context context,
                             @NonNull String accessToken) throws IOException, JsonSyntaxException {
        Dribbble.accessToken = accessToken;
        storeAccessToken(context, accessToken);

        Dribbble.user = getUser();
        storeUser(context, user);
    }

    public static void logout(@NonNull Context context) {
        storeAccessToken(context, null);
        storeUser(context, null);

        accessToken = null;
        user = null;
    }

    public static User getCurrentUser() {
        return user;
    }

    public static void storeAccessToken(@NonNull Context context, @Nullable String token) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(
                SP_AUTH, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_ACCESS_TOKEN, token).apply();
    }

    public static String loadAccessToken(@NonNull Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(
                SP_AUTH, Context.MODE_PRIVATE);
        return sp.getString(KEY_ACCESS_TOKEN, null);
    }

    public static void storeUser(@NonNull Context context, @Nullable User user) {
        ModelUtils.save(context, KEY_USER, user);
    }

    public static User loadUser(@NonNull Context context) {
        return ModelUtils.read(context, KEY_USER, new TypeToken<User>(){});
    }

    public static User getUser() throws IOException, JsonSyntaxException {
        return parseResponse(makeGetRequest(USER_END_POINT), USER_TYPE);
    }

    private static void checkStatusCode(Response response,
                                        int statusCode) throws IOException {
        if (response.code() != statusCode) {
            throw new IOException(response.message());
        }
    }

    public static List<Comment> getComments(String shotId) throws IOException, JsonSyntaxException {
        String url = SHOTS_END_POINT + "/" + shotId + "/comments";
        return parseResponse(makeGetRequest(url), COMMENT_LIST);
    }

    public static List<Bucket> getUserBucketsAll() throws IOException, JsonSyntaxException {
        String url = USER_END_POINT + "/" + "buckets?per_page=" + Integer.MAX_VALUE;
        return parseResponse(makeGetRequest(url), BUCKET_LIST_TYPE);
    }

    public static List<Shot> getShots(int page) throws IOException, JsonSyntaxException {
        String url = SHOTS_END_POINT + "?page=" + page;
        Log.d("getShots", url);
        return parseResponse(makeGetRequest(url), SHOT_LIST_TYPE);
    }


    public static List<Shot> getLikeShots(int page) throws IOException, JsonSyntaxException {
        String url = USER_END_POINT + "/likes" + "?page=" + page;
        Log.d("getLikeShots", url);
        List<Like> likes = parseResponse(makeGetRequest(url), LIKE_LIST_TYPE);
        List<Shot> shots = new ArrayList<>();
        for (Like l : likes)
            shots.add(l.shot);
        return shots;
    }

    public static List<User> getFollowUsers(int page) throws IOException, JsonSyntaxException {
        String url = USER_END_POINT + "/following?page=" + page;
        Log.d("getFollows", url);
        List<Follow> follows = parseResponse(makeGetRequest(url), FOLLOW_LIST_TYPE);
        List<User> users = new ArrayList<>();
        for (Follow f : follows)
            users.add(f.followee);
        //shots.addAll(follow);
        Log.d("getFollows size", "" + users.size());
        return users;
    }

    public static List<Shot> geFollowShots(int page) throws IOException, JsonSyntaxException {
        String url = USER_END_POINT + "/following/shots" + "?page=" + page;
        Log.d("getFollowShots", url);
        List<Shot> shots = parseResponse(makeGetRequest(url), SHOT_LIST_TYPE);
        //List<Shot> shots = new ArrayList<>();
        //shots.addAll(follow);
        return shots;
    }

    public static List<Shot> getBucketShots(String id, int page) throws IOException, JsonSyntaxException {
        String url = BUCKETS_END_POINT + "/" + id +  "/shots?page=" + page;
        Log.d("getBucketShots", url);
        return parseResponse(makeGetRequest(url), SHOT_LIST_TYPE);
    }

    public static List<Shot> getAuthorShots(String id, int page) throws IOException, JsonSyntaxException {
        String url = USER_END_POINT + "s/" + id + "/shots?page=" + page;
        Log.d("getAuthorShots", url);
        return parseResponse(makeGetRequest(url), SHOT_LIST_TYPE);
    }


    public static List<Bucket> getUserBuckets(int page) throws IOException, JsonSyntaxException {
        String url = USER_END_POINT + "/" + "buckets?page=" + page;
        Log.d("getUserBuckets", url);
        return parseResponse(makeGetRequest(url), BUCKET_LIST_TYPE);
    }

    public static List<Bucket> getShotBucket(String id) throws IOException ,JsonSyntaxException{
        String url = SHOTS_END_POINT + "/" + id + "/buckets?per_page=" + Integer.MAX_VALUE;
        return parseResponse(makeGetRequest(url), BUCKET_LIST_TYPE);
    }

    public static boolean getIfFollow(String id) throws IOException ,JsonSyntaxException{
        String url = USER_END_POINT + "/following/" + id;
        Response response = makeGetRequest(url);
        Log.d("Check if like ", response.toString());
        return response.code() == 204;
    }

    public static boolean getIfLike(String id) throws IOException ,JsonSyntaxException{
        String url = SHOTS_END_POINT + "/" + id + "/like";
        Response response = makeGetRequest(url);
        Log.d("Check if like ", response.toString());
        return response.code() == 200;
    }

    public static boolean postLike(String id) throws IOException ,JsonSyntaxException{
        String url = SHOTS_END_POINT + "/" + id + "/like";
        FormBody formBody = new FormBody.Builder().build();
        Response response = makePostRequest(url, formBody);
        Log.d("postLike", "response " + response.toString());
        return response.code() == 201;
    }

    public static boolean deleteLike(String id) throws IOException ,JsonSyntaxException{
        String url = SHOTS_END_POINT + "/" + id + "/like";
        FormBody formBody = new FormBody.Builder().build();
        Response response = makeDeleteRequest(url, formBody);
        Log.d("deleteLike", "response " + response.toString());
        return response.code() == 204;
    }

    public static boolean postFollow(String id) throws IOException ,JsonSyntaxException{
        String url = USER_END_POINT + "s/" + id + "/follow";
        FormBody formBody = new FormBody.Builder().build();
        Response response = makePutRequest(url, formBody);
        Log.d("postFollow", "response " + response.toString());
        return response.code() == 204;
    }

    public static boolean deleteFollow(String id) throws IOException ,JsonSyntaxException{
        String url = USER_END_POINT + "s/" + id + "/follow";
        FormBody formBody = new FormBody.Builder().build();
        Response response = makeDeleteRequest(url, formBody);
        Log.d("deleteFollow", "response " + response.toString());
        return response.code() == 204;
    }

    /**
     * Add a shot to a bucket
     * @param bucketId
     * @param shotId
     * @throws IOException
     * @throws JsonSyntaxException
     */
    public static void addBucketShot(@NonNull String bucketId,
                                     @NonNull String shotId) throws IOException, JsonSyntaxException {
        String url = BUCKETS_END_POINT + "/" + bucketId + "/shots";
        FormBody formBody = new FormBody.Builder()
                .add(KEY_SHOT_ID, shotId)
                .build();

        Response response = makePutRequest(url, formBody);
        checkStatusCode(response, HttpURLConnection.HTTP_NO_CONTENT);
    }

    public static void removeBucketShot(@NonNull String bucketId,
                                        @NonNull String shotId) throws IOException, JsonSyntaxException {
        String url = BUCKETS_END_POINT + "/" + bucketId + "/shots";
        FormBody formBody = new FormBody.Builder()
                .add(KEY_SHOT_ID, shotId)
                .build();

        Response response = makeDeleteRequest(url, formBody);
        checkStatusCode(response, HttpURLConnection.HTTP_NO_CONTENT);
    }

    public static Bucket newBucket(@NonNull String name,
                                   @NonNull String description) throws IOException, JsonSyntaxException {
        FormBody formBody = new FormBody.Builder()
                .add(KEY_NAME, name)
                .add(KEY_DESCRIPTION, description)
                .build();
        return parseResponse(makePostRequest(BUCKETS_END_POINT, formBody), BUCKET_TYPE);
    }
}
