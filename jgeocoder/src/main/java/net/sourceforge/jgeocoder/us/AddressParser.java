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

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.jgeocoder.AddressComponent;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

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
  private static final Pattern CLEANUP = Pattern.compile("^\\W+|\\W+$|[\\s\\p{Punct}&&[^\\)\\(#&,:;@'`-]]");
  private static final Pattern STREET_TYPES = Pattern.compile(RegexLibrary.STREET_DESIGNATOR);
  private static final Pattern STATES = Pattern.compile(RegexLibrary.US_STATES);
  
  private static String getCleanSttring(String rawAddr){
    return CLEANUP.matcher(rawAddr).replaceAll(" ").replaceAll("\\s+", " ").trim();
  }
  /**
   * Parses a raw address string 
   * @param rawAddr
   * @param autoCorrectStateSpelling swith on/off auto correction on state mis-spelling
   * @return a map of parsed address components
   */
  private static Map<AddressComponent, String> jGeocodeParseAddress(String rawAddr, boolean autoCorrectStateSpelling){
    rawAddr = getCleanSttring(rawAddr);
    if(autoCorrectStateSpelling){
      rawAddr = SpellingCorrector.correctStateSpelling(rawAddr);
    }
    Matcher m = STREET_ADDRESS.matcher(rawAddr);
    Map<AddressComponent, String> ret = null;
    if(m.matches()){
      ret = getAddrMap(m, P_STREET_ADDRESS.getNamedGroupMap());
      postProcess(ret);
      String splitRawAddr = null;
      String line12sep = ret.get(AddressComponent.TLID);//HACK!
      if(!line12sep.contains(",") 
          && (splitRawAddr = designatorConfusingCitiesCorrection(ret, rawAddr))!=null){
        m = STREET_ADDRESS.matcher(splitRawAddr);
        if(m.matches()){
          ret = getAddrMap(m, P_STREET_ADDRESS.getNamedGroupMap());
          ret.remove(AddressComponent.TLID);//HACK!
          return ret;
        }
      }
      ret.remove(AddressComponent.TLID);//HACK!
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
  /**
   * Parses a raw address string, this delegates to {@linkplain AddressParser#parseAddress(String, boolean)} with autoCorrectStateSpelling set to false
   * @param rawAddr
   * @return a map of parsed address components
   */
  public static Map<AddressComponent, String> parseAddress(String rawAddr){
    return parseAddress(rawAddr, true);
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
    Map<AddressComponent, String> ret = new EnumMap<AddressComponent, String>(AddressComponent.class);
    for(int i=1; i<= m.groupCount(); i++){
      String name = groupMap.get(i);
      AddressComponent comp = valueOf(name);
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
  private static String designatorConfusingCitiesCorrection(Map<AddressComponent, String> parsedLocation, String input){
    String street = parsedLocation.get(AddressComponent.STREET);
    String type = parsedLocation.get(AddressComponent.TYPE);
    String line2 = parsedLocation.get(AddressComponent.LINE2);
    if(street == null || type == null || line2 != null || street.split(" ").length < 2){ return null;}
	  Matcher m = STREET_DESIGNATOR_CHECK.matcher(street);
	  if(m.find()){
		  String parsedstate = parsedLocation.get(AddressComponent.STATE);
		  if(parsedstate == null){
			  String parsedcity = parsedLocation.get(AddressComponent.CITY);
			  if(parsedcity != null && parsedcity.length() == 2){
				  parsedstate = parsedcity;
			  }
		  }
		  String normalizedState = AddressStandardizer.normalizeState(StringUtils.upperCase(parsedstate));
		  String inputUpper =  input.toUpperCase();
		  String ret = null;
		  Set<String> stateSet = new HashSet<String>();
		  if(normalizedState != null){
			  stateSet.add(normalizedState);
		  }else{ //if no state in put, this needs to work much harder
			  stateSet.addAll(SpecialData.C_MAP.keySet());
		  }
		  int stateIdx = parsedstate == null ? input.length() : input.lastIndexOf(parsedstate);
		  for(String state : stateSet){
		      for(String s : SpecialData.C_MAP.get(state)){
			        int idx = -1;
			        if((idx =inputUpper.lastIndexOf(s))!=-1){ //and the input has one of the city names that can confuse the parser
			          //this almost guaranteed to break the parser, help the parser by putting a comma separator before the city
			        	if(idx+s.length() >= stateIdx -2){
			        		return input.substring(0, idx)+","+input.substring(idx);
			        	}
			        }
			      }
		  }
	      return ret;
	  }		
  return null;
    
  }
  
//code used to replace hashcodes so they don't get removed by the address parser
	private static final String HASHCODE_VALUE = "48914631374";
	
	
	/**
	 * This is the main method that calls all the other method
	 * to successfully split up and address into separate fields.
	 * 
	 * Returns a string key,value map containing address parts
	 * along with their string values.
	 * 
	 * @param streetNumber
	 * @param streetDirection
	 * @param streetName
	 * @param streetType
	 * @param unitNumber
	 * @param city
	 * @param state
	 * @return
	 */
	public static Map<AddressComponent, String> parseAddress(String address, boolean autoCorrectStateSpelling)
	{
		
		/*First we replace individual fields with temporary escaped value. 
		 * The reason we do this separately is because we can
		 * strip out certain characters in the split up fields more accurately than
		 * in the constructed address (commas for instance can easily be stripped out of
		 * the split up fields, but are important to maintain in the address) 
		*/
		
		// There are some words that we want to remove / permanently change in the address (undefined / undefd)
		Map<String, String> replacementStrings = getReplacementStrings();
		address = escapeString(address, replacementStrings);
		
		// create the map of regexp string to temporarily replace with fillers, since the addressparser doesn't like them
		HashBiMap<String, String> constStringMap = setupConstStringMap();
		
		// create the map of regexp string to temporarily replace with fillers, since the addressparser doesn't like them
		HashBiMap<String, String> regexpStringMap = formatRegexpStringMap();
		
		// create the map of regexp strings that we explicitly do not want to escape
		HashBiMap<String, String> doNotEscapeRegexpStringMap = formatdoNotEscapeRegexpStringMap();
		
		// Store the strings that we do not want to escape
		HashBiMap<String, String> doNotEscapeFilledStringMap = getMatchingStrings(address, doNotEscapeRegexpStringMap);
		
		// temporarily move the parts of the string that we explicitly do not want to escape
		address = escapeString(address, doNotEscapeRegexpStringMap);
		
		// Store the strings that match the regular expression fillers in the map for replacement later
		HashBiMap<String, String> filledRegexpStringMap = getMatchingStrings(address, regexpStringMap);
		
		// replace the strings that match within the address with fillers
		address = escapeString(address, filledRegexpStringMap);
		address = escapeString(address, constStringMap);
		
		// Put the strings we do not want to escape back in
		address = escapeString(address, doNotEscapeFilledStringMap.inverse());
		
		// parse the address into separate fields
		Map<AddressComponent, String> results = prepareAddressForParsingAndParse(address, autoCorrectStateSpelling);
		
		if (results == null)
		{
			// there was a problem parsing the address
			return null;
		}
		
		// Replace the fillers in the split up fields with the original values (requires an inverse)
		results = replaceOriginalStringsInSplitUpFields(results, filledRegexpStringMap.inverse());
		results = replaceOriginalStringsInSplitUpFields(results, constStringMap.inverse());
		
		return results;
	}

	/**
	 * Returns a map containing strings that we want to permanently
	 * replace within the address.
	 * 
	 * Note -- since this map reuses the code in escapeString(),
	 * the replacement keys are CaSe SENsitIVE
	 * @return
	 */
	private static Map<String, String> getReplacementStrings() {
		Map<String, String> replacementMap = new HashMap<String,String>();
		replacementMap.put("Undefined", "TBD"); // replace Undefined with TBD
		replacementMap.put("undefined", "TBD"); // replace undefined with TBD
		replacementMap.put("UNDEFINED", "TBD"); // replace undefined with TBD
		replacementMap.put(" Undefd", ""); // remove Undefd
		replacementMap.put(" undefd", ""); // remove undefd
		replacementMap.put("County Road", "CR"); // Change County Road to CR
		replacementMap.put("county road", "CR");
		return replacementMap;
	}

	/**
	 * This method takes a map of Strings that need to
	 * be replaced in the address, along with the fillvalues/escape values
	 * they will temporarily be replaced with. The strings
	 * have to be replaced before the addressparser looks
	 * at them because it gets confused on these strings.
	 * 
	 * Next, the map is iterated through and all instances
	 * of the strings in address are replaced with their 
	 * fill values.
	 * 
	 * @param stringToEscape
	 * @param codeMaps
	 * @return
	 */
	private static String escapeString(String stringToEscape, Map<String, String> codeMaps )
	{
		if (stringToEscape == null)
		{
			return stringToEscape;
		}
		for(String key : codeMaps.keySet())
		{
			// temporarily substitute strings that the address parser gets confused on
			stringToEscape = stringToEscape.replaceAll(key, codeMaps.get(key));
		}
		return stringToEscape;
	}
	
	/**
	 * This method takes a map containing fill values that were
	 * temporarily inserted into the address, mapped to
	 * the strings that they originally replaced. Next, this
	 * method iterates through the split up street fields and
	 * replaces instances of the fillvalues with their originals.
	 * 
	 * @param results
	 * @param codeMaps
	 * @return
	 */
	
	private static Map<AddressComponent, String> replaceOriginalStringsInSplitUpFields(Map<AddressComponent, String> results, BiMap<String, String> codeMaps )
	{
		for(String oldString : codeMaps.keySet()) // contains a map of the form <stringToBeReplaced><StringToReplaceWith>
		{
			
			for (AddressComponent fieldKey : results.keySet())
			{
				if (results.get(fieldKey) == null) // only update fields that are not null
				{
					continue;
				}
				else
				{
					String originalString = results.get(fieldKey);
					String updatedString = originalString.replaceAll(oldString, codeMaps.get(oldString));
					results.put(fieldKey, updatedString);
				}
			}
		}
		return results;
	}
	
	/**
	 * This method stores the strings that match regular
	 * expressions in the regular expression map and
	 * returns a map containing the strings as well as
	 * the fill values they are being replaced with.
	 * 
	 * @param address
	 * @param regexpMap
	 * @return
	 */
	private static HashBiMap<String, String> getMatchingStrings(String address, Map<String, String> regexpMap )
	{
		HashBiMap<String, String> matchingStrings = HashBiMap.create();
		for(String key : regexpMap.keySet())
		{
			Pattern p = Pattern.compile(key);
			Matcher m = p.matcher(address);
			boolean matching = m.find();
			
			if (matching)
			{
				String from = m.group(1);
				String to = regexpMap.get(key);
				matchingStrings.put(from, to);
			}
			/*if (m.find())
			{
				// If there are two of these regex patterns in the address this may cause an error,
				// as only one will be replaced by the filler string
				
				logger.debug("~~~Warning~~~ -- multiple matches found for regexp: " + key + "\n" +
						"On address: " + address);
			}*/
		}
		return matchingStrings;
	}
	
	/**
	 * Setup String fill value bimap. That is, a bidirectional
	 * map of Strings that we use to temporarily insert
	 * fill values into the address so that the address parser
	 * doesn't get confused.
	 * @return
	 */
	private static HashBiMap<String, String> setupConstStringMap()
	{
		// bidi map to hold string to string mappings
		HashBiMap<String, String> constStrings = HashBiMap.create();
		
		// Replaces slashes with a temporary string replacement, since the addressparser doesn't get along with them
		constStrings.put("/", "21421161");
		
		// The address parser gets confused with TBD as an street number, so we replace it with a temporary number
		constStrings.put("TBD", "112521521");
		constStrings.put("Tbd", "4654231");
		constStrings.put("TBD", "9784261");
		
		// The address parser sometimes removes hashes
		constStrings.put("#", HASHCODE_VALUE);
		
		constStrings.put("Us Hwy", "Hwy Us28409182");
		constStrings.put("US HWY", "Hwy Us8123754741");
		return constStrings;
	}
	
	/**
	 * Setup RegularExpression fill value bimap. That is, a bidirectional
	 * map of regular expressions that we use to temporarily insert
	 * fill values into the address so that the address parser
	 * doesn't get confused.
	 * @return
	 */
	private static HashBiMap<String, String> formatRegexpStringMap()
	{
		// bidi map to hold string to regular expression mappings
		// Notice the parantheses around the part that we are temporarily replacing (group) -- these normally do no include surrounding spaces
		HashBiMap<String, String> regexpStrings = HashBiMap.create();
		
		// remove letter number/number from this address as in "2533 G 3/8 Road"
		regexpStrings.put(" ([a-zA-Z] \\d[\\/-]\\d)", "regexpCode1");
		
		// remove interstate from this address as in "2880, I-70 Business Loop"
		regexpStrings.put(" ([iI]-\\d+) ", "regExpCode2");
		
		
		// remove periods from the middle of words / numbers
		regexpStrings.put("\\s?(\\s*#?\\w+(\\.\\w+)+)", "522597205");
		
		// remove commas from the middle of words / numbers
		regexpStrings.put("\\s?(\\s*#?\\w+(,\\w+)+)", "784561789");
		
		// "(Fp-f-1)- 68-1371 Kinzel Place"
		// remove dashes from the middle of words / numbers
		regexpStrings.put("\\s?((\\-?\\s*#?(\\w+\\-\\w*)|(\\w*\\-\\w+))+)", "189237654");
		
		//555 Rivergate Lane #B1-104
		//Wheeler Circle 314D-6
		
		/*
		"(Fp-f-1- 68-1371 Kinzel Place"
		"abc-abc abc-abc"
		"abc- abc-"
		"-abc -abc"*/
		return regexpStrings;
	}
	
	/**
	 * Generates a bi-map of regular reg expressions that we don't want to escape
	 * 
	 * @return
	 */
	private static HashBiMap<String, String> formatdoNotEscapeRegexpStringMap()
	{
		HashBiMap<String, String> savedRegexpStrings = HashBiMap.create();
		
		// We don't want to escape dashes in zip codes
		savedRegexpStrings.put("(\\d{5}\\-\\d{4})", "zipCodeRegExp");
		
		return savedRegexpStrings;
	}
	
	/**
	 * Passes the address to the jgeocoder address parser and
	 * formats the results into a string map. Also handles
	 * the strange case of highways -- that is, street type
	 * PRECEDING street_number.
	 * @param address
	 * @return
	 * @throws AddressParsingException 
	 */
	private static Map<AddressComponent, String> prepareAddressForParsingAndParse(String address, boolean autoCorrectSpelling)
	{
		Map<AddressComponent, String> results = jGeocodeParseAddress(address, autoCorrectSpelling);
		if( results == null)
		{
			return null; // Something went wrong in the process of parsing the address
		}

		String splitStreetNumber = results.get(net.sourceforge.jgeocoder.AddressComponent.NUMBER);
		String splitStreetDir = results.get(net.sourceforge.jgeocoder.AddressComponent.PREDIR);
		String splitStreetName = results.get(net.sourceforge.jgeocoder.AddressComponent.STREET);
		String splitStreetType = results.get(net.sourceforge.jgeocoder.AddressComponent.TYPE);
		String splitUnitNumber = results.get(net.sourceforge.jgeocoder.AddressComponent.LINE2);
		
		Collection<String> streetDirs = getStreetDirs();
		
		/** if the street name is a direction then chances
		 * are that the street direction was pulled into
		 * the street name incorrectly and the street name
		 * is in the unit number (following conditional)
		 */
		if (splitStreetName != null && streetDirs.contains(splitStreetName.toLowerCase()) && splitStreetDir == null)
		{
			splitStreetDir = splitStreetName;
			splitStreetName = null;
		}
		
		// We need to do some switching if the street name is null (usually a result of Hwy 19 or Road 5)
		// -- Second case --> Or if street type is null and unit number is non null we assume that part of the street
		// name got pulled into the unit number
		if ((splitStreetName == null && splitStreetType != null && splitUnitNumber != null) || 
				(splitStreetType == null && splitUnitNumber != null)) 
		{
			if (splitStreetName == null)
			{
				splitStreetName = "";
			}
			
			// if we have a street type then we are going to want to pull in 
			// the first part of the unit number into street name (e.g. hwy 19 or road 52)
			if (splitStreetType != null)
			{
				Pattern p1 = Pattern.compile("^\\s?(\\S+)\\s?");
				Matcher m1 = p1.matcher(splitUnitNumber);
				
				if(m1.find())
				{
					String streetTypePart = m1.group(1);
					splitStreetName = createStreetName(splitStreetName, splitStreetType, streetTypePart);
					splitUnitNumber = splitUnitNumber.substring(m1.end(0));
					splitStreetType = null; // since we pulled in the street type we don't want to pull it in again if the next matcher matches
				}
			}
			
			// Get the indexes of unit or a hash mark as clues for where to split
			int unitIndex = splitUnitNumber.toLowerCase().indexOf("unit");
			int hashIndex = splitUnitNumber.indexOf(HASHCODE_VALUE);
			
			
			// Part of the unit number to be removed and added to street name
			String partOfStreetName = "";
			// We want to leave the part of the unit number that has a hash mark or the string "unit"
			// in the unit number
			if (unitIndex !=-1 || hashIndex != -1)
			{
				if (unitIndex !=-1)
				{
					
					if (unitIndex == 0) // Unit number starts with a # so the whole unit number stays as the unit number
					{
						partOfStreetName = "";
					}
					else
					{
						partOfStreetName = splitUnitNumber.substring(0, unitIndex-1);
					}
					splitUnitNumber = splitUnitNumber.substring(unitIndex);
				}
				else
				{
					if (hashIndex == 0) // Unit number starts with a # so the whole unit number stays as the unit number
					{
						partOfStreetName = "";
					}
					else
					{
						partOfStreetName = splitUnitNumber.substring(0, hashIndex-1);
					}
					splitUnitNumber = splitUnitNumber.substring(hashIndex);
				}
				splitStreetName = createStreetName(splitStreetName,
						splitStreetType, partOfStreetName);
			}
			else // We can't find any clues regarding what part of the unit number is actually the unit number
			{
				// Pattern to get the last string so we can look at it to see if it looks like a unit number
				// We assume that any string less than 3 characters or that is a number is a unit number
				Pattern p2 = Pattern.compile("\\s?(\\S+)$");
				Matcher m2 = p2.matcher(splitUnitNumber);
				
				if (m2.find())
				{
					// the last word in the unit number is what we look at to try to guess
					// whether it is a unit number
					String lastWord = m2.group(1);
					if (lastWord.length()<3 || isNumeric(lastWord))
					{
						partOfStreetName = splitUnitNumber.substring(0, m2.start(0));
						splitStreetName = createStreetName(splitStreetName,
								splitStreetType, partOfStreetName);
						splitUnitNumber = lastWord;
					}
					else
					{
						splitStreetName += " " + splitUnitNumber;
				    	splitUnitNumber = null;
					}
				}
			}
			// Either streetType was pulled into streetName or it started as null so it should be null
			splitStreetType = null;
			
			// set splitUnitNumber to null if it's empty
			if (splitUnitNumber != null && splitUnitNumber.equals(""))
			{
				splitUnitNumber = null;
			}
		}
		
		
		
		results.put(AddressComponent.NUMBER, splitStreetNumber);
		results.put(AddressComponent.PREDIR, splitStreetDir);
		results.put(AddressComponent.STREET, splitStreetName);
		results.put(AddressComponent.TYPE, splitStreetType);
		results.put(AddressComponent.LINE2, splitUnitNumber);
		
		return results;
	}
	
	/**
	 * Creates the street name from the summation of the original
	 * streetName + streetType (if non null) + part of the street
	 * name split off from the unit number
	 * 
	 * @param splitStreetName
	 * @param splitStreetType
	 * @param partOfStreetName
	 * @return
	 */
	private static String createStreetName(String splitStreetName,
			String splitStreetType, String partOfStreetName) {
		
		String addSpace = " ";
		String addPartSpace = " ";
		if (splitStreetName.equals("") || splitStreetType == null)
		{
			addSpace = ""; // don't add a space if the streetNumber is null
		}
		
		if (partOfStreetName.equals(""))
		{
			addPartSpace = ""; // don't add a space if partOfStreetName is null or if we don't have a street type
		}
		
		if (splitStreetType == null)
		{
			splitStreetName += addPartSpace + partOfStreetName;
		}
		else
		{
			splitStreetName += addSpace + splitStreetType + addPartSpace + partOfStreetName;
		}
		return splitStreetName;
	}

	private static Collection<String> getStreetDirs() {
		
		Collection<String> streetDirs = new ArrayList<String>();
		streetDirs.add("e");
		streetDirs.add("w");
		streetDirs.add("n");
		streetDirs.add("s");
		streetDirs.add("sw");
		streetDirs.add("nw");
		streetDirs.add("se");
		streetDirs.add("ne");

		return streetDirs;
	}
	
	private static boolean isNumeric(String str)
	{
	    for (char c : str.toCharArray())
	    {
	        if (!Character.isDigit(c)) return false;
	    }
	    return true;
	}
}