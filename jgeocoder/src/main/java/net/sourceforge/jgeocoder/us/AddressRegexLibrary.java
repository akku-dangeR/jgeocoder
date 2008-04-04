package net.sourceforge.jgeocoder.us;

import static net.sourceforge.jgeocoder.us.RegexLibrary.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class AddressRegexLibrary{
  
  private static final String NUMBER =
    "(?:\\p{Alpha})?\\d+(?:[- ][\\p{Alpha}&&[^NSEW]]" +
    "(?!\\s+(?:st|street|ave|aven|avenu|avenue|blvd|boulv|boulevard|boulv|plz|plaza|plza)))?" +
    "|\\d+-?\\d*(?:-?\\p{Alpha})?|"+TXT_NUM_0_9; 
  
  private static final String FRACTION = "\\d+\\/\\d+";
  
  private static final String LINE1A = 
    "(?P<street-A>"+DIRECTIONS+")\\W+" + 
    "(?P<type-A>"+STREET_DESIGNATOR+")\\b";
  
  private static final String LINE1B = 
    "(?:(?P<pre-dir>"+DIRECTIONS+")\\W+)?" +
    "(?:" +
      "(?P<street-B>[^,]+)" +
      "(?:[^\\w,]+(?P<type-B>"+STREET_DESIGNATOR+")\\b)" +
      "(?:[^\\w,]+(?P<post-dir>"+DIRECTIONS+")\\b)?" +
     "|" +
       "(?P<street-B2>[^,]*\\d)" +
       "(?:(?P<post-dir2>"+DIRECTIONS+")\\b)" +
     "|" +
       "(?P<street-B3>[^,]+?)" +
       "(?:[^\\w,]+(?P<type-B2>"+STREET_DESIGNATOR+")\\b)?" +
       "(?:[^\\w,]+(?P<post-dir3>"+DIRECTIONS+")\\b)?" +       
    ")";
  
  private static final String LINE1 =
    "(?P<number>(?:" + NUMBER + ")(?:\\W+"+FRACTION+")?)\\W+" + 
    "(?:" + LINE1A + "|" + LINE1B + ")";
  
  //A, 2A, 22, A2, 2-a, 2/a, etc...
  private static final String UNIT_NUMBER = 
    "(?:\\b\\p{Alpha}{1}\\s+|\\p{Alpha}*[-/]?)?" +
    "(?:\\d+|\\b\\p{Alpha}\\b(?=\\s|$))" +
    "(?:[ ]*\\p{Alpha}\\b|-\\w+)?";
  private static final String LINE2A = "\\b(?:"+ADDR_UNIT+")\\W*?(?:"+UNIT_NUMBER+")";
  private static final String LINE2B = "\\b(?:(?:"+TXT_ORDINAL_0_99+"|"+ORDINAL_ALL+")\\W*(?:"+ADDR_UNIT+"))\\b";
  private static final String LINE2 = "(?:(?P<line2>"+LINE2A+"|"+LINE2B+")\\W+)?";
  
  private static final String ZIP = "\\d{5}(?:[- ]\\d{4})?";
  private static final String LASTLINE = 
    "(?:" +
      "(?:(?P<city>[^\\d,]+?)\\W+)?" +  //city                                              
      "(?P<state>"+US_STATES+")\\W*" + //state                                          
    ")?" +
    "(?P<zip>"+ZIP+")?";      //zip
  
  
  private static final String STREET_ADDRESS = 
    "(?:(?P<name>[^,]+)\\W+)??" + LINE1 + "\\W+"+ LINE2 + LASTLINE +"\\W*";
  
  public static void main(String[] args) {
    NamedGroupPattern p = compile(STREET_ADDRESS);
    Pattern pattern = Pattern.compile(p.getRegex(), Pattern.CASE_INSENSITIVE);
    Matcher m = pattern.matcher("attention blah blah 123, 1443 United States Highway 22,  NJ 07090");
    m.matches();
    for(int i =1; i<= m.groupCount(); i++){
      System.out.println(m.group(i));
    }
  }
  
  private static final Pattern NAMED_GROUP_PATTERN = Pattern.compile("\\(\\?P<(.*?)>");
  //assumes all capturing groups are named
  private static NamedGroupPattern compile(String regex){
    Matcher m = NAMED_GROUP_PATTERN.matcher(regex);
    Map<String, Integer> namedGroupMap = new HashMap<String, Integer>();
    int i =1;
    while(m.matches()){
      namedGroupMap.put(m.group(1), i);
      i++;
    }
    return new NamedGroupPattern(m.replaceAll("("), namedGroupMap);
  }
  
  private static class NamedGroupPattern {
    private final String _regex;
    private final Map<String, Integer> _namedGroupMap;
    public NamedGroupPattern(String regex, Map<String, Integer> namedGroupMap) {
      _regex = regex;
      _namedGroupMap = namedGroupMap;
    }
    public String getRegex() {
      return _regex;
    }
    public Map<String, Integer> getNamedGroupMap() {
      return _namedGroupMap;
    }
  }
  
}