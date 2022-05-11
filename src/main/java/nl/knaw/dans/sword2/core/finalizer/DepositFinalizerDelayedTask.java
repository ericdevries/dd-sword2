
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;

class DepositFinalizerDelayedTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(DepositFinalizerDelayedTask.class);

    private final String id;
    private final Duration delay;
    private final BlockingQueue<DepositFinalizerEvent> taskQueue;

    public DepositFinalizerDelayedTask(String id,
        Duration delay,
        BlockingQueue<DepositFinalizerEvent> taskQueue
    ) {
        this.id = id;
        this.delay = delay;
        this.taskQueue = taskQueue;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(delay.toMillis());
            taskQueue.put(new DepositFinalizerEvent(id));
        } catch (InterruptedException e) {
            log.error("Unable to reschedule task because the thread was interrupted", e);
        }
    }
}
