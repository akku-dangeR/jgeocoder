package geo.streetaddress.us;
import static geo.streetaddress.us.AddressData.*;
import static geo.streetaddress.us.PerlLikeUtils.*;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;

class AddressPattern{
  
  //need to use this to make more adjustment to address before passing to geostan
  //geostan does not know about '222 St Paul St' but it knows '222 Saint Paul St'
  public static final String SAINT_NAME = "(?:ANTOINE|JOSEPH|PAUL|MARRY|PETER|LUKE|LOUI|ONTARIO|ANDREW|VINCENT" +
    "|CLAIR|FRANCI|BOYNE|MICHAEL|CLEMENT|MARY|ANTHONYS|JAME|NICHOLA|SEBASTIAN|LUCIE|CATHERINE" +
    "|IMMANUEL|PHIPP|ANNE|CHRISTOPHER|JOHN|PETER|CHARLE|CECIL[L]?E|CHAD|KEVIN|PATRICK|RICHAEL" +
    "|SANTO)S?";
  //this is used to protect the parser from splitting prematurely on 'st' when 
  //'st' is used for 'saint' as in 'st paul', 'st joseph' etc
  private static final String ST_NEGATIVE_LOOKAHEAD = 
    "(?![ ]+"+SAINT_NAME+")";
  
  //This is for street type such as 'ROUTE 1', 'HIGHWAY 36' etc
  private static final String SPECIAL_STREET_TYPE = 
     "(?:ROUTE|RT|RTE|HIGHWAY|HIGHWY|HWY|HWAY|LOOP|LOOPS)\\W+\\d+(?:-?\\p{Alpha})?" +
     "|" +
     //Ave of art, avenue of america, etc
     "(?:AV|AVE|AVEN|AVENU|AVENUE|AVN|AVNUE)\\W+OF\\W+(?:\\p{Alpha}+)";
  
  //street type street|str|ave|...
  public static final String TYPE =  
    joinAll('|', new HashSet<String>(STREET_TYPE_MAP.values()), STREET_TYPE_MAP.keySet() );

  //this pattern is used to protect the parser from taking things like '123 Y' in
  //'123 Y ST' as the street num, since 'Y' is a street name in this case
  public static final String NOT_STREET_TYPE_LOOKAHEAD = 
    "(?!\\s+(?:st|street|ave|aven|avenu|avenue|blvd|boulv|boulevard|boulv|plz|plaza|plza))";
  
  //123, 123-a, 123B, 123 B etc, but not 123 N because N is likely to mean North 
  public static final String NUMBER = 
    "(?:\\p{Alpha})?\\d+(?:[- ][\\p{Alpha}&&[^NSEW]]"+NOT_STREET_TYPE_LOOKAHEAD+")?" +
    "|\\d+-?\\d*(?:-?\\p{Alpha})?" +
    "|one|two|three|four|five|six|seven|eight|nine|ten"; //sometimes ppl like to put 'ONE MAIN ST'
  //instead of '1 MAIN ST' when the numbers are small

  //1/2, 23/124
  private static final String FRACTION = "\\d+/\\d+";
  //pa|ny|ca|...
  private static final String STATE = joinAll('|', STATE_CODE_MAP.keySet(), STATE_CODE_MAP.values());
  
  //direction: N|N.|south|SouthWest|SW|...
  public static final String DIRECT = joinAll('|', DIRECTIONAL_MAP.keySet(), transformDirection(), DIRECTIONAL_MAP.values());
  
  private static final String ZIP = "\\d{5}(?:[- ]\\d{4})?";
  
  //this is used to identify street intersection
  private static final String CORNER = "(:?\\band\\b|\\bat\\b|&|\\@)";
  
  //used to identify street units
  public static final String UNIT_SUFFIX =
    "#|p\\W*[om]\\W*b(?:ox)?|box|"+
    "BLDG|PH|HANGAR|BUILDING|FRONT|SIDE|NUM|UPPR|PENTHOUSE|STOP|SPC|ROOM|LOWR|NUMB|SLIP|OFC|LOWER|BASEME"+
    "NT|FRNT|OFFICE|OFIC|DEPARTMENT|TRAILER|PIER|BSMT|TRLR|SPACE|LBBY|STE|NO|UNIT|REAR|FL|RM|NUMBER|APT|HNGR"+
    "|FLOOR|DEPT|LOT|LOBBY|UPPER|SUITE|SUIT|APARTMENT|SLOT|F";
  
