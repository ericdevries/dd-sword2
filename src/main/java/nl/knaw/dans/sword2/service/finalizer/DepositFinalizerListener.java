
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
package nl.knaw.dans.sword2.service.finalizer;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

import nl.knaw.dans.sword2.service.DepositHandler;

public class DepositFinalizerListener implements Runnable {

    private final BlockingQueue<DepositFinalizerEvent> taskQueue;
    private final ExecutorService finalizerQueue;
    private final DepositHandler depositHandler;

    public DepositFinalizerListener(BlockingQueue<DepositFinalizerEvent> taskQueue,
        ExecutorService finalizerQueue,
        DepositHandler depositHandler
    ) {
        this.taskQueue = taskQueue;
        this.finalizerQueue = finalizerQueue;
        this.depositHandler = depositHandler;
    }

    @Override
    public void run() {

        while (true) {
            try {
                var depositTask = taskQueue.take();

                Runnable task = null;

                switch (depositTask.getEventType()) {
                    case STOP:
                        return;

                    case FINALIZE:
                        task = new DepositFinalizer(depositTask.getDepositId(),
                            depositHandler,
                            taskQueue);
                        break;

                    case RETRY:
                        task = new DepositFinalizerDelayedTask(depositTask.getDepositId(),
                            30,
                            taskQueue);
                        break;

                }

                if (task != null) {
                    finalizerQueue.submit(task);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }

    }
}
