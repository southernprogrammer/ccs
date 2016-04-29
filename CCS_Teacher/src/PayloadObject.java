

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author HP_Owner
 */

import java.io.*;
import java.util.*;

public class PayloadObject implements Serializable{
    private Vector<String> messages;
    private byte[] encrypted;
    public PayloadObject(String message, byte[] encrypted)
    {
        this.messages = new Vector<String>();
        this.messages.add(message);
        this.encrypted = encrypted;
    }
    public PayloadObject(String message)
    {
        this.messages = new Vector<String>();
        this.messages.add(message);
    }
    public PayloadObject(Vector<String> messages)
    {
        this.messages = messages;
    }
    public PayloadObject(Vector<String> messages, byte[] encrypted)
    {
        this.messages = messages;
        this.encrypted = encrypted;
    }
    public String getMessage()
    {
        return this.messages.firstElement();
    }

    public String getMessage(int index)
    {
        return this.messages.get(index);
    }
    public byte[] getEncrypted()
    {
        return encrypted;
    }
}
