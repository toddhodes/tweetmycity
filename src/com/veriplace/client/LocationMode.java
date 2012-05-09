package com.veriplace.client;

/**
 * String constants representing the allowable location modes. See
 * {@link com.veriplace.client.GetLocationAPI#getLocation(com.veriplace.oauth.consumer.Token, User, String)}.
 */
public interface LocationMode {

   /**
    * A high-accuracy request that may have higher latency and cost.
    */
   public static final String ZOOM = "zoom";
   
   /**
    * An approximate location request with lower latency and cost.
    */
   public static final String AREA = "area";
}
