package com.example.moodspace;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

import io.paperdb.Paper;

import static com.example.moodspace.Utils.makeInfoToast;
import static com.example.moodspace.Utils.makeSuccessToast;
import static com.example.moodspace.Utils.makeWarnToast;

public class FollowActivity extends AppCompatActivity
        implements ControllerCallback {
    public static final String FOLLOW_ACTION_KEY = "moodspace.FollowActivity.followActionKey";
    public static final String FOLLOW_ACTION_SEND_REQUEST = "moodspace.FollowActivity.followActionSendRequest";
    public static final String FOLLOW_LISTS_LISTENER_KEY = "moodspace.FollowActivity.followListsListenerKey";
    public static final String TARGET_KEY = "moodspace.FollowActivity.targetKey";

    private final CacheListener cacheListener = CacheListener.getInstance();

    private static final String TAG = FollowActivity.class.getSimpleName();
    private FollowController fc;
    private UserController uc;

    private String username;
    private String unfollowId;
    TabLayout tabs;
    TextView followText;
    AppCompatEditText userField;
    ListView requestList;
    AppCompatButton sendRequestBtn;
    ListView sentRequestList;
    ListView followersList;
    ListView followingList;

    List<String> following;
    List<String> followers;
    List<String> followRequestsFrom;
    List<String> followRequestsTo;
    ArrayAdapter requestAdapter;
    ArrayAdapter followersAdapter;
    ArrayAdapter followingAdapter;
    AnswerRequestAdapter answerAdapter;

    /**
     * The activity in which the user can modify who they follow, and who follows them.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        this.fc = new FollowController(this);
        uc = new UserController(this);
        setContentView(R.layout.activity_follow);
        this.username = getIntent().getExtras().getString(Utils.USERNAME_KEY);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        followText = findViewById(R.id.follow_text);
        userField = findViewById(R.id.username);
        requestList = findViewById(R.id.request_listview);
        sendRequestBtn = findViewById(R.id.request_btn);
        sentRequestList = findViewById(R.id.sent_requests_listview);
        followersList = findViewById(R.id.followers_listview);
        followingList = findViewById(R.id.following_listview);

        // sets up the menu button
        final DrawerLayout drawerLayout = findViewById(R.id.follow_layout);
        toolbar.setNavigationIcon(R.drawable.ic_menu_button);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // sets up navigation viewer (side bar)
        final NavigationView navigationView = findViewById(R.id.nav_view);
        final TextView headerTextView
                = navigationView.getHeaderView(0).findViewById(R.id.header_text_view);
        headerTextView.setText(username);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                drawerLayout.closeDrawers();
                switch (item.getItemId()) {
                    case R.id.nav_item_profile:
                        Intent intent = new Intent(FollowActivity.this, ProfileListActivity.class);
                        intent.putExtra(Utils.USERNAME_KEY, username);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        return true;
                    case R.id.nav_item_feed:
                        Intent intent1 = new Intent(FollowActivity.this, ProfileListActivity.class);
                        intent1.putExtra(Utils.USERNAME_KEY, username);
                        intent1.putExtra("feed", true);
                        intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent1);
                        finish();
                        return true;
                    case R.id.nav_item_following:
                        Intent intent2 = new Intent(FollowActivity.this, FollowActivity.class);
                        intent2.putExtra(Utils.USERNAME_KEY, username);
                        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent2);
                        finish();
                        return true;
                    case R.id.nav_item_map:
                        Intent intent3 = new Intent(FollowActivity.this, MapsActivity.class);
                        intent3.putExtra(Utils.USERNAME_KEY, username);
                        startActivity(intent3);
                        return true;
                    case R.id.nav_item_log_out:
                        Paper.book().delete(UserController.PAPER_USERNAME_KEY);
                        Paper.book().delete(UserController.PAPER_PASSWORD_KEY);
                        Intent loginScreen = new Intent(FollowActivity.this, LoginActivity.class);
                        loginScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(loginScreen);
                        finish();
                        return true;

                    default:
                        return false;
                }
            }
        });

        tabs = findViewById(R.id.tab_layout);
        // when a tab is clicked, show only the views related to that tab
        // when a tab is deselected, hide the views related to that tab
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateUser();
                int pos = tab.getPosition();
                if (pos == 0) {
                    requestList.setVisibility(View.VISIBLE);

                }
                else if (pos == 1){
                    followersList.setAdapter(followersAdapter);
                    followersList.setVisibility(View.VISIBLE);
                }
                else if (pos == 2){
                    followText.setVisibility(View.VISIBLE);
                    userField.setVisibility(View.VISIBLE);
                    sendRequestBtn.setVisibility(View.VISIBLE);
                    sentRequestList.setVisibility(View.VISIBLE);
                }
                else if (pos == 3){
                    followingList.setAdapter(followingAdapter);
                    followingList.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                updateUser();
                int pos = tab.getPosition();
                if (pos == 0) {
                    requestList.setVisibility(View.GONE);

                }
                else if (pos == 1){
                    followersList.setVisibility(View.GONE);
                }
                else if (pos == 2){
                    followText.setVisibility(View.GONE);
                    userField.setVisibility(View.GONE);
                    sendRequestBtn.setVisibility(View.GONE);
                    sentRequestList.setVisibility(View.GONE);
                }
                else if (pos == 3){
                    followingList.setVisibility(View.GONE);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                updateUser();
            }
        });

        // when the button is clicked, send a follow request to the entered username, if the entered
        // username is not a participant that is already followed, or the entered username is not
        // the user's username
        sendRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String addedUser = userField.getText().toString();
                if (following.contains(addedUser)){
                    makeInfoToast(FollowActivity.this, "You Already Follow Them!");
                }
                else if (addedUser.equals(username)){
                    makeInfoToast(FollowActivity.this, "You Can't Follow Yourself!");
                }
                else if (!(addedUser.length() == 0)){
                    // first checks if the user exists (see case USERNAME_EXISTS)
                    Bundle bundle = new Bundle();
                    bundle.putString(FOLLOW_ACTION_KEY, FOLLOW_ACTION_SEND_REQUEST);
                    bundle.putString(TARGET_KEY, addedUser);
                    uc.checkUsernameExists(addedUser, bundle);
                }
            }
        });

        updateUser();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cacheListener.removeListener(FOLLOW_LISTS_LISTENER_KEY);
    }

    /**
     * Call getFollowData and update the users values.
     */
    public void updateUser(){
        fc.getFollowData(username, FOLLOW_LISTS_LISTENER_KEY, new FollowController.GetDataCallback() {
            @Override
            public void callbackFollowData(@NonNull String user, @NonNull List<String> following,
                                           @NonNull List<String> followers,
                                           @NonNull List<String> followRequestsFrom,
                                           @NonNull List<String> followRequestsTo) {

                FollowActivity.this.following = following;
                FollowActivity.this.followers = followers;
                FollowActivity.this.followRequestsFrom = followRequestsFrom;
                FollowActivity.this.followRequestsTo = followRequestsTo;

                answerAdapter = new AnswerRequestAdapter(FollowActivity.this, followRequestsFrom, username, fc);
                requestList.setAdapter(answerAdapter);

                requestAdapter = new ArrayAdapter<>(FollowActivity.this, R.layout.request_content, followRequestsTo);
                sentRequestList.setAdapter(requestAdapter);

                followersAdapter = new ArrayAdapter<>(FollowActivity.this, R.layout.request_content, followers);
                followingAdapter = new ArrayAdapter<>(FollowActivity.this, R.layout.request_content, following);

                followersList.setAdapter(followersAdapter);
                followingList.setAdapter(followingAdapter);

                registerForContextMenu(followingList);
                registerForContextMenu(sentRequestList);
            }
        });
    }

    /**
     * Set up the context menu for unfollowing or cancelling, depending on the clicked view.
     * @param menu
     * @param v
     * @param menuInfo
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.following_listview){
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.unfollow_menu, menu);
            int index = info.position;
            Log.d(TAG, this.following.get(index));
            unfollowId = this.following.get(index);
        }
        else if (v.getId() == R.id.sent_requests_listview){
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.cancel_request, menu);
            int index = info.position;
            Log.d(TAG, this.followRequestsTo.get(index));
            unfollowId = this.followRequestsTo.get(index);
        }
    }

    /**
     * Unfollow or cancelled the long clicked item from the context menu.
     * @param item
     * @return
     */
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.cancel:
                makeSuccessToast(this, "Cancelled");
                this.followRequestsTo.remove(info.position);
                this.fc.removeFollowRequest(username, unfollowId);
                requestAdapter.notifyDataSetChanged();
                return true;

            case R.id.unfollow:
                makeSuccessToast(this, "Unfollowed");
                this.following.remove(info.position);
                this.fc.removeFollower(username, unfollowId);
                followingAdapter.notifyDataSetChanged();
                return true;

            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * Satisfy requirements.
     * @param callbackId
     */
    @Override
    public void callback(CallbackId callbackId) {
        this.callback(callbackId, null);
    }

    /**
     * Satisfy requirements.
     * @param callbackId
     * @param bundle
     */
    @Override
    public void callback(CallbackId callbackId, Bundle bundle) {
        View snackBarView = findViewById(R.id.follow_layout);
        if (callbackId instanceof FollowCallbackId) {
            switch ((FollowCallbackId) callbackId) {
                case ADD_FOLLOWER_COMPLETE:
                    return;
                case ADD_USER_TO_FOLLOWING_FAIL:
                    return;
                case ADD_USER_AS_FOLLOWER_FAIL:
                    return;

                case REMOVE_FOLLOWER_COMPLETE:
                    return;
                case REMOVE_FROM_FOLLOWING_FAIL:
                    return;
                case REMOVE_AS_FOLLOWER_FAIL:
                    return;

                case ADD_FOLLOW_REQUEST_COMPLETE:
                    updateUser();
                    return;
                case ADD_FOLLOW_REQUEST_TO_FAIL:
                    return;
                case ADD_FOLLOW_REQUEST_FROM_FAIL:
                    return;

                case REMOVE_FOLLOW_REQUEST_COMPLETE:
                    return;
                case REMOVE_FOLLOW_REQUEST_TO_FAIL:
                    return;
                case REMOVE_FOLLOW_REQUEST_FROM_FAIL:
                    return;

                case ACCEPT_FOLLOW_REQUEST_COMPLETE:
                    return;
                case FOLLOWEE_MOOD_READ_FAIL:
                    return;
                default:
                    return;
            }
        } else if (callbackId instanceof UserCallbackId) {
            switch ((UserCallbackId) callbackId) {
                case USERNAME_EXISTS:
                    String target;
                    String followAction;

                    target = Utils.getStringFromBundle(bundle, TARGET_KEY, snackBarView,
                            "Unexpected error: target key bundle should not be null",
                            "Unexpected error: target key result should not contain a null string");
                    if (target == null) {
                        return;
                    }

                    followAction = Utils.getStringFromBundle(bundle, FOLLOW_ACTION_KEY, snackBarView,
                        "Unexpected error: follow action key bundle should not be null",
                        "Unexpected error: follow action key result should not contain a null string");
                    if (followAction == null) {
                        return;
                    }

                    // switch for future-proofing: in case this must be checked for other actions
                    switch (followAction) {
                        case FOLLOW_ACTION_SEND_REQUEST:
                            fc.sendFollowRequest(username, target);
                            userField.getText().clear();
                            updateUser();
                            return;
                        default:
                            // TODO handle unexpected case if we have time
                            return;
                    }

                case USERNAME_DOESNT_EXIST:
                    target = Utils.getStringFromBundle(bundle, TARGET_KEY, snackBarView,
                            "Unexpected error: target key bundle should not be null",
                            "Unexpected error: target key result should not contain a null string");
                    if (target == null) {
                        return;
                    }

                    followAction = Utils.getStringFromBundle(bundle, FOLLOW_ACTION_KEY, snackBarView,
                            "Unexpected error: follow action key bundle should not be null",
                            "Unexpected error: follow action key result should not contain a null string");
                    if (followAction == null) {
                        return;
                    }

                    switch (followAction) {
                        case FOLLOW_ACTION_SEND_REQUEST:
                            makeWarnToast(this, "User '" + target + "' does not exist");
                        default:
                            return;
                    }

                case USER_READ_DATA_FAIL:
                    return;
                case USER_TASK_NULL:
                    return;
                case USER_NONEXISTENT:
                    return;
                default:
                    return;
            }
        } else {
            Log.w(TAG, "unrecognized callback ID: " + callbackId);
        }
    }
}

