package net.sourceforge.jgeocoder.us;


import static net.sourceforge.jgeocoder.us.AddressRegexLibrary.LINE2A_GROUPED;
import static net.sourceforge.jgeocoder.us.Data.getDIRECTIONAL_MAP;
import static net.sourceforge.jgeocoder.us.Data.getNUMBER_MAP;
import static net.sourceforge.jgeocoder.us.Data.getSTATE_CODE_MAP;
import static net.sourceforge.jgeocoder.us.Data.getSTREET_TYPE_MAP;
import static net.sourceforge.jgeocoder.us.Data.getUNIT_MAP;
import static net.sourceforge.jgeocoder.us.RegexLibrary.TXT_NUM_0_99;
import static net.sourceforge.jgeocoder.us.Utils.nvl;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.jgeocoder.us.AddressParser.AddressComponent;
import static net.sourceforge.jgeocoder.us.AddressParser.AddressComponent.*;
import org.apache.commons.lang.StringUtils;
public class AddressStandardizer{
  /**
   * Turn input map into one line of format
   * 
   * {name, num predir street type postdir, line2, city, state, zip}
   * 
   * @param parsedAddr
   * @return
   */
  public static String toSingleLine(Map<AddressComponent, String> parsedAddr){
    StringBuilder sb = new StringBuilder();
    appendIfNotNull(sb, parsedAddr.get(name), ", ");
    appendIfNotNull(sb, parsedAddr.get(number), " ");
    appendIfNotNull(sb, parsedAddr.get(predir), " ");
    appendIfNotNull(sb, parsedAddr.get(street), " ");
    if(parsedAddr.get(street2) != null){
      appendIfNotNull(sb, parsedAddr.get(type2), " ");
      appendIfNotNull(sb, parsedAddr.get(postdir2), " ");
      sb.append("& ");
      appendIfNotNull(sb, parsedAddr.get(predir2), " ");
      appendIfNotNull(sb, parsedAddr.get(street2), " ");
    }
    appendIfNotNull(sb, parsedAddr.get(type), " ");
    appendIfNotNull(sb, parsedAddr.get(postdir), " ");
    if(StringUtils.isNotBlank(sb.toString())){
      sb.append(", ");
    }
    appendIfNotNull(sb, parsedAddr.get(line2), ", ");
    appendIfNotNull(sb, parsedAddr.get(city), ", ");
    appendIfNotNull(sb, parsedAddr.get(state), " ");
    appendIfNotNull(sb, parsedAddr.get(zip), " ");
    return sb.toString().replaceAll(" ,", ",");
  }
  
  private static void appendIfNotNull(StringBuilder sb, String s, String suffix){
    if(s != null){
      sb.append(s).append(suffix);
    }
  }
  
  /**
   * Normalize the input parsedAddr map into a standardize format
   * 
   * @param parsedAddr
   * @return normalized address in a map
   */
  public static Map<AddressComponent, String>  normalizeParsedAddress(Map<AddressComponent, String> parsedAddr){
    Map<AddressComponent, String> ret = new HashMap<AddressComponent, String>();
    //just take the digits from the number component
    for(Map.Entry<AddressComponent, String> e : parsedAddr.entrySet()){
      String v = StringUtils.upperCase(e.getValue());
      switch (e.getKey()) {
        case predir: ret.put(AddressComponent.predir, normalizeDir(v)); break;
        case postdir: ret.put(AddressComponent.postdir, normalizeDir(v)); break;
        case type: ret.put(AddressComponent.type, normalizeStreetType(v)); break;
        case predir2: ret.put(AddressComponent.predir2, normalizeDir(v)); break;
        case postdir2: ret.put(AddressComponent.postdir2, normalizeDir(v)); break;
        case type2: ret.put(AddressComponent.type2, normalizeStreetType(v)); break;
        case number: ret.put(AddressComponent.number, normalizeNum(v)); break;
        case state: ret.put(AddressComponent.state, normalizeState(v)); break;
        case zip: ret.put(AddressComponent.zip, normalizeZip(v)); break;
        case line2: ret.put(AddressComponent.line2, normalizeLine2(v)); break;                    
        default: ret.put(e.getKey(), v); break;
      }
    }
    return ret;
  }
  //oh man... what had i got myself into...
  //XXX this class is tightly coupled with the regex library classes
  private static final Pattern TXT_NUM = Pattern.compile("^\\W*("+TXT_NUM_0_99+")\\W*");
  private static final Pattern DIGIT = Pattern.compile("(.*?\\d+)\\W*(.+)?");
  private static String normalizeNum(String num){
    if(num == null) return null;
    Matcher m = TXT_NUM.matcher(num);
    String ret = null;
    if(m.matches()){
      ret = m.group(1);
      if(ret.contains("-") || ret.contains(" ")){//it's a 2 part number
        String[] pair = ret.split("[ -]");
        String pre = getNUMBER_MAP().get(pair[0]).substring(0, 1);
        ret = pre+getNUMBER_MAP().get(pair[1]);
      }else{
        ret = getNUMBER_MAP().get(ret);
      }
    }else{
      m = DIGIT.matcher(num);
      if(m.matches()){
        ret = m.group(2) == null? m.group(1): m.group(1)+"-"+m.group(2);
      }
    }
    return nvl(ret, num) ;
  }

  private static String normalizeDir(String dir){
    if(dir == null) return null;
    dir = dir.replace(" ", "");
    return dir.length() > 2 ? getDIRECTIONAL_MAP().get(dir): dir;
  }
  
  private static String normalizeStreetType(String type){
    return nvl(getSTREET_TYPE_MAP().get(type), type);
  }
  
  private static String normalizeState(String state){
    return nvl(getSTATE_CODE_MAP().get(state), state);
  }
  private static final Pattern LINE2A = Pattern.compile("\\W*(?:"+LINE2A_GROUPED+")\\W*");
  private static String normalizeLine2(String line2){
    if(line2 == null) return null;
    Matcher m = LINE2A.matcher(line2);
    if(m.matches()){
      for(Map.Entry<String, String> e : getUNIT_MAP().entrySet()){
        if(line2.startsWith(e.getKey()+" ")){
          line2 = line2.replaceFirst(e.getKey()+" ", e.getValue()+" ");
          break;
        }
      }
    }
    return line2;
  }
  
  private static String normalizeZip(String zip){
    return StringUtils.length(zip) > 5 ? zip.substring(0, 5) : zip;
  }
  

}