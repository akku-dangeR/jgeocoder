package net.sourceforge.jgeocoder.us;
import static net.sourceforge.jgeocoder.AddressComponent.LINE2;
import static net.sourceforge.jgeocoder.AddressComponent.PREDIR;
import static net.sourceforge.jgeocoder.AddressComponent.STATE;
import static net.sourceforge.jgeocoder.AddressComponent.STREET;
import static net.sourceforge.jgeocoder.AddressComponent.TYPE;
import static net.sourceforge.jgeocoder.AddressComponent.valueOf;
import static net.sourceforge.jgeocoder.us.AddressRegexLibrary.P_CORNER;
import static net.sourceforge.jgeocoder.us.AddressRegexLibrary.P_CSZ;
import static net.sourceforge.jgeocoder.us.AddressRegexLibrary.P_INTERSECTION;
import static net.sourceforge.jgeocoder.us.AddressRegexLibrary.P_STREET_ADDRESS;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.jgeocoder.AddressComponent;

//TODO: support theses
//123 Avenue of art, philadelphia pa 12345
//PO box 123, abc city, ca 24656
//123 Route 29 South, new jersey, 12323  
/**
 * TODO javadocs me
 * @author jliang
 *
 */
public class AddressParser{

  private static final Pattern CORNER = Pattern.compile(P_CORNER.getRegex());
  private static final Pattern STREET_ADDRESS = Pattern.compile(P_STREET_ADDRESS.getRegex());
  private static final Pattern CSZ = Pattern.compile(P_CSZ.getRegex());
  private static final Pattern INTERSECTION = Pattern.compile(P_INTERSECTION.getRegex());
  private static final Pattern CLEANUP = Pattern.compile("[\\s\\p{Punct}&&[^\\)\\(#&,:;@'`-]]");
  private static final Pattern STREET_TYPES = Pattern.compile(RegexLibrary.STREET_DESIGNATOR);
  private static final Pattern STATES = Pattern.compile(RegexLibrary.US_STATES);
  
  private static String getCleanSttring(String rawAddr){
    return CLEANUP.matcher(rawAddr).replaceAll(" ").replaceAll("\\s+", " ");
  }
  
  public static Map<AddressComponent, String> parseAddress(String rawAddr){
    rawAddr = getCleanSttring(rawAddr);
    Matcher m = STREET_ADDRESS.matcher(rawAddr);
    Map<AddressComponent, String> ret = null;
    if(m.matches()){
      ret = getAddrMap(m, P_STREET_ADDRESS.getNamedGroupMap());
      postProcess(ret);
      String splitRawAddr = null;
      if((splitRawAddr = designatorConfusingCitiesCorrection(ret, rawAddr))!=null){
        m = STREET_ADDRESS.matcher(splitRawAddr);
        if(m.matches()){
          ret = getAddrMap(m, P_STREET_ADDRESS.getNamedGroupMap());
          return ret;
        }
      }
    }
    m = CORNER.matcher(rawAddr);
    if(ret == null && m.find()){
      m = INTERSECTION.matcher(rawAddr);
      if(m.matches()){
        ret = getAddrMap(m, P_INTERSECTION.getNamedGroupMap());
      }
    }
    
    if(ret == null){
      m = CSZ.matcher(rawAddr);
      if(m.matches()){
        ret = getAddrMap(m, P_CSZ.getNamedGroupMap());
      }
    }
    return ret;
  }
  
  private static void postProcess(Map<AddressComponent, String> m){
    //these are (temporary?) hacks...
    if(m.get(TYPE) == null && m.get(STREET)!= null 
            && STREET_TYPES.matcher(m.get(STREET).toUpperCase()).matches()){
      m.put(TYPE, m.get(STREET));
      m.put(STREET, m.get(PREDIR));
      m.put(PREDIR, null);
    }
    if(m.get(STATE) == null && m.get(LINE2)!= null 
            && STATES.matcher(m.get(LINE2).toUpperCase()).matches()){
      m.put(STATE, m.get(LINE2));
      m.put(LINE2, null);
    }
  }
  
  private static Map<AddressComponent, String> getAddrMap(Matcher m, Map<Integer, String> groupMap){
    Map<AddressComponent, String> ret = new HashMap<AddressComponent, String>();
    for(int i=1; i<= m.groupCount(); i++){
      AddressComponent comp = valueOf(groupMap.get(i));
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

  //TODO: document this craziness
  private static Pattern STREET_DESIGNATOR_CHECK = Pattern.compile("\\b(?i:(?:"+RegexLibrary.STREET_DESIGNATOR+"))\\b");
  private static String designatorConfusingCitiesCorrection(Map<AddressComponent, String> m, String input){
    String street = m.get(AddressComponent.STREET);
    String type = m.get(AddressComponent.TYPE);
    if(street == null || type == null || street.split(" ").length < 2){ return null;}
    
    int start = street.indexOf(' ')+1;
    Matcher matcher = STREET_DESIGNATOR_CHECK.matcher(street);
    if(matcher.find(start)){ //if the street itself also contains some designator
      String inputUpper = input.toUpperCase();
      for(String s : Data.DESIGNATOR_CONFUSING_CITIES_SET){ 
        int idx = -1;
        if((idx =inputUpper.indexOf(s))!=-1){ //and the input has one of the city names that can confuse the parser
          //this almost guaranteed to break the parser, help the parser by putting a comma separator before the city
          return input.substring(0, idx)+","+input.substring(idx);
        }
      }
    }
    
    return null;
    
  }  
}