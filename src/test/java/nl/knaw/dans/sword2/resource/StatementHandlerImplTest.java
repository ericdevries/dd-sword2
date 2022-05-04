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

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import nl.knaw.dans.sword2.Deposit;
import nl.knaw.dans.sword2.DepositState;
import nl.knaw.dans.sword2.exceptions.DepositNotFoundException;
import nl.knaw.dans.sword2.models.statement.Feed;
import nl.knaw.dans.sword2.service.DepositHandler;
import nl.knaw.dans.sword2.service.DepositHandlerImpl;
import nl.knaw.dans.sword2.service.ErrorResponseFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.net.URI;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(DropwizardExtensionsSupport.class)
class StatementHandlerImplTest {
    private static final DepositHandler depositHandler = Mockito.mock(DepositHandler.class);
    private static final ErrorResponseFactory errorResponseFactory = Mockito.mock(ErrorResponseFactory.class);
    private static final ResourceExtension EXT = ResourceExtension.builder()
        .bootstrapLogging(true)
        .addResource(new StatementHandlerImpl(URI.create("http://localhost:8080"), depositHandler, errorResponseFactory))
        .build();

    @Test
    void testStatement() throws JAXBException, DepositNotFoundException {
        var deposit = new Deposit();
        deposit.setId("a03ca6f1-608b-4247-8c22-99681b8494a0");
        deposit.setCreated(OffsetDateTime.now());
        deposit.setState(DepositState.SUBMITTED);
        deposit.setStateDescription("Submitted");

        Mockito.when(depositHandler.getDeposit(Mockito.anyString(), Mockito.any()))
            .thenReturn(deposit);

        var response = EXT.client().target("/statement/a03ca6f1-608b-4247-8c22-99681b8494a0")
            .request().get();

        assertEquals(200, response.getStatus());

        var feed = response.readEntity(Feed.class);
        printStatement(feed);
    }

    void printStatement(Feed feed) throws JAXBException {
        var ctx = JAXBContext.newInstance(Feed.class);
        var marshaller = ctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        var writer = new StringWriter();
        marshaller.marshal(feed, writer);
        var str = writer.toString();
        System.out.println(str);
    }
}
