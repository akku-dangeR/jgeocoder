package net.sourceforge.jgeocoder.us;
import static net.sourceforge.jgeocoder.us.AddressRegexLibrary.P_CORNER;
import static net.sourceforge.jgeocoder.us.AddressRegexLibrary.P_INTERSECTION;
import static net.sourceforge.jgeocoder.us.AddressRegexLibrary.P_STREET_ADDRESS;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddressParser{
  
  public static enum AddressComponent{
    street, name, predir, postdir, type, number, city, state, zip, line2
  }
  private static final Pattern CORNER = Pattern.compile(P_CORNER.getRegex(), Pattern.CASE_INSENSITIVE);
  private static final Pattern STREET_ADDRESS = Pattern.compile(P_STREET_ADDRESS.getRegex(), Pattern.CASE_INSENSITIVE);
  private static final Pattern INTERSECTION = Pattern.compile(P_INTERSECTION.getRegex(), Pattern.CASE_INSENSITIVE);
  
  public static Map<AddressComponent, String> parseAddress(String rawAddr){
    Matcher m = CORNER.matcher(rawAddr);
    if(m.find()){
      m = INTERSECTION.matcher(rawAddr);
      if(m.matches()){
        return getAddrMap(m, P_INTERSECTION.getNamedGroupMap());
      }
    }else{
      m = STREET_ADDRESS.matcher(rawAddr);
      if(m.matches()){
        return getAddrMap(m, P_STREET_ADDRESS.getNamedGroupMap());
      }
    }
    return null;
  }
  
  private static Map<AddressComponent, String> getAddrMap(Matcher m, Map<Integer, String> groupMap){
    Map<AddressComponent, String> ret = new HashMap<AddressComponent, String>();
    for(int i=1; i<= m.groupCount(); i++){
      AddressComponent comp = AddressComponent.valueOf(groupMap.get(i));
      if(ret.get(comp) == null)
        ret.put(comp, m.group(i));
    }
    return ret;
  }
  

}