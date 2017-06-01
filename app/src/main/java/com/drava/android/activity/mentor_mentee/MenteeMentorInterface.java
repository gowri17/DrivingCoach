package com.drava.android.activity.mentor_mentee;

/**
 * Created by admin on 11/3/2016.
 */

public interface MenteeMentorInterface {
    public void onRemoveClick(MentorListParser.MentorList mentorList);
    public void checkUserTokenWithGooglePlayToViewMentee(int position);
}
