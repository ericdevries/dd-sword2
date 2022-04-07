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
package nl.knaw.dans.sword2;

import com.sun.xml.bind.v2.runtime.IllegalAnnotationException;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import nl.knaw.dans.sword2.models.Entry;
import nl.knaw.dans.sword2.openapi.api.CollectionDto;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import java.io.IOException;
import java.io.InputStream;

public class CollectionApiImpl implements CollectionHandler {

    @Override
    public CollectionDto deposit(CollectionDto input) {
        System.out.println("DEPOSIT1");
        return new CollectionDto();
    }

    @Override
    public Entry getDeposit(HttpHeaders headers) {
        var element = new Entry();
        element.setId("TESTID");
        element.setTitle("TITLE");
        return element;
    }

    @Override
    public Response depositAnything(InputStream inputStream, HttpHeaders headers) {
        System.out.println("DEPOSIT2");
        for (var entry: headers.getRequestHeaders().entrySet()) {
            System.out.println(entry.getKey());
            for (var value: entry.getValue()) {
                System.out.println(" - " + value);
            }
        }

        try {
            var content = new String(inputStream.readAllBytes());
            System.out.println("CONTENT: " + content.length());
        } catch (IOException e) {
            e.printStackTrace();
        }
        var str = "<entry xmlns=\"http://www.w3.org/2005/Atom\"\n" + "        xmlns:sword=\"http://purl.org/net/sword/\"\n"
                + "        xmlns:dcterms=\"http://purl.org/dc/terms/\">\n" + "\n" + "    <title>My Deposit</title>\n"
                + "    <id>info:something:1</id>\n" + "    <updated>2008-08-18T14:27:08Z</updated>\n"
                + "    <summary type=\"text\">A summary</summary>\n"
                + "    <generator uri=\"http://www.myrepository.ac.uk/sword-plugin\" version=\"1.0\"/>\n" + "\n"
                + "    <!-- the item's metadata -->\n" + "    <dcterms:abstract>The abstract</dcterms:abstract>\n"
                + "    <dcterms:accessRights>Access Rights</dcterms:accessRights>\n"
                + "    <dcterms:alternative>Alternative Title</dcterms:alternative>\n"
                + "    <dcterms:available>Date Available</dcterms:available>\n"
                + "    <dcterms:bibliographicCitation>Bibliographic Citation</dcterms:bibliographicCitation>\n"
                + "    <dcterms:contributor>Contributor</dcterms:contributor>\n"
                + "    <dcterms:description>Description</dcterms:description>\n" + "    <dcterms:hasPart>Has Part</dcterms:hasPart>\n"
                + "    <dcterms:hasVersion>Has Version</dcterms:hasVersion>\n" + "    <dcterms:identifier>Identifier</dcterms:identifier>\n"
                + "    <dcterms:isPartOf>Is Part Of</dcterms:isPartOf>\n" + "    <dcterms:publisher>Publisher</dcterms:publisher>\n"
                + "    <dcterms:references>References</dcterms:references>\n"
                + "    <dcterms:rightsHolder>Rights Holder</dcterms:rightsHolder>\n" + "    <dcterms:source>Source</dcterms:source>\n"
                + "    <dcterms:title>Title</dcterms:title>\n" + "    <dcterms:type>Type</dcterms:type>\n" + "\n"
                + "    <sword:verboseDescription>Verbose description</sword:verboseDescription>\n"
                + "    <sword:treatment>Unpacked. JPEG contents converted to JPEG2000.</sword:treatment>\n" + "\n"
                + "    <link rel=\"alternate\" href=\"http://www.swordserver.ac.uk/col1/mydeposit.html\"/>\n"
                + "    <content type=\"application/zip\" src=\"http://www.swordserver.ac.uk/col1/mydeposit\"/>\n"
                + "    <link rel=\"edit-media\" href=\"http://www.swordserver.ac.uk/col1/mydeposit\"/>\n"
                + "    <link rel=\"edit\" href=\"http://www.swordserver.ac.uk/col1/mydeposit.atom\" />\n"
                + "    <link rel=\"http://purl.org/net/sword/terms/add\" href=\"http://www.swordserver.ac.uk/col1/mydeposit.atom\" />\n"
                + "    <sword:packaging>http://purl.org/net/sword/package/BagIt</sword:packaging>\n" + "\n"
                + "    <link rel=\"http://purl.org/net/sword/terms/originalDeposit\" \n" + "            type=\"application/zip\" \n"
                + "            href=\"http://www.swordserver.ac.uk/col1/mydeposit/package.zip\"/>\n"
                + "    <link rel=\"http://purl.org/net/sword/terms/derivedResource\" \n" + "            type=\"application/pdf\" \n"
                + "            href=\"http://www.swordserver.ac.uk/col1/mydeposit/file1.pdf\"/>\n"
                + "    <link rel=\"http://purl.org/net/sword/terms/derivedResource\" \n" + "            type=\"application/pdf\" \n"
                + "            href=\"http://www.swordserver.ac.uk/col1/mydeposit/file2.pdf\"/>\n" + "\n"
                + "    <link rel=\"http://purl.org/net/sword/terms/statement\" \n"
                + "            type=\"application/atom+xml;type=feed\" \n"
                + "            href=\"http://www.swordserver.ac.uk/col1/mydeposit.feed\"/>\n"
                + "    <link rel=\"http://purl.org/net/sword/terms/statement\" \n" + "            type=\"application/rdf+xml\" \n"
                + "            href=\"http://www.swordserver.ac.uk/col1/mydeposit.rdf\"/>\n" + "\n" + "\n" + "</entry>";

        var element = new Entry();
        element.setId("TESTID");
        element.setTitle("TITLE");

        return Response.status(Response.Status.CREATED).entity(element).build();
    }

    @Override
    public CollectionDto depositAtom(InputStream inputStream) {
        try {
            var s = new String(inputStream.readAllBytes());
            System.out.println("STRING:  " + s);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new CollectionDto();
    }
}
