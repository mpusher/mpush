package com.shinemo.mpush.tools.crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

/**
 * Created by ohun on 2015/12/24.
 */
public class CryptoUtils {
    public static final int[] DES_KEY_OFFSET_ARRAY = new int[]{12, 8, 4, 52, 33, 24, 12, 43, 5, 86, 79, 44, 21, 1, 66, 88};
    public static final int DES_KEY_SIZE = 8;

    public byte[] decode(byte[] data, PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher c2 = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        c2.init(Cipher.DECRYPT_MODE, privateKey);
        return c2.doFinal(data);
    }

    public byte[] encode(byte[] data, PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher c2 = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        c2.init(Cipher.DECRYPT_MODE, privateKey);
        return c2.doFinal(data);
    }



    public static String fill2Length(String str, int length) {
        if (str.length() == length) {
            return str;
        } else if (str.length() < length) {
            char[] cs = str.toCharArray();
            char[] fcs = new char[length];
            int less = length - cs.length;
            System.arraycopy(cs, 0, fcs, less, length);
            for (int i = less; i >= 0; i--) {
                fcs[i] = '*';
            }
            return new String(fcs);
        } else {
            char[] cs = str.toCharArray();
            char[] fcs = new char[length];
            System.arraycopy(cs, 0, fcs, 0, length);
            return new String(fcs);
        }
    }


    public static String mixString(String a, String b) {
        char[] charsA = fill2Length(a, DES_KEY_SIZE).toCharArray();
        char[] charsB = fill2Length(b, DES_KEY_SIZE).toCharArray();
        char[] charsC = new char[DES_KEY_SIZE];
        for (int i = 0; i < DES_KEY_SIZE; i++) {
            char charA = charsA[i];
            char charB = charsB[i];
            int sum = charA + charB;
            int offset = DES_KEY_OFFSET_ARRAY[sum % DES_KEY_OFFSET_ARRAY.length];
            boolean chooseA = (sum % 2 == 0);
            boolean upOrDown = (charA % 2 == 0);
            char charC;
            if (chooseA) {
                charC = charA;
            } else {
                charC = charB;
            }
            if (upOrDown) {
                offset = -offset;
            }
            int result = charC + offset;
            if (result > 126 || result < 33) {
                result = 33 + Math.abs(result % (126 - 33));
            }
            charC = (char) result;
            charsC[i] = charC;
        }
        return new String(charsC);
    }
}
