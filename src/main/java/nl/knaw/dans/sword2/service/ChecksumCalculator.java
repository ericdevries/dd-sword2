package nl.knaw.dans.sword2.service;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

public interface ChecksumCalculator {

    String calculateMD5Checksum(Path path) throws IOException, NoSuchAlgorithmException;
    String calculateSHA1Checksum(Path path) throws NoSuchAlgorithmException, IOException;

}
