package com.blue.bluetoothcomm;

public class devices {
public String names,address;

    public devices(String names, String address) {
        this.names = names;
        this.address = address;
    }

    public String getNames() {
        return names;
    }

    public void setNames(String names) {
        this.names = names;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
