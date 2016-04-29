/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Bryan
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author HP_Owner
 */

import java.security.*;
import java.io.*;

public class Generator {
    public static void main(String args[])
    {
       try{
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(512); // this is the keysize - in bits.
            KeyPair kp = kpg.generateKeyPair();
            FileOutputStream privfile = new FileOutputStream("c:\\program files\\ccs\\CCS_Teacher\\Private.dat");
            privfile.write(kp.getPrivate().getEncoded());
            privfile.close();
            FileOutputStream pubfile = new FileOutputStream("c:\\program files\\ccs\\Public.dat");
            pubfile.write(kp.getPublic().getEncoded());
            pubfile.close();
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }
}
