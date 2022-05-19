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

import nl.knaw.dans.sword2.api.entry.Link;
import nl.knaw.dans.sword2.api.statement.Feed;
import nl.knaw.dans.sword2.api.statement.FeedAuthor;
import nl.knaw.dans.sword2.api.statement.FeedCategory;
import nl.knaw.dans.sword2.api.statement.TextElement;
import nl.knaw.dans.sword2.auth.Depositor;
import nl.knaw.dans.sword2.core.exceptions.DepositNotFoundException;
import nl.knaw.dans.sword2.core.exceptions.InvalidDepositException;
import nl.knaw.dans.sword2.core.service.DepositHandler;
import nl.knaw.dans.sword2.core.service.ErrorResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.net.URI;

public class StatementResourceImpl extends BaseHandler implements StatementResource {
    private static final Logger log = LoggerFactory.getLogger(StatementResourceImpl.class);

    private final URI baseUrl;
    private final DepositHandler depositHandler;

    public StatementResourceImpl(URI baseUrl, DepositHandler depositHandler, ErrorResponseFactory errorResponseFactory) {
        super(errorResponseFactory);
        this.baseUrl = baseUrl;
        this.depositHandler = depositHandler;
    }

    @Override
    public Response getStatement(String depositId, HttpHeaders headers, Depositor depositor) {
        log.info("Received getStatement request for deposit with ID {} and user {}", depositId, depositor.getName());

        try {
            var deposit = depositHandler.getDeposit(depositId, depositor);
            var url = baseUrl.resolve("/statement/" + depositId).toString();
            var feed = new Feed();

            feed.setId(url);
            feed.setTitle(new TextElement(String.format("Deposit %s", depositId), "text"));
            feed.setUpdated(deposit.getCreated().toString());
            feed.addLink(new Link(URI.create(url), "self", null));
            feed.setCategory(new FeedCategory("State", "http://purl.org/net/sword/terms/state",
                deposit.getState().toString(), deposit.getStateDescription()));
            feed.getAuthors().add(new FeedAuthor("DANS-EASY"));

            return Response.status(Response.Status.OK)
                .entity(feed)
                .build();
        }
        catch (DepositNotFoundException e) {
            log.error("Deposit with id {} could not be found", depositId, e);
            throw new WebApplicationException(404);
        }
        catch (InvalidDepositException e) {
            log.error("Deposit with id {} is invalid", depositId, e);
            throw new WebApplicationException(400);
        }
    }
}
