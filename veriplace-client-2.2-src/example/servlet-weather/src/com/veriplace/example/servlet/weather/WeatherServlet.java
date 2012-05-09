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
package com.veriplace.example.servlet.weather;

import com.veriplace.client.LocationMode;
import com.veriplace.web.servlet.AbstractVeriplaceServlet;
import com.veriplace.web.servlet.UsesVeriplace;

/**
 */
@UsesVeriplace(
      requireLocation = true,
      mode = LocationMode.AREA
)
public class WeatherServlet 
   extends AbstractVeriplaceServlet {

   // The super class doRequestInternal() method will forward to the
   // "veriplace.defaultview" parameter in web.xml when location is obtained.

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
