package com.example.moodspace;

import android.content.Context;

import java.util.List;

/**
 * Followers can be thought of as a directed graph between users.
 * Both requests and following are represented in firestore as adjacency lists under:
 *   users -> (username) -> Following
 *   users -> (username) -> FollowRequestsFrom
 *
 * For notation,let x and y be users.
 *   x => y means the following:
 *    - x is following y
 *    - x is a follower of y
 *    - y is a followee of x
 *
 *   x -> y means the following:
 *    - x has sent a follow request to y
 */
public class FollowController {
    private Context context;

    public FollowController(Context context) {
        this.context = context;
    }

    /**
     * user <= follower
     */
    public void addFollower(String user, String follower) {
    }

    /**
     * user <=/= follower
     * (unfollow)
     */
    public void removeFollower(String user, String follower) {
    }

    /**
     * user -?-> potentialFollowee
     */
    public void sendFollowRequest(String user, String potentialFollowee) {
    }

    /**
     * user -/-> potentialFollowee
     * (also used for declining follow requests)
     */
    public void removeFollowRequest(String user, String potentialFollowee) {
    }

    /**
     * user -/-> potentialFollowee
     * user => potentialFollowee
     */
    public void acceptFollowRequest(String user, String potentialFollowee) {
        this.removeFollower(user, potentialFollowee);
        this.addFollower(potentialFollowee, user);
    }

    public List<MoodOther> getFollowingMoods(String user) {
        return null;
    }


}
