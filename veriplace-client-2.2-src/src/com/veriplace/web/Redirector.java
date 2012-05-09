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
package com.veriplace.web;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interface for an object that can redirect the end user to a different URL.  The default
 * implementation of this is {@link com.veriplace.web.DefaultRedirector}; you may wish to
 * provide a different implementation if your application runs in an environment where
 * standard HTTP redirects do not work.
 * @since 2.0
 */
public interface Redirector {

   public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url)
         throws IOException;
}
