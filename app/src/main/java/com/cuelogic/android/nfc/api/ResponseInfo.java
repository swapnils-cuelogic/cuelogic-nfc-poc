package com.cuelogic.android.nfc.api;

public class ResponseInfo {
    public boolean device_duplicate;
    public boolean device_exists;
    public boolean device_type_cannot_be_assigned;
    public boolean device_under_repair;
    public boolean employee_exists;
    public boolean employee_duplicate;
    public boolean assigned;
    public boolean inactive_user;


    @Override
    public String toString() {
        return "ResponseInfo{" +
                "device_duplicate=" + device_duplicate +
                ", device_exists=" + device_exists +
                ", device_type_cannot_be_assigned=" + device_type_cannot_be_assigned +
                ", device_under_repair=" + device_under_repair +
                ", employee_exists=" + employee_exists +
                ", employee_duplicate=" + employee_duplicate +
                ", assigned=" + assigned +
                ", inactive_user=" + inactive_user +
                '}';
    }
}
