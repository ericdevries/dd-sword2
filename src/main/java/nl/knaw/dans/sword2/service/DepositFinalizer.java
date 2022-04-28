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

import java.util.concurrent.BlockingQueue;
import nl.knaw.dans.sword2.exceptions.CollectionNotFoundException;
import nl.knaw.dans.sword2.exceptions.DepositNotFoundException;
import nl.knaw.dans.sword2.exceptions.InvalidDepositException;
import nl.knaw.dans.sword2.exceptions.InvalidPartialFileException;
import nl.knaw.dans.sword2.service.finalizer.DepositFinalizerEvent;
import nl.knaw.dans.sword2.service.finalizer.DepositFinalizerRetryEvent;

public class DepositFinalizer implements Runnable {

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
        } catch (DepositNotFoundException e) {
            e.printStackTrace();
        } catch (InvalidDepositException e) {
            e.printStackTrace();
        } catch (InvalidPartialFileException e) {
            e.printStackTrace();
        } catch (CollectionNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            try {
                taskQueue.put(new DepositFinalizerRetryEvent(depositId));
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }

}
