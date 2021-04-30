package com.cuelogic.android.nfc.api;

public class ConsoleResponse {
    public int status;
    public ResponseInfo data;

    @Override
    public String toString() {
        return "ConsoleResponse{" +
                "status=" + status +
                ", info=" + data +
                '}';
    }
}
