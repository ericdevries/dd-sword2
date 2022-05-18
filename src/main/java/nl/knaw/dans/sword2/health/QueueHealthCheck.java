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
package nl.knaw.dans.sword2.health;

import com.codahale.metrics.health.HealthCheck;

import java.util.concurrent.BlockingQueue;

public class QueueHealthCheck extends HealthCheck {

    private final BlockingQueue<?> queue;

    public QueueHealthCheck(BlockingQueue<?> queue) {
        this.queue = queue;
    }

    @Override
    protected Result check() throws Exception {
        var remaining = queue.remainingCapacity();
        var size = queue.size();
        var ratio = size / ((float) (remaining + size));
        var result = Result.builder();

        result.withMessage("Queue usage is at %.1f%%", ratio * 100);

        // warn at 90%
        if (ratio > 0.9f) {
            return result.withMessage("Queue is almost full: %s remaining capacity vs %s used (ratio: %.1f%%)", remaining, size, ratio * 100).unhealthy().build();
        }

        return result.healthy().build();
    }
}
