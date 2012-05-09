/* Copyright 2008-2009 WaveMarket, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.veriplace.client.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * In-memory implementation of {@link RequestStore}.
 */
public class MemoryRequestStore<ResultType> 
      implements RequestStore<ResultType> {

   private static Log logger = LogFactory.getLog(MemoryRequestStore.class);

   protected static final long FIVE_MINUTES = 300L;

   protected long allocator = 0L;

   /**
    * Map from identifier to result
    */
   protected Map<Long, RequestStatus<ResultType>> requests =
         new HashMap<Long, RequestStatus<ResultType>>();

   /**
    * Map from expiration time (in seconds) to identifier(s).
    */
   protected SortedMap<Long,LinkedList<Long>> expiration = new TreeMap<Long,LinkedList<Long>>();

   public synchronized long add() {

      // lazy cleanup
      long time = System.currentTimeMillis() / 1000L;

      // for all entries with keys less than current time
      SortedMap<Long,LinkedList<Long>> expired = expiration.headMap(time);
      for (LinkedList<Long> identifiers: expired.values()) {
         // for each identifier
         for (long identifier: identifiers) {
            // remove location result
            logger.debug("Removing result for: " + identifier);
            requests.remove(identifier);
         }
      }
      logger.debug("Clearing: " + expired.size() + " entries");
      expired.clear();

      // insert new value
      long id = ++allocator;
      requests.put(id, new RequestStatus<ResultType>());

      LinkedList<Long> expiring = expiration.get(id);
      if (expiring == null) {
         expiring = new LinkedList<Long>();
         expiration.put(time + FIVE_MINUTES,expiring);
      }
      expiring.add(id);

      logger.debug("Added id: " + id);
      return id;
   }

   public boolean waitForCompletion(long id) {
      return waitForCompletion(id,0L);
   }
   
   public boolean waitForCompletion(long id, long timeout) {

      if (timeout < 0) {
         timeout = 0;
      }

      logger.debug("Waiting for location for id: " + id);

      RequestStatus<ResultType> status = null;
      synchronized (this) {
         status = requests.get(id);
      }

      if (status != null) {
         final long now = System.currentTimeMillis();
         synchronized (status) {
            while (!status.complete && 
                   (timeout == 0 || 
                    System.currentTimeMillis() <= now + timeout)) {
               try {
                  status.wait(timeout);
               } catch (InterruptedException e) {
                  // just loop again
               }
            }
         }
      }

      return status == null ? true : status.complete;
   }

   public void put(long id, ResultType result) {
      logger.debug("Updating result for id: " + id);

      RequestStatus<ResultType> status = null;
      synchronized (this) {
         status = requests.get(id);
      }

      if (status != null) {
         synchronized (status) {
            status.result = result;
            status.complete = true;
            status.notifyAll();
         }
      }
   }

   public synchronized ResultType get(long id) {
      logger.debug("Removing result for id: " + id);
      
      RequestStatus<ResultType> status = requests.get(id);
      return status == null ? null : status.result;
   }

   protected static class RequestStatus<ResultType> {
      public ResultType result = null;
      public boolean complete = false;
   }
}

/*
** Local Variables:
**   c-basic-offset: 3
**   tab-width: 3
**   indent-tabs-mode: nil
** End:
**
** ex: set softtabstop=3 tabstop=3 expandtab cindent shiftwidth=3
*/
