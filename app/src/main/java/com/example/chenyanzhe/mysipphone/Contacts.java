package com.example.chenyanzhe.mysipphone;

/**
 * Created by 树宇 on 2017/8/30.
 */

class Contacts {
    private String contacts;
    private String sipAdress;
    public Contacts(String contacts, String sipAdress){
        this.contacts=contacts;
        this.sipAdress=sipAdress;
    }
    public String getContacts(){
        return contacts;
    }

    public String getSipAdress() {
        return sipAdress;
    }

    public void setContacts(String contacts) {
        this.contacts = contacts;
    }

    public void setSipAdress(String sipAdress) {
        this.sipAdress = sipAdress;
    }
}
