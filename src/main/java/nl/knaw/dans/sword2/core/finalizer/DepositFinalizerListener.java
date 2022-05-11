
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

import nl.knaw.dans.sword2.core.service.DepositHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

public class DepositFinalizerListener implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(DepositFinalizerListener.class);

    private final BlockingQueue<DepositFinalizerEvent> taskQueue;
    private final ExecutorService finalizerQueue;
    private final ExecutorService rescheduleQueue;
    private final DepositHandler depositHandler;
    private final Duration rescheduleDelay;

    public DepositFinalizerListener(BlockingQueue<DepositFinalizerEvent> taskQueue, ExecutorService finalizerQueue, DepositHandler depositHandler, ExecutorService rescheduleQueue,
        Duration rescheduleDelay) {
        this.taskQueue = taskQueue;
        this.finalizerQueue = finalizerQueue;
        this.depositHandler = depositHandler;
        this.rescheduleQueue = rescheduleQueue;
        this.rescheduleDelay = rescheduleDelay;
    }

    @Override
    public void run() {

        while (true) {
            try {
                var depositTask = taskQueue.take();

                log.info("Received task from queue: {}", depositTask);

                switch (depositTask.getEventType()) {
                    case STOP:
                        return;

                    case FINALIZE:
                        finalizerQueue.submit(new DepositFinalizer(depositTask.getDepositId(), depositHandler, taskQueue));
                        break;

                    case RESCHEDULE:
                        rescheduleQueue.submit(new DepositFinalizerDelayedTask(depositTask.getDepositId(), rescheduleDelay, taskQueue));
                        break;

                }

            }
            catch (InterruptedException e) {
                log.error("Unable to run task because the thread was interrupted", e);
                break;
            }
        }
    }
}
