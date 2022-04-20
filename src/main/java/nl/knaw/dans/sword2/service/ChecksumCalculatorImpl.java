package nl.knaw.dans.sword2.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import javax.xml.bind.DatatypeConverter;

public class ChecksumCalculatorImpl implements
    ChecksumCalculator {

    @Override
    public String calculateMD5Checksum(Path path) throws IOException, NoSuchAlgorithmException {
        return calculateChecksum(path, "MD5");
    }

    @Override
    public String calculateSHA1Checksum(Path path) throws NoSuchAlgorithmException, IOException {
        return calculateChecksum(path, "SHA-1");
    }

    String calculateChecksum(Path path, String algorithm)
        throws NoSuchAlgorithmException, IOException {
        var md = MessageDigest.getInstance(algorithm);
        var is = Files.newInputStream(path);
        var buf = new byte[1024 * 8];

        var bytesRead = 0;

        while ((bytesRead = is.read(buf)) != -1) {
            md.update(buf, 0, bytesRead);
        }

        return DatatypeConverter.printHexBinary(md.digest())
            .toLowerCase(Locale.ROOT);
    }
}

