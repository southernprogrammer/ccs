

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Bryan
 */

import java.security.*;
import java.security.spec.*;
import javax.crypto.Cipher;
import java.io.*;

public class Encryption {
    private PrivateKey privkey;
    private String xform = "RSA";

    public Encryption()
    {
        try{
            FileInputStream keyfis = new FileInputStream("c:\\program files\\ccs\\CCS_Teacher\\Private.dat");
            byte[] encKey = new byte[keyfis.available()];
            keyfis.read(encKey);
            keyfis.close();

            PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(encKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "SunRsaSign");
            privkey = keyFactory.generatePrivate(privKeySpec);
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }

  public byte[] encryptSmall(byte[] inpBytes) throws Exception {
    Cipher cipher = Cipher.getInstance(xform);
    cipher.init(Cipher.ENCRYPT_MODE, privkey);
    return cipher.doFinal(inpBytes);
  }
  public byte[] decryptSmall(byte[] inpBytes) throws Exception{
    Cipher cipher = Cipher.getInstance(xform);
    cipher.init(Cipher.DECRYPT_MODE, privkey);
    return cipher.doFinal(inpBytes);
  }
  public byte[] encrypt(byte[] inpBytes)
  {
      try{
        return encryptDecryptBig(Cipher.ENCRYPT_MODE, inpBytes);
      }
      catch(Exception e)
      {
          System.out.println(e);
      }
      return null;
  }
  public byte[] decrypt(byte[] inpBytes)
  {
      try{
        return encryptDecryptBig(Cipher.DECRYPT_MODE, inpBytes);
      }
      catch(Exception e)
      {
          System.out.println(e);
      }
      return null;
  }
  public byte[] encryptDecryptBig(int cipherMode, byte[] inpBytes) throws Exception
  {

      ByteArrayOutputStream outputWriter = null;
      ByteArrayInputStream inputReader = null;
      try
      {

          Cipher cipher = Cipher.getInstance("RSA");
        //RSA encryption data size limitations are slightly less than the key modulus size,
        //depending on the actual padding scheme used (e.g. with 512 bit (64 byte) RSA key,
        //the size limit is 53 bytes for PKCS#1 v 1.5 padding. (http://www.jensign.com/JavaScience/dotnet/RSAEncrypt/)
          byte[] buf = cipherMode == Cipher.ENCRYPT_MODE? new byte[53] : new byte[64];
          int bufl;
          // init the Cipher object for Encryption…
          cipher.init(cipherMode, privkey);

          // start BufferIO
          outputWriter = new ByteArrayOutputStream();
          inputReader = new ByteArrayInputStream(inpBytes);
        while ( (bufl = inputReader.read(buf)) != -1)
        {
            byte[] encText = null;
            if (cipherMode == Cipher.ENCRYPT_MODE)
            {
                encText = encryptSmall(copyBytes(buf,bufl));
            }
            else
            {
                encText = decryptSmall(copyBytes(buf,bufl));
            }
            outputWriter.write(encText);
        }
            outputWriter.flush();
    }
    finally
    {

        try
        {
            byte[] returnMe = null;
            if (outputWriter != null)
            {
                returnMe = outputWriter.toByteArray();
                outputWriter.close();

            }
            if (inputReader != null)
            {
                inputReader.close();
            }
            return returnMe;
        }
        catch (Exception e)
        {

              // do nothing…

        }
    }
    return null;
}

    public static byte[] copyBytes(byte[] arr, int length)
    {
        byte[] newArr = null;
        if (arr.length == length)
        {
            newArr = arr;
        }
        else
        {
            newArr = new byte[length];
            for (int i = 0; i < length; i++)
            {
              newArr[i] = (byte) arr[i];
            }
        }
        return newArr;
    }
}
