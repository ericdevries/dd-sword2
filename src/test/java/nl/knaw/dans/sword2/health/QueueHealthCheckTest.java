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

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class QueueHealthCheckTest {

    @Test
    void check() throws Exception {
        var queue = Mockito.mock(BlockingQueue.class);
        Mockito.when(queue.size()).thenReturn(0);
        Mockito.when(queue.remainingCapacity()).thenReturn(1000);

        var result = new QueueHealthCheck(queue).check();
        assertTrue(result.isHealthy());
    }

    @Test
    void checkHealthy() throws Exception {
        var queue = Mockito.mock(BlockingQueue.class);
        Mockito.when(queue.size()).thenReturn(100);
        Mockito.when(queue.remainingCapacity()).thenReturn(1000);

        var result = new QueueHealthCheck(queue).check();

        assertTrue(result.isHealthy());
    }

    @Test
    void checkHealthyButVeryCloseToUnhealty() throws Exception {
        var queue = Mockito.mock(BlockingQueue.class);

        // assuming size of 1000
        Mockito.when(queue.size()).thenReturn(899);
        Mockito.when(queue.remainingCapacity()).thenReturn(101);

        var result = new QueueHealthCheck(queue).check();

        assertTrue(result.isHealthy());
    }

    @Test
    void checkUnhealthyButVeryCloseToHealty() throws Exception {
        var queue = Mockito.mock(BlockingQueue.class);

        // assuming size of 1000
        Mockito.when(queue.size()).thenReturn(901);
        Mockito.when(queue.remainingCapacity()).thenReturn(99);

        var result = new QueueHealthCheck(queue).check();

        assertFalse(result.isHealthy());
    }

    @Test
    void checkCompletelyFull() throws Exception {
        var queue = Mockito.mock(BlockingQueue.class);

        // assuming size of 1000
        Mockito.when(queue.size()).thenReturn(1000);
        Mockito.when(queue.remainingCapacity()).thenReturn(0);

        var result = new QueueHealthCheck(queue).check();

        assertFalse(result.isHealthy());
    }
}