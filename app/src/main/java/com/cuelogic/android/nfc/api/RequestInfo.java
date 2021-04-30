package com.cuelogic.android.nfc.api;

public class RequestInfo {
    public Device device;
    public Employee employee;

    public static class Spec {
        public String desc;
    }

    public static class Device {
        public String type;
        public Spec spec;
    }

    public static class Employee {
        public String type;
        public Spec spec;
    }

}

