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
package nl.knaw.dans.sword2.core.finalizer;

import java.util.concurrent.BlockingQueue;
import nl.knaw.dans.sword2.core.exceptions.CollectionNotFoundException;
import nl.knaw.dans.sword2.core.exceptions.DepositNotFoundException;
import nl.knaw.dans.sword2.core.exceptions.InvalidDepositException;
import nl.knaw.dans.sword2.core.exceptions.InvalidPartialFileException;
import nl.knaw.dans.sword2.core.exceptions.NotEnoughDiskSpaceException;
import nl.knaw.dans.sword2.core.service.DepositHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DepositFinalizer implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(DepositFinalizer.class);

    private final DepositHandler depositHandler;
    private final String depositId;
    private final BlockingQueue<DepositFinalizerEvent> taskQueue;

    public DepositFinalizer(String depositId,
        DepositHandler depositHandler,
        BlockingQueue<DepositFinalizerEvent> taskQueue
    ) {
        this.depositId = depositId;
        this.depositHandler = depositHandler;
        this.taskQueue = taskQueue;
    }

    @Override
    public void run() {
        try {
            var deposit = depositHandler.finalizeDeposit(depositId);
            log.info("Finalized deposit {}", deposit);
        }
        catch (DepositNotFoundException e) {
            log.error("Unable to finalize deposit with id {} because it could not be found", depositId, e);
        }
        catch (InvalidDepositException e) {
            log.error("Unable to finalize deposit with id {} because it is invalid", depositId, e);
        }
        catch (InvalidPartialFileException e) {
            log.error("Unable to finalize deposit with id {} because some files are incorrectly named", depositId, e);
        }
        catch (CollectionNotFoundException e) {
            log.error("Unable to finalize deposit with id {} because the collection could not be found", depositId, e);
        }
        catch (NotEnoughDiskSpaceException e) {
            try {
                log.warn("Rescheduling deposit with ID {}", depositId, e);
                taskQueue.put(new DepositFinalizerRescheduleEvent(depositId));
            }
            catch (InterruptedException ex) {
                log.error("Unable to add deposit with ID {} to reschedule queue", depositId, ex);
            }
        }
        // in all other cases, we should try again
        catch (Exception e) {
            log.error("Unknown error while finalizing deposit", e);
        }
    }

}
