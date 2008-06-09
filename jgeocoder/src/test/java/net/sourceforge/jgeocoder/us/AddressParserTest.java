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
}
