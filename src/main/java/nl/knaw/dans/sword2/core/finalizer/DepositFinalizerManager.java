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

import io.dropwizard.lifecycle.Managed;
import nl.knaw.dans.sword2.core.service.DepositHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

public class DepositFinalizerManager implements Managed {
    private static final Logger log = LoggerFactory.getLogger(DepositFinalizerManager.class);

    private final DepositHandler depositHandler;
    private final Thread depositFinalizerListenerThread;
    private final BlockingQueue<DepositFinalizerEvent> taskQueue;
    private final ExecutorService finalizerQueue;
    private final ExecutorService rescheduleQueue;

    public DepositFinalizerManager(ExecutorService finalizerQueue, DepositHandler depositHandler,
        BlockingQueue<DepositFinalizerEvent> taskQueue, ExecutorService rescheduleQueue,
        Duration rescheduleDelay) {
        this.depositHandler = depositHandler;
        this.depositFinalizerListenerThread = new Thread(new DepositFinalizerListener(taskQueue, finalizerQueue, depositHandler, rescheduleQueue, rescheduleDelay));
        this.taskQueue = taskQueue;
        this.finalizerQueue = finalizerQueue;
        this.rescheduleQueue = rescheduleQueue;
    }

    @Override
    public void start() throws Exception {
        this.depositFinalizerListenerThread.start();

        // scan all items in the uploads folder and add them to the queue
        var deposits = this.depositHandler.getOpenDeposits();
        log.info("Found {} deposits that need to be checked", deposits.size());

        for (var deposit: deposits) {
            log.info("Adding finalizing event for deposit {} to the queue", deposit.getId());
            this.taskQueue.put(new DepositFinalizerEvent(deposit.getId()));
        }
    }

    @Override
    public void stop() throws Exception {
        this.taskQueue.put(new DepositFinalizerStopEvent());
        this.finalizerQueue.shutdown();
        this.rescheduleQueue.shutdownNow();
    }
}
