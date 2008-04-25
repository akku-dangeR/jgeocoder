package net.sourceforge.jgeocoder.us;
import static net.sourceforge.jgeocoder.us.AddressRegexLibrary.P_CORNER;
import static net.sourceforge.jgeocoder.us.AddressRegexLibrary.P_INTERSECTION;
import static net.sourceforge.jgeocoder.us.AddressRegexLibrary.P_STREET_ADDRESS;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO: support theses
//123 Avenue of art, philadelphia pa 12345
//PO box 123, abc city, ca 24656
//123 Route 29 South, new jersey, 12323  
public class AddressParser{
  
  public static enum AddressComponent{
    street, name, predir, postdir, type, number, city, state, zip, line2, 
    //intersections
    street2, predir2, postdir2, type2
  }
  private static final Pattern CORNER = Pattern.compile(P_CORNER.getRegex());
  private static final Pattern STREET_ADDRESS = Pattern.compile(P_STREET_ADDRESS.getRegex());
  private static final Pattern INTERSECTION = Pattern.compile(P_INTERSECTION.getRegex());
  private static final Pattern CLEANUP = Pattern.compile("[\\s\\p{Punct}&&[^\\)\\(#&,:;@'`-]]");
  private static final Pattern STREET_TYPES = Pattern.compile(RegexLibrary.STREET_DESIGNATOR);
  private static final Pattern STATES = Pattern.compile(RegexLibrary.US_STATES);
  
  private static String getCleanSttring(String rawAddr){
    return CLEANUP.matcher(rawAddr).replaceAll(" ").replaceAll("\\s+", " ");
  }
  
  public static Map<AddressComponent, String> parseAddress(String rawAddr){
    rawAddr = getCleanSttring(rawAddr);
    Matcher m = CORNER.matcher(rawAddr);
    Map<AddressComponent, String> ret = null;
    if(m.find()){
      m = INTERSECTION.matcher(rawAddr);
      if(m.matches()){
        ret = getAddrMap(m, P_INTERSECTION.getNamedGroupMap());
      }
    }
    if(ret == null){
      m = STREET_ADDRESS.matcher(rawAddr);
      if(m.matches()){
        ret = getAddrMap(m, P_STREET_ADDRESS.getNamedGroupMap());
        postProcess(ret);
      }
    }
    return ret;
  }
  
  private static void postProcess(Map<AddressComponent, String> m){
    //these are (temporary?) hacks...
    if(m.get(AddressComponent.type) == null && m.get(AddressComponent.street)!= null 
            && STREET_TYPES.matcher(m.get(AddressComponent.street).toUpperCase()).matches()){
      m.put(AddressComponent.type, m.get(AddressComponent.street));
      m.put(AddressComponent.street, m.get(AddressComponent.predir));
      m.put(AddressComponent.predir, null);
    }
    if(m.get(AddressComponent.state) == null && m.get(AddressComponent.line2)!= null 
            && STATES.matcher(m.get(AddressComponent.line2).toUpperCase()).matches()){
      m.put(AddressComponent.state, m.get(AddressComponent.line2));
      m.put(AddressComponent.line2, null);
    }
  }
  
  private static Map<AddressComponent, String> getAddrMap(Matcher m, Map<Integer, String> groupMap){
    Map<AddressComponent, String> ret = new HashMap<AddressComponent, String>();
    for(int i=1; i<= m.groupCount(); i++){
      AddressComponent comp = AddressComponent.valueOf(groupMap.get(i));
      if(ret.get(comp) == null){
        putIfNotNull(ret, comp, m.group(i));
      }
    }
    return ret;
  }
  
  private static void putIfNotNull(Map<AddressComponent, String> m , AddressComponent ac, String v){
    if(v != null)
      m.put(ac, v);
  }

}