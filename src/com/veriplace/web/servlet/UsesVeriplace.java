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
package com.veriplace.web.servlet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Attach this annotation to a servlet class derived from {@link AbstractVeriplaceServlet} to
 * specify what Veriplace requests to make.  It has no effect if used in a class that is not
 * derived from AbstractVeriplaceServlet.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface UsesVeriplace {
   
   /**
    * Set this property to <tt>true</tt> if the servlet needs to know the current Veriplace user.
    * <p>
    * This property triggers the Veriplace user discovery process, which may include a redirect
    * to an external page.  If the process fails or is cancelled by the user, the request will
    * be redirected to an error page.
    * <p>
    * If you specify {@link #requireLocation()}, the location request includes discovery of the
    * current user; you only need to specify {@link #requireUser()} if you need to do something
    * different, such as getting the user ID when you are not requesting the location, or
    * specifying {@link #allowUserInteraction()} = <tt>false</tt>.
    */
   boolean requireUser() default false;
   
   /**
    * This property is true (the default) if Veriplace is allowed to solicit user interaction (i.e. show
    * a login page).  Set it to false if user discovery can only use the current login cookie.
    * <p>
    * If you need the value of this property to vary depending on some request parameter, you can set it by
    * overriding {@link AbstractVeriplaceServlet#setupVeriplaceState(javax.servlet.http.HttpServletRequest, com.veriplace.web.VeriplaceState)}
    * instead.
    */
   boolean allowUserInteraction() default true;
   
   /**
    * Set this property to <tt>true</tt> if the servlet needs to know the current user's location.
    * <p>
    * This property triggers a Veriplace location request, which may include a redirect to an
    * external page.  If the process fails or is cancelled by the user, the request will be
    * redirected to an error page.
    */
   boolean requireLocation() default false;

   /**
    * Specifies the method or degree of accuracy for obtaining location. The names of allowable
    * location modes are defined in {@link com.veriplace.client.LocationMode}.
    * <p>
    * If you need the value of this property to vary depending on some request parameter, you can set it by
    * overriding {@link AbstractVeriplaceServlet#setupVeriplaceState(javax.servlet.http.HttpServletRequest, com.veriplace.web.VeriplaceState)}
    * instead.
    */
   String mode() default "";
   
   /**
    * Set this property to <tt>true</tt> if the servlet needs permission to change the current user's
    * location.  This capability is not supported in the current Veriplace platform; see
    * {@link com.veriplace.client.SetLocationAPI}.
    * <p>
    * This property triggers a Veriplace permission request, which may include a redirect to an
    * external page.  If the process fails or is cancelled by the user, the request will be
    * redirected to an error page.
    */
   boolean requireSetLocationPermission() default false;
}
