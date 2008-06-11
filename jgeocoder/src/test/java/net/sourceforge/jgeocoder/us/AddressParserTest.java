package net.sourceforge.jgeocoder.us;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import net.sourceforge.jgeocoder.AddressComponent;

public class AddressParserTest
{

   @org.junit.Test
   public void testParseAddress()
   {
      String addr1 = "123 Avenue of art, philadelphia pa 12345";
      Map<AddressComponent, String> addressComponents = AddressParser.parseAddress(addr1);
      System.out.println("addressComponents: " + addressComponents);

      assertEquals("12345", addressComponents.get(AddressComponent.ZIP));
      assertEquals("philadelphia", addressComponents.get(AddressComponent.CITY));
      assertEquals("pa", addressComponents.get(AddressComponent.STATE));
      assertEquals("123", addressComponents.get(AddressComponent.NUMBER));
   }
   
   @org.junit.Test
   public void saintNameExpansionTest(){
     String addr1 = "St. louis Missouri";
     Map<AddressComponent, String> m = AddressStandardizer.normalizeParsedAddress(AddressParser.parseAddress(addr1));
     assertEquals("SAINT LOUIS", m.get(AddressComponent.CITY));
     assertEquals("MO", m.get(AddressComponent.STATE));
     addr1 = "123 St peters ave, St. louis Missouri";
     m = AddressStandardizer.normalizeParsedAddress(AddressParser.parseAddress(addr1));
     assertEquals("SAINT LOUIS", m.get(AddressComponent.CITY));
     assertEquals("SAINT PETERS", m.get(AddressComponent.STREET));
     assertEquals("MO", m.get(AddressComponent.STATE));
   }
   @org.junit.Test
   public void testDesignatorConfusingCitiesParsing(){
     String addr1 = "123 main street St. louis Missouri";
     Map<AddressComponent, String> m = AddressStandardizer.normalizeParsedAddress(AddressParser.parseAddress(addr1));
     assertEquals("123", m.get(AddressComponent.NUMBER));
     assertEquals("MAIN", m.get(AddressComponent.STREET));
     assertEquals("ST", m.get(AddressComponent.TYPE));
     assertEquals("SAINT LOUIS", m.get(AddressComponent.CITY));
     assertEquals("MO", m.get(AddressComponent.STATE));
     addr1 = "123 south lake park  Fort Duchesne Utah";
     m = AddressStandardizer.normalizeParsedAddress(AddressParser.parseAddress(addr1));
     assertEquals("FORT DUCHESNE", m.get(AddressComponent.CITY));
     assertEquals("LAKE", m.get(AddressComponent.STREET));
     assertEquals("PARK", m.get(AddressComponent.TYPE));
     assertEquals("UT", m.get(AddressComponent.STATE));
   }
}
