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

import io.dropwizard.lifecycle.Managed;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import nl.knaw.dans.sword2.service.DepositHandler;


// TODO make a separate executor service for managing retries, so we can call shutdownNow to exit faster
public class DepositFinalizerManager implements Managed {

    private final Thread depositFinalizerListenerThread;
    private final BlockingQueue<DepositFinalizerEvent> taskQueue;
    private final ExecutorService finalizerQueue;

    public DepositFinalizerManager(ExecutorService finalizerQueue,
        DepositHandler depositHandler,
        BlockingQueue<DepositFinalizerEvent> taskQueue
    ) {
        this.depositFinalizerListenerThread = new Thread(new DepositFinalizerListener(taskQueue,
            finalizerQueue,
            depositHandler));
        this.taskQueue = taskQueue;
        this.finalizerQueue = finalizerQueue;
    }


    @Override
    public void start() throws Exception {
        this.depositFinalizerListenerThread.start();
    }

    @Override
    public void stop() throws Exception {
        this.taskQueue.put(new DepositFinalizerStopEvent());
        this.finalizerQueue.shutdown();
    }
}
