/* Copyright 2008 WaveMarket, Inc.
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
package com.veriplace.example.spring.map;

import com.veriplace.client.BadParameterException;
import com.veriplace.client.UserDiscoveryException;
import com.veriplace.client.UserDiscoveryParameters;
import com.veriplace.web.VeriplaceState;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

/**
 * Simple Spring implementation of user discovery by search parameters (phone number, email,
 * or OpenID).  Derived from SimpleFormController, so our configuration file can specify what
 * view to display when the form is submitted.
 */
public class MapFindUserController extends SimpleFormController {

   @Override
   protected boolean isFormSubmission(HttpServletRequest request) {
      return (request.getParameter("find") != null);
   }

   @Override
   protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response,
            Object command, BindException errors) throws Exception {
      UserDiscoveryParameters params = (UserDiscoveryParameters) command;
      VeriplaceState state = VeriplaceState.getFromRequest(request);

      // Attempt to find the user.
      try {
         state.requireUser(params);
      } catch (BadParameterException e) {
         ModelAndView mv = showForm(request, response, errors);
         mv.addObject("error", "Please enter a search parameter.");
         return mv;
			} catch (UserDiscoveryException e) {
         ModelAndView mv = showForm(request, response, errors);
         mv.addObject("error", "User search failed (not found, or insufficient permission).");
         return mv;
      }

      // Call the superclass implementation, which returns the success view that's defined in our configuration.
      return super.onSubmit(command, errors);
   }
}
