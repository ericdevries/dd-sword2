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
import java.util.concurrent.ExecutorService;

public class DepositFinalizerManager implements Runnable {


    private final BlockingQueue<DepositFinalizerTask> taskQueue ;
    private final ExecutorService finalizerQueue;
    private final DepositHandler depositHandler;

    public DepositFinalizerManager(
        ExecutorService finalizerQueue,
        DepositHandler depositHandler,
        BlockingQueue<DepositFinalizerTask> taskQueue
    ) {
        this.taskQueue = taskQueue;
        this.finalizerQueue = finalizerQueue;
        this.depositHandler = depositHandler;
    }

    @Override
    public void run() {
        // check all existing paths for anything in state FINALIZING, retry

        try {
            System.out.println("WATCHING QUEUE: " + taskQueue);
            while (true) {
                var depositTask = taskQueue.take();

                System.out.println("GOT TASK: " + depositTask);
                Runnable task;

                if (depositTask.isRetry()) {
                    System.out.println("NEW DELAYED TASK");
                    task = new DepositFinalizerDelayedTask(depositTask.getDepositId(), 30, taskQueue);
                } else {
                    System.out.println("NEW NORMAL TASK");
                    task = new DepositFinalizer(depositTask.getDepositId(), depositHandler, taskQueue);
                }

                System.out.println("SUBMITTING");
                finalizerQueue.submit(task);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // start loop
        // wait for entries
        // wait for queue to finish

    }

    public static class DepositFinalizerTask {

        private final String depositId;
        private final boolean retry;

        public DepositFinalizerTask(String depositId, boolean retry) {
            this.depositId = depositId;
            this.retry = retry;
        }

        public String getDepositId() {
            return depositId;
        }

        public boolean isRetry() {
            return retry;
        }
    }

    private static class DepositFinalizerDelayedTask implements Runnable {
        private final String id;
        private final long delaySeconds;
        private final BlockingQueue<DepositFinalizerTask> taskQueue ;

        public DepositFinalizerDelayedTask(String id,
            long delaySeconds,
            BlockingQueue<DepositFinalizerTask> taskQueue
        ) {
            this.id = id;
            this.delaySeconds = delaySeconds;
            this.taskQueue = taskQueue;
        }

        @Override
        public void run() {
            try {
                System.out.println("SLEEPING");
                Thread.sleep(delaySeconds * 1000);
                taskQueue.put(new DepositFinalizerTask(id, false));
                System.out.println("DONE");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
