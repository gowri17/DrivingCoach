package com.drava.android.activity;

import com.drava.android.activity.mentor_mentee.MentorListParser;

import java.util.List;

/**
 * Created by admin on 11/4/2016.
 */

public class InviteListParser {
    public Meta meta;
    public List<InviteList> InviteList;
    public String[] notifications = new String[]{};

    public class Meta{
        String code,dataPropertyName,TotalCount,ListedCount,errorMessage;
    }

    public class InviteList{
        String InviteId,UserId,FirstName,LastName,Email,UserType,PhoneNumber;
    }
}
