package com.drava.android.parser;

/**
 * Created by evuser on 10-10-2016.
 */

public class Signup
{
    private Login Login;

    private String[] notifications;

    private Meta meta;

    public Login getLogin ()
    {
        return Login;
    }

    public void setLogin (Login Login)
    {
        this.Login = Login;
    }

    public String[] getNotifications ()
    {
        return notifications;
    }

    public void setNotifications (String[] notifications)
    {
        this.notifications = notifications;
    }

    public Meta getMeta ()
    {
        return meta;
    }

    public void setMeta (Meta meta)
    {
        this.meta = meta;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [Login = "+Login+", notifications = "+notifications+", meta = "+meta+"]";
    }

    public class Login
    {
        private String UserType;

        private String Status;

        private String ExpiresIn;

        private String AccessToken;

        private String Expires;

        private String TokenType;

        private String UserId;

        public String getUserType ()
        {
            return UserType;
        }

        public void setUserType (String UserType)
        {
            this.UserType = UserType;
        }

        public String getStatus ()
        {
            return Status;
        }

        public void setStatus (String Status)
        {
            this.Status = Status;
        }

        public String getExpiresIn ()
        {
            return ExpiresIn;
        }

        public void setExpiresIn (String ExpiresIn)
        {
            this.ExpiresIn = ExpiresIn;
        }

        public String getAccessToken ()
        {
            return AccessToken;
        }

        public void setAccessToken (String AccessToken)
        {
            this.AccessToken = AccessToken;
        }

        public String getExpires ()
        {
            return Expires;
        }

        public void setExpires (String Expires)
        {
            this.Expires = Expires;
        }

        public String getTokenType ()
        {
            return TokenType;
        }

        public void setTokenType (String TokenType)
        {
            this.TokenType = TokenType;
        }

        public String getUserId ()
        {
            return UserId;
        }

        public void setUserId (String UserId)
        {
            this.UserId = UserId;
        }

        @Override
        public String toString()
        {
            return "ClassPojo [UserType = "+UserType+", Status = "+Status+", ExpiresIn = "+ExpiresIn+", AccessToken = "+AccessToken+", Expires = "+Expires+", TokenType = "+TokenType+", UserId = "+UserId+"]";
        }
    }

    public class Meta
    {
        private String code;

        private String dataPropertyName;

        private String errorMessage;

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public String getCode ()
        {
            return code;
        }

        public void setCode (String code)
        {
            this.code = code;
        }

        public String getDataPropertyName ()
        {
            return dataPropertyName;
        }

        public void setDataPropertyName (String dataPropertyName)
        {
            this.dataPropertyName = dataPropertyName;
        }

        @Override
        public String toString()
        {
            return "ClassPojo [code = "+code+", dataPropertyName = "+dataPropertyName+"]";
        }
    }
}
