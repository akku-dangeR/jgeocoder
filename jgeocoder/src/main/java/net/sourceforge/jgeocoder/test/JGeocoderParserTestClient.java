package net.sourceforge.jgeocoder.test;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

import net.sourceforge.jgeocoder.AddressComponent;
import net.sourceforge.jgeocoder.tiger.JGeocoder;
import net.sourceforge.jgeocoder.us.AddressParser;
import net.sourceforge.jgeocoder.us.AddressStandardizer;


public class JGeocoderParserTestClient {
  public static void main(String[] args) throws Exception{
    String in;
    JGeocoder jg = new JGeocoder();
    if(args.length != 0){
      in = args[0];
      System.out.println("Parsing input...");
      Map<AddressComponent, String> m  = AddressParser.parseAddress(in);
      if(m == null){
        System.out.println("Unable to parse input, please try again");
        return;
      }
      printMap(m);
      
      System.out.println("Normalizing parsed address...");
      m = AddressStandardizer.normalizeParsedAddress(m);
      printMap(m);
      System.out.println("Geocoding normalized address (Only zip and {city, state} geocoding are supported currently)...");
      printMap(jg.geocode(m));
      jg.cleanup();
      return;
    }
    
    System.out.println("Input raw address as a single line");
    System.out.println("Enter blank line to end session");
    System.out.println();
    
    BufferedReader inbuf = new BufferedReader(new InputStreamReader(System.in));
    
    while ((in = inbuf.readLine()) != null && in.length() != 0){
      System.out.println("Parsing input...");
      Map<AddressComponent, String> m  = AddressParser.parseAddress(in);
      if(m == null){
        System.out.println("Unable to parse input, please try again");
        continue;
      }
      printMap(m);
      
      System.out.println("Normalizing parsed address...");
      m = AddressStandardizer.normalizeParsedAddress(m);
      printMap(m);
      System.out.println("Geocoding normalized address (Only zip and {city, state} geocoding are supported currently)...");
      printMap(jg.geocode(m));
    }
    jg.cleanup();
  }
  
  private static void printMap(Map<AddressComponent, String> m){
    System.out.println();
    System.out.println("Address: "+AddressStandardizer.toSingleLine(m));
    for(Map.Entry<AddressComponent, String> e : m.entrySet()){
      System.out.println(e.getKey()+" : "+e.getValue());
    }
    System.out.println();
  }
}
