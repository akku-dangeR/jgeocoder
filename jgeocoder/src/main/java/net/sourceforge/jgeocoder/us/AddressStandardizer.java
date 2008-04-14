package net.sourceforge.jgeocoder.us;

import static net.sourceforge.jgeocoder.us.Data.*;
import static net.sourceforge.jgeocoder.us.RegexLibrary.*;
import static net.sourceforge.jgeocoder.us.AddressRegexLibrary.*;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.jgeocoder.us.AddressParser.AddressComponent;
import static net.sourceforge.jgeocoder.us.Utils.*;
public class AddressStandardizer{

  //oh man... what had i got myself into...
  //XXX this class is tightly coupled with the regex library classes
  private static final Pattern TXT_NUM = Pattern.compile("^\\W*("+TXT_NUM_0_99+")\\W*");
  private static final Pattern DIGIT = Pattern.compile("(.*?\\d+)\\W*(.+)?");
  private static String normalizeNum(String num){
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
//  street, name, predir, postdir, type, number, city, state, zip, line2
  private static String normalizeDir(String dir){
    dir = dir.replace(" ", "");
    return dir.length() > 2 ? getDIRECTIONAL_MAP().get(dir): dir;
  }
  
  private static String normalizeType(String type){
    return nvl(getSTREET_TYPE_MAP().get(type), type);
  }
  
  private static String normalizeState(String state){
    return nvl(getSTATE_CODE_MAP().get(state), state);
  }
  private static final Pattern LINE2A = Pattern.compile("\\W*(?:"+LINE2A_GROUPED+")\\W*");
  private static String normalizeLine2(String line2){
    Matcher m = LINE2A.matcher(line2);
    if(m.matches()){
      for(Map.Entry<String, String> e : getUNIT_MAP().entrySet()){
        if(line2.startsWith(e.getKey()+" ")){
          
        }
      }
    }
    return line2;
  }
  
  public static void main(String[] args) {
    System.out.println(normalizeDir(" S  W"));
  }
  
  public static Map<AddressComponent, String>  normalizeParsedAddress(Map<AddressComponent, String> parsedAddr){
    //just take the digits from the number component
    
    return null;
  }
}