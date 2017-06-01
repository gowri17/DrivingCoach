package com.drava.android.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.drava.android.R;
import com.drava.android.activity.contacts.CustomContactActivity;


public class AlertUtils {

    public static void showAlert(Context context, String title, String message,
                                 DialogInterface.OnClickListener onClick, boolean cancelable) {
        if (!((Activity) context).isFinishing()) {
            new AlertDialog.Builder(context, R.style.Theme_CarFit_Alert)
                    .setMessage(message)
                    .setTitle((title != null && !title.equals("")) ? title : context.getString(R.string.app_name)) //TODO null check the title with TextUtils.
                    .setCancelable(cancelable)
                    .setPositiveButton(android.R.string.ok, onClick)
                    .create().show();
        }
    }

    public static void showBackAlert(final Context context, String title, String message){
        if(!((Activity)context).isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_CarFit_Alert);
            builder.setTitle(title);
            builder.setMessage(message);
            builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ((Activity)context).onBackPressed();
                }
            });
            builder.show();
        }
    }

    public static void showListAlert(Context context, String title, CharSequence[] itemList, DialogInterface.OnClickListener onClick){
        if (!((Activity) context).isFinishing()) {
            new AlertDialog.Builder(context, R.style.Theme_CarFit_Alert)
                    .setTitle((title != null && !title.equals("")) ? title : context.getString(R.string.choose_one))
                    .setItems(itemList, onClick)
                    .setCancelable(true)
                    .create().show();
        }
    }

    public static void showSingleChoiceAlert(Context context, String title, CharSequence[] itemList, int checkedItem, DialogInterface.OnClickListener onClick){
        if (!((Activity) context).isFinishing()) {
            new AlertDialog.Builder(context, R.style.Theme_CarFit_Alert)
                    .setTitle((title != null && !title.equals("")) ? title : context.getString(R.string.choose_one))
                    .setSingleChoiceItems(itemList, checkedItem, onClick)
                    .setCancelable(false)
                    .create().show();
        }
    }

    public static void showAlert(Context context, String message) {
        showAlert(context, null, message, null, false);
    }

    public static void showAlert(Context context, String title, String message) {
        showAlert(context, title, message, null, false);
    }

    public static void showAlert(Context context, String message, DialogInterface.OnClickListener onClick, boolean cancelable) {
        showAlert(context, null, message, onClick, cancelable);
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


    public static void showAlert(Context context, String title, String message,
                                 DialogInterface.OnClickListener okClick,DialogInterface.OnClickListener cancelClick, boolean cancelable) {
        if (!((Activity) context).isFinishing()) {
            new AlertDialog.Builder(context, R.style.Theme_CarFit_Alert)
                    .setMessage(message)
                    .setTitle((title != null && !title.equals("")) ? title : context.getString(R.string.app_name)) //TODO null check the title with TextUtils.
                    .setCancelable(cancelable)
                    .setPositiveButton(android.R.string.ok, okClick)
                    .setNegativeButton(android.R.string.cancel,cancelClick)
                    .create().show();
        }
    }
}
