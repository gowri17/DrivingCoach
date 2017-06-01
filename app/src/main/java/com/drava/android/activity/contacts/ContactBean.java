package com.drava.android.activity.contacts;

public class ContactBean {
    String nameEmail, email;
    int status;
    int userStatus;
    boolean isSelected;
    String profileImage;

    public ContactBean(String nameEmail, String email, String image, int status, int userStatus, boolean isSelected) {
        this.nameEmail = nameEmail;
        this.email = email;
        this.status = status;
        this.userStatus = userStatus;
        this.profileImage = image;
        this.isSelected = isSelected;
    }

    public void setUserStatus(int userStatus) {
        this.userStatus = userStatus;
    }

    public String getEmail(){
        return email;
    }

    public void setStatus(int status){
        this.status = status;
    }

    public int getInviteStatus() {
        return status;
    }

    public int getUserStatus() {
        return userStatus;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public String toString() {
        return "ContactBean{" +
                "nameEmail='" + nameEmail + '\'' +
                ", email='" + email + '\'' +
                ", status=" + status +
                ", userStatus=" + userStatus +
                ", profileImage='" + profileImage + '\'' +
                '}';
    }
}