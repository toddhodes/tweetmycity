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
package org.tweetmycity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import twitter4j.http.AccessToken;


public class TmcUser {

   private static final Log logger = LogFactory.getLog(TmcUser.class);

   private long userId;
   private String twitterToken;
   private String twitterTokenSecret;
   private String deviceDescription;
   private String lastCityState;

   public TmcUser(long uId, String tTok, String tTokSec, String dev, String cityState) {
      userId = uId;
      twitterToken = tTok;
      twitterTokenSecret = tTokSec;

      deviceDescription = dev;
      if (deviceDescription == null)
         deviceDescription = "phone";

      lastCityState = cityState;
      if (lastCityState == null)
         lastCityState = "unknown";
   }

   public void updateLastCity(String cs) {
      lastCityState = cs;
   }

   public void updateDeviceDescription(String dd) {
      deviceDescription = dd;
   }

   public long getUserId() { return userId; }

   public String getTwitterToken() { return twitterToken; }
   public String getTwitterTokenSecret() { return twitterTokenSecret; }
   public AccessToken getAccessToken() { 
      return new AccessToken(getTwitterToken(), getTwitterTokenSecret()); 
   }

   public String getDeviceDescription() { return deviceDescription; }
   public String getLastCityState() { return lastCityState; }

   public String toString() {
      return "[TmcUser: vpId=" + userId 
         + ", twitterToken=" + twitterToken
         //+ ", twitterTokenSecret=" + twitterTokenSecret
         + ", device=" + deviceDescription
         + ", lastloc=" + lastCityState
         + "]";
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


