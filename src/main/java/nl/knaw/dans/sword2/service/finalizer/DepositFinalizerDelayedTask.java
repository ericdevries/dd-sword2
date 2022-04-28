
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

class DepositFinalizerDelayedTask implements Runnable {

    private final String id;
    private final long delaySeconds;
    private final BlockingQueue<DepositFinalizerEvent> taskQueue;

    public DepositFinalizerDelayedTask(String id,
        long delaySeconds,
        BlockingQueue<DepositFinalizerEvent> taskQueue
    ) {
        this.id = id;
        this.delaySeconds = delaySeconds;
        this.taskQueue = taskQueue;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(delaySeconds * 1000);
            taskQueue.put(new DepositFinalizerEvent(id));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
