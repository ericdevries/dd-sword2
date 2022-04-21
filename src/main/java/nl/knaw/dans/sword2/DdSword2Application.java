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

import io.dropwizard.Application;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.knaw.dans.sword2.resource.CollectionHandlerImpl;
import nl.knaw.dans.sword2.resource.ServiceDocumentHandlerImpl;
import nl.knaw.dans.sword2.service.BagExtractorImpl;
import nl.knaw.dans.sword2.service.ChecksumCalculatorImpl;
import nl.knaw.dans.sword2.service.DepositHandlerImpl;
import nl.knaw.dans.sword2.service.DepositPropertiesManagerImpl;
import nl.knaw.dans.sword2.service.DepositReceiptFactoryImpl;
import nl.knaw.dans.sword2.service.FileServiceImpl;
import nl.knaw.dans.sword2.service.ZipServiceImpl;

public class DdSword2Application extends Application<DdSword2Configuration> {

    public static void main(final String[] args) throws Exception {
        new DdSword2Application().run(args);
    }

    @Override
    public String getName() {
        return "Dd Sword2";
    }

    @Override
    public void initialize(final Bootstrap<DdSword2Configuration> bootstrap) {
        // TODO: application initialization
        bootstrap.addBundle(new MultiPartBundle());
    }

    @Override
    public void run(final DdSword2Configuration configuration, final Environment environment)
        throws Exception {
        var fileService = new FileServiceImpl();
        var depositPropertiesManager = new DepositPropertiesManagerImpl(configuration.getSword2());
        var checksumCalculator = new ChecksumCalculatorImpl();

        var zipService = new ZipServiceImpl(fileService);

        var bagExtractor = new BagExtractorImpl(zipService);
        var depositManager = new DepositHandlerImpl(configuration.getSword2(),
            bagExtractor,
            fileService,
            depositPropertiesManager, checksumCalculator);

        var depositReceiptFactory = new DepositReceiptFactoryImpl(configuration.getSword2()
            .getBaseUrl());

        environment.jersey()
            .register(new CollectionHandlerImpl(depositManager, depositReceiptFactory,
                checksumCalculator));

        environment.jersey()
            .register(new ServiceDocumentHandlerImpl(configuration.getUsers(),
                configuration.getSword2()
                    .getCollections(),
                configuration.getSword2()
                    .getBaseUrl()));
    }
}
