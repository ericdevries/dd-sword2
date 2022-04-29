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
package nl.knaw.dans.sword2.service;

import nl.knaw.dans.sword2.config.Sword2Config;
import nl.knaw.dans.sword2.exceptions.CollectionNotFoundException;
import nl.knaw.dans.sword2.exceptions.DepositNotFoundException;
import nl.knaw.dans.sword2.exceptions.InvalidDepositException;
import nl.knaw.dans.sword2.exceptions.InvalidPartialFileException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class DepositHandlerImplTest {
    final FileService fileService = Mockito.mock(FileService.class);
    final BagExtractor bagExtractor = Mockito.mock(BagExtractor.class);
    final DepositPropertiesManager depositPropertiesManager = Mockito.mock(DepositPropertiesManager.class);
    final CollectionManager collectionManager = Mockito.mock(CollectionManager.class);
    final UserManager userManager = Mockito.mock(UserManager.class);
    final BlockingQueue queue = Mockito.mock(BlockingQueue.class);
    final BagItManager bagItManager = Mockito.mock(BagItManager.class);
//    final
    @Test
    void finalizeDeposit() throws InvalidDepositException, InvalidPartialFileException, DepositNotFoundException, Exception, CollectionNotFoundException {
        var config = new Sword2Config();
        var depositHandler = new DepositHandlerImpl(config,
            bagExtractor,
            fileService,
            depositPropertiesManager, collectionManager, userManager, queue, bagItManager);

        depositHandler.finalizeDeposit("testid");

        assertEquals(1, 2);
    }
}
