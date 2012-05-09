/* Copyright 2008-2010 WaveMarket, Inc.
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

import com.veriplace.client.BadParameterException;
import com.veriplace.client.Client;
import com.veriplace.client.UnexpectedException;
import com.veriplace.client.User;
import com.veriplace.client.VeriplaceException;
import com.veriplace.oauth.consumer.Token;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstraction around making a Veriplace API request and retrieving the result.
 * <p>
 * Requests can block for some time, and the user interface should not be coupled
 * too tightly with the request behavior.  This implementation performs all requests
 * on background threads.  The caller can then block for the result at any time;
 * typically the blocking wait will happen after we've displayed an automatically-refreshing
 * "please wait" page.
 */
public class AbstractRequestManager<ResultType> {

   private static final Log logger = LogFactory.getLog(AbstractRequestManager.class);

   protected RequestStore<ResultWrapper<ResultType>> requestStore =
         new MemoryRequestStore<ResultWrapper<ResultType>>();
   private ExecutorService executor = Executors.newCachedThreadPool();

   public void setRequestStore(RequestStore<ResultWrapper<ResultType>> requestStore) {
      this.requestStore = requestStore;
   }

   public void setExecutorService(ExecutorService executor) {
      this.executor = executor;
   }
   
   /**
    * Submit a request object (which was created by some method of the specific
    * AbstractRequestManager sucblass) for asynchronous processing.
    * @return  the new request ID.
    */
   public long submitRequest(AbstractRequest<ResultType> request) {
      request.setRequestStore(requestStore);
      long id = requestStore.add();
      request.setId(id);
      executor.submit(request);
      return id;
   }
   
   /**
    * Wait for the result of a request to be available.
    * @return true
    */
   public boolean waitForCompletion(long id) {
      return requestStore.waitForCompletion(id);
   }

   /**
    * Wait for the result of a request to be available.
    * @return if the result was available or a timeout occurred
    */
   public boolean waitForCompletion(long id, long timeout) {
      return requestStore.waitForCompletion(id, timeout);
   }

   /**
    * Get (and remove) the result returned for the given request id,
    * assuming that the request has completed.
    * @return the result obtained, or null if the request has not completed
    * @throws VeriplaceException  if the request completed with an error result
    */
   public ResultType getResult(long id) throws VeriplaceException {
      ResultWrapper<ResultType> wrapper = getResultAndToken(id);
      if (wrapper == null) {
         return null;
      }
      return wrapper.getResult();
   }

   /**
    * Get (and remove) the result returned for the given request id,
    * assuming that the request has completed.
    * @return an object containing the result obtained, and the access token that
    *   was used for the request
    * @throws VeriplaceException  if the request completed with an error result
    * @throws BadParameterException  if the request ID does not exist or the
    *   request has not completed 
    */
   public ResultWrapper<ResultType> getResultAndToken(long id) throws VeriplaceException {
      ResultWrapper<ResultType> wrapper = requestStore.get(id);
      if (wrapper == null) {
         throw new BadParameterException("Invalid request ID: " + id);
      }
      if (wrapper.getException() != null) {
         throw wrapper.getException();
      }
      return wrapper;
   }

   /**
    * Generic inner class for background requests.
    */
   public static abstract class AbstractRequest<ResultType>
         implements Runnable {

      protected final Client client;
      protected final User user;
      protected final Token accessToken;
      protected long id;
      private RequestStore<ResultWrapper<ResultType>> requestStore;
      
      protected AbstractRequest(Client client, User user, Token accessToken) {
         this.client = client;
         this.user = user;
         this.accessToken = accessToken;
      }
      
      public long getId() {
         return id;
      }

      public void run() {
         try {
            ResultType result = call();
            storeResult(result);
         }
         catch (VeriplaceException e) {
            storeException(e);
         }
         catch (Throwable t) {
            storeException(new UnexpectedException(t));
         }
      }
      
      protected abstract ResultType call()
            throws VeriplaceException;
      
      protected void setId(long id) {
         this.id = id;
      }
      
      protected void setRequestStore(RequestStore<ResultWrapper<ResultType>> requestStore) {
         this.requestStore = requestStore;
      }
      
      protected void storeResult(ResultType result) {
         logger.debug("Storing result for request ID: " + id);
         requestStore.put(id, new ResultWrapper<ResultType>(result, accessToken));
      }
      
      protected void storeException(VeriplaceException exception) {
         logger.debug("Storing exception result for request ID: " + id);
         requestStore.put(id, new ResultWrapper<ResultType>(exception));
      }
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
