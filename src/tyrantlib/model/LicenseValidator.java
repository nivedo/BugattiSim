package tyrantlib.model;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.RSAPrivateKeySpec;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by Jay on 7/21/2014.
 */

import java.util.List;

public class LicenseValidator {

    private static String LICENSE_FILE = "license.txt";

    public LicenseValidator() {}

    // Returns seconds until expiry
    public int validate() {
        String licenseStr = "";

        // Try to find local license
        try {
            BufferedReader br = new BufferedReader(new FileReader(LICENSE_FILE));
            licenseStr = br.readLine();
            br.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        // Testing RSA Encryption
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            String modString = "2C8Q09vAQ8dWR9syQJryc0OFEFfmNKfI82JaLXP9asPp2J+qCR6FNTUuZwC6ocapf/pqnknoLw9m/OO2Or3w1s4rDfOskrm6T88jCUGeTHYP6BFewaj/wmoCedx+PStUX/caTjDkrLbse5oHsaUe7vRaClN2UtS9tbX/O8EAZzc=";
            String expString = "XIzhvmFw0VOQi5i6zc/IBjKcz99hrZ87N38eriDfGAshnNzV9at8Scgnwm8cd0/OlvyFEpj/bs5AP/nYtRNF/FZV2zpV6fZ6eNbZkO10WktlaN55BGD6w38WjdsfGkK8CcG6ITMzBqXqjax45NPZfql5ryrpVhj5WlNuVQbAXxE=";
            BigInteger modulus = new BigInteger(1, DatatypeConverter.parseBase64Binary(modString));
            BigInteger pExp = new BigInteger(1, DatatypeConverter.parseBase64Binary(expString));
            RSAPrivateKeySpec privKeySpec = new RSAPrivateKeySpec(modulus, pExp);
            RSAPrivateKey key = (RSAPrivateKey) keyFactory.generatePrivate(privKeySpec);

            Cipher rsa;
            rsa = Cipher.getInstance("RSA");
            rsa.init(Cipher.DECRYPT_MODE, key);

            byte[] encoded = DatatypeConverter.parseBase64Binary(licenseStr);
            byte[] utf8 = rsa.doFinal(encoded);

            String decrypted = new String(utf8, "utf8");
            String[] split = decrypted.split("_");
            int expireTime = Integer.parseInt(split[1].trim());
            int secondsToExpiry = expireTime - (int) (System.currentTimeMillis() / 1e3);

            return secondsToExpiry;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}
