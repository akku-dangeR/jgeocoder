package geo.streetaddress.us;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

class PerlLikeUtils{
  
  /**
   * joins both key and values sets
   */
  @SuppressWarnings("unchecked")
  public static String getJoinString(char separator, Map<String, String> map){
    return joinAll(separator, map.keySet(), map.values());
  }
  
  /**
   * joins all collections
   */
  public static String joinAll(char separator, Collection<String>...collections){
    StringBuilder sb = new StringBuilder();
    for(Collection<String> c : collections){
      sb.append(separator);
      sb.append(StringUtils.join(c, separator));
    }
    return sb.toString().substring(1);
  }
  
}