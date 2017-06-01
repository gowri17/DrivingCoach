package com.drava.android.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Meta {

    @SerializedName("code")
    @Expose
    private Integer code;
    @SerializedName("dataPropertyName")
    @Expose
    private String dataPropertyName;

    /**
     * @return The code
     */
    public Integer getCode() {
        return code;
    }

    /**
     * @param code The code
     */
    public void setCode(Integer code) {
        this.code = code;
    }

    /**
     * @return The dataPropertyName
     */
    public String getDataPropertyName() {
        return dataPropertyName;
    }

    /**
     * @param dataPropertyName The dataPropertyName
     */
    public void setDataPropertyName(String dataPropertyName) {
        this.dataPropertyName = dataPropertyName;
    }

}