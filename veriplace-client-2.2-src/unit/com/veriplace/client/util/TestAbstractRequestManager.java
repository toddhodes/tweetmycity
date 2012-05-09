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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.veriplace.client.Client;
import com.veriplace.client.GetLocationException;
import com.veriplace.client.TestData;
import com.veriplace.client.VeriplaceException;
import com.veriplace.client.util.AbstractRequestManager.AbstractRequest;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link com.veriplace.client.util.AbstractRequestManager}.
 */
public class TestAbstractRequestManager
      implements TestData {

   private TestRequestManager manager;
   private TestRequestStore store;
   private final Client client;
   private Object requestCompletionSignal;
   
   public TestAbstractRequestManager() throws Exception {
      client = new Client(CONSUMER_KEY, CONSUMER_SECRET);
   }
   
   @Before
   public void setUp() throws Exception {
      store = new TestRequestStore();
      manager = new TestRequestManager();
      manager.setRequestStore(store);
      requestCompletionSignal = new Object();
   }
   
   @Test
   public void testWaitForNonexistentRequest() {
      // If request ID isn't found, waitForCompletion returns true (don't block for this request)
      
      long requestId = 1;
      boolean ready = manager.waitForCompletion(requestId);
      assertTrue(ready);
   }

   @Test
   public void testWaitRequestCompleted() throws Exception {
      TestRequest request = new TestRequest(client, "foo");
      long requestId = manager.submitRequest(request);
      
      request.complete();
      
      boolean ready = manager.waitForCompletion(requestId);
      assertTrue(ready);
      
      String result = manager.getResult(requestId);
      assertEquals("foo", result);
   }

   @Test
   public void testWaitRequestCompletedWithToken() throws Exception {
      TestRequest request = new TestRequest(client, "foo");
      long requestId = manager.submitRequest(request);
      
      request.complete();
      
      boolean ready = manager.waitForCompletion(requestId);
      assertTrue(ready);
      
      ResultWrapper<String> result = manager.getResultAndToken(requestId);
      assertNotNull(result);
      assertEquals("foo", result.getResult());
      assertSame(ACCESS_TOKEN, result.getToken());
   }

   @Test
   public void testWaitTimeout() {
      TestRequest request = new TestRequest(client, "foo");
      long requestId = manager.submitRequest(request);
      
      boolean ready = manager.waitForCompletion(requestId, 50L);
      assertFalse(ready);
      assertFalse(request.isCompleted());
      
      request.complete();

      ready = manager.waitForCompletion(requestId, 50L);
      assertTrue(ready);
      assertTrue(request.isCompleted());
   }
   
   @Test
   public void testResultError() throws Exception {
      TestRequest request = new TestRequest(client, "foo");
      long requestId = manager.submitRequest(request);
      
      VeriplaceException exception = new GetLocationException();
      request.completeError(exception);
      
      boolean ready = manager.waitForCompletion(requestId);
      assertTrue(ready);
      
      try {
         manager.getResult(requestId);
         fail("Expected exception");
      }
      catch (VeriplaceException e) {
         assertSame(exception, e);
      }
   }
   
   public static class TestRequestManager
         extends AbstractRequestManager<String> {
      
   }

   public static class TestRequestStore
         extends MemoryRequestStore<ResultWrapper<String>> {
      
   }
   
   public class TestRequest
         extends AbstractRequest<String> {

      private final String resultValue;
      private boolean completed;
      private VeriplaceException exception;
      
      public TestRequest(Client client, String resultValue) {
         super(client, USER, ACCESS_TOKEN);
         this.resultValue = resultValue;
         this.completed = false;
      }

      @Override
      protected String call() throws VeriplaceException {
         synchronized(this) {
            if (! completed) {
               try {
                  wait();
               }
               catch (InterruptedException e) {
               }
            }
         }
         if (exception != null) {
            throw exception;
         }
         return resultValue;
      }
      
      public synchronized void complete() {
         completed = true;
         notifyAll();
      }
      
      public synchronized void completeError(VeriplaceException exception) {
         this.exception = exception;
         complete();
      }
      
      public boolean isCompleted() {
         return completed;
      }
   }
}