  private static final String ANYTEXT_BUT_NOT_CITY = "[^,]+?(?!(?:,|\\W+\\b(?:"+STATE+")\\b))";
  private static final String UNIT = 
    "(?:(?:(?:"+UNIT_SUFFIX+"|"+ANYTEXT_BUT_NOT_CITY+")\\W+|#\\W*)[\\w-]+)";

  //!!!WARNING!!! BECAREFUL WITH THE GROUPING, USE NON-CAPTURING GROUP ONLY => (?:...)
  //IF YOU ADD A CAPTURING GROUP, YOU NEED TO MODIFY ALL THE GROUP NUMBER REFERENCES IN 
  //THE ABOVE COMPONENT TO GROUP NUMBER MAP, 
  //AS WELL AS THE DOCUMENTATIONS BELOW TO REFLECT THE CHANGES. 
  
  //IF YOU DONT KNOW THE DIFFERENCES BETWEEN CAPTURING GROUP AND NON-CAPTURING GROUP, 
  //DONT EVEN TOUCH THIS CLASS
  //until java regex has named group capturing, this is what you need to deal with
  
  //# special case for addresses like 100 South Street
  private static final String STREET_OPTION1 = 
    "("+DIRECT+")\\W+"                              //GROUP 4
    + "("+TYPE+")\\b";                       //GROUP 5                

  private static final String STREET_OPTION2 =
  //optional <DIRECTION> before street name
    "(?:("+DIRECT+")\\W+)?"                         //GROUP 6 
    
  + "(?:" //<Street Name> <Street Type> <Direction>? like "Monkey Drive North" or "Monkey Drive"
      + "([^,]+)"  //this is the street name        //GROUP 7
      + "(?:[^\\w,]+("+TYPE+")\\b)"                 //GROUP 8
      + "(?:[^\\w,]+("+DIRECT+")\\b)?"            //GROUP 9
    + "|"
      + "([^,]*\\d+)"                              //GROUP 10
      + "(?:[^\\w,]+("+DIRECT+")\\b)"            //GROUP 11
    + "|" 
    //this is the no street type case, which is the most difficult case
    //right now this pattern will take the first word after the street num as the name
      + "([^,]+?)\\b"                                  //GROUP 12
      + "(?:[^\\w,]+("+TYPE+")\\b)?"        //GROUP 13
      + "(?:[^\\w,]+("+DIRECT+")\\b)?"            //GROUP 14
    + ")";
  
  private static final String STREET = 
    "(?:"+
      STREET_OPTION1 +
    "|"+
      STREET_OPTION2 +
    ")";
  
  public static final String PLACE = 
    "(?:" +
      "([^\\d,]+?)\\W+" +  //city                                              //GROUP 12
      "("+STATE+")\\W*" + //state                                             //GROUP 13
    ")?" +
    "("+ZIP+")?";      //zip                                                  //GROUP 14  

