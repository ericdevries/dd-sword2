/*
 * Copyright (C) 2022 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.sword2.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class HashHeaderInterceptor implements WriterInterceptor {
    private static final Logger log = LoggerFactory.getLogger(HashHeaderInterceptor.class);

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        try {
            var digest = MessageDigest.getInstance("MD5");
            context.setOutputStream(new DigestOutputStream(context.getOutputStream(), digest));
            context.proceed();

            var checksum = DatatypeConverter.printHexBinary(digest.digest()).toLowerCase(Locale.ROOT);
            context.getHeaders().add("Content-MD5", checksum);

            log.trace("Set Content-MD5 checksum for response payload to {}", checksum);
        }
        catch (NoSuchAlgorithmException e) {
            log.error("No such algorithm", e);
            context.proceed();
        }
    }
}
