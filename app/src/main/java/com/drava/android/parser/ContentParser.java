package com.drava.android.parser;

import java.io.Serializable;

/**
 * Created by admin on 12/30/2016.
 */

public class ContentParser implements Serializable {
    public Meta meta;
    public Settings Settings;

    public class Settings{
        public String PrivacyPolicy,TermsCondition,ContactUs,About;
    }
}