  public static final String STREET_ADDRESS = 
    "\\b(" + NUMBER + ")\\b\\s+" +                                          //GROUP 3
    "(?:" + FRACTION + "\\W*)?" +                                        
    "\\b(?:" + STREET + ")";                                              //GROUP 4 - 14                              

//  //FIXME: this pattern will NOT compiled under java 1.5
//  public static final Pattern ADDRESS_PATTERN = Pattern.compile(
////    "\\W*(?:"+NON_ADDRESS+")?"     +
//    "(?:(?!^\\d)([^,]+?)\\W*)?" +                                                         //GROUP 1
//    "(?:" +
//      "(" +                                         //address line 1 group //GROUP 2
//        STREET_ADDRESS +                            
//      ")" +
//    "\\W+)" +
//    //line 2 group, it's also the unit group
//    "(?:("+UNIT+")\\W+)?" +                                                     //GROUP 15
//    //last line group
//    "("+PLACE +")"+ "\\W*$", Pattern.CASE_INSENSITIVE);                         //GROUP 16
//  
  public static final Pattern CORNER_PATTERN = 
    Pattern.compile(CORNER, Pattern.CASE_INSENSITIVE);
  
  
  public static final Pattern NAME_NUMBER_SEP = Pattern.compile(
          "\\b(?:"+NUMBER+")\\s+",Pattern.CASE_INSENSITIVE);
  
  
  @SuppressWarnings("unchecked")
  public static final Pattern STREET_UNIT_SEP = Pattern.compile(
      "(?:" + 
        "[^\\d,]*?\\b(?:"+joinAll('|', new HashSet<String>(STREET_TYPE_MAP.values())) + ")\\s+" +
        "(?:"+TYPE.replaceFirst("\\|ST\\|", "|st"+ST_NEGATIVE_LOOKAHEAD+"|") +")"+
      "|" +//making the word character before street type optional because of 
        "(?:\\w)?\\s+" + //crazy street like "1606 PRAIRIE CENTER PKWY", "123 CENTER PARK RD"
        "(?:"+TYPE.replaceFirst("\\|ST\\|", "|st"+ST_NEGATIVE_LOOKAHEAD+"|") +")"+
      "|" +
        "(?:\\p{Alpha}+\\s+)?" +      
        "(?:"+SPECIAL_STREET_TYPE+")" +
      ")" +
      "\\b(?:[^\\w,]+(?:"+DIRECT+")\\b)?" +
      "(?:\\s*[,.])?"
        ,Pattern.CASE_INSENSITIVE);
  
  private static final Map<String, Integer> NAMED_GROUP_MAP = new HashMap<String, Integer>();
  private static final Pattern NAMED_GROUP_PATTERN = Pattern.compile("\\(\\?P<(.*?)>");
  private static final String NAMED_BACKREFERENCE_TEMPLATE = "\\(\\?P={0}\\)";
  private static String buildNamedGroupMap(String regex){
    Matcher m = NAMED_GROUP_PATTERN.matcher(regex);
    int i = 0;
    while(m.find()){
      i++;
      NAMED_GROUP_MAP.put(m.group(1), i);
    }
    String ret = m.replaceAll("(");
    for(Map.Entry<String, Integer> e : NAMED_GROUP_MAP.entrySet()){
      ret = ret.replaceAll(MessageFormat.format(NAMED_BACKREFERENCE_TEMPLATE, e.getKey()), "\\\\"+e.getValue());
    }
    return ret;
  }
  
  public static void main(String[] args) {
    System.out.println(Pattern.compile(buildNamedGroupMap("(?P<atoc>[a-c])x(?P=atoc)x(?P=atoc)")).matcher("axaxa").matches());
  }
  
//  public static final Pattern NON_ADDRESS_PATTERN = 
//    Pattern.compile(NON_ADDRESS, Pattern.CASE_INSENSITIVE);
  
  //helpers that basically adds some '.' characters to the directions
  private static Collection<String> transformDirection(){
    Collection<String> ret = 
      CollectionUtils.collect(DIRECTIONAL_MAP.values(), new Transformer(){
      public Object transform(Object arg0) {
        String s = (String)arg0;
        Matcher matcher = Pattern.compile("\\w").matcher(s);
        StringBuilder sb = new StringBuilder();
        while(matcher.find()){
          sb.append(matcher.group()+"\\.");
        }
        return sb.toString();
      }
    });
    Collection<String> ret2 = 
      CollectionUtils.collect(DIRECTIONAL_MAP.values(), new Transformer(){
      public Object transform(Object arg0) {
        String s = (String)arg0;
        Matcher matcher = Pattern.compile("\\w").matcher(s);
        StringBuilder sb = new StringBuilder();
        while(matcher.find()){
          sb.append(matcher.group()+" ");
        }
        return sb.toString();
      }
    });
    return CollectionUtils.union((Collection<String>)ret, (Collection<String>)ret2);
  }
  
}
