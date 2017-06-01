package com.drava.android.parser;

import java.util.List;

public class InviteParser {
    public Meta meta;
    public InviteDetails inviteDetails;
    public List<String> notifications;

    public InviteDetails getInviteDetails() {
        return inviteDetails;
    }

    public class Meta
    {
        public int code;
        public String dataPropertyName,errorMessage;

        public int getCode() {
            return code;
        }
    }

    public class InviteDetails
    {
        public List<ContactNumber> ContactNumber;
        public List<ContactEmail> ContactEmail;

        public List<InviteDetails.ContactNumber> getContactNumber() {
            return ContactNumber;
        }

        public List<InviteDetails.ContactEmail> getContactEmail() {
            return ContactEmail;
        }

        public class ContactEmail
        {
            public String id;
            public int InviteStatus;
            public int UserStatus;

            public String getId() {
                return id;
            }

            public int getInviteStatus() {
                return InviteStatus;
            }

            public int getUserStatus() {
                return UserStatus;
            }
        }

        public class ContactNumber
        {
            public String id;
            public int InviteStatus;
            public int UserStatus;

            public String getId() {
                return id;
            }

            public int getInviteStatus() {
                return InviteStatus;
            }

            public int getUserStatus() {
                return UserStatus;
            }
        }
    }


}






