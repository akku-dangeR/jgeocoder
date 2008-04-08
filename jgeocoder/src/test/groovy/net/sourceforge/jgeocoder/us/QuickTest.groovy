package net.sourceforge.jgeocoder.us

import net.sourceforge.jgeocoder.us.RegexLibraryimport java.util.concurrent.ExecutorServiceimport java.util.concurrent.Executorsimport java.util.concurrent.Futureimport java.math.BigIntegerimport java.math.BigInteger
import java.util.regex.Patternimport java.util.regex.Matcherimport org.apache.commons.lang.StringUtils
import net.sourceforge.jgeocoder.us.AddressParserimport net.sourceforge.jgeocoder.us.AddressRegexLibraryimport net.sourceforge.jgeocoder.us.AddressRegexLibraryimport net.sourceforge.jgeocoder.us.AddressParserimport net.sourceforge.jgeocoder.us.Utilsimport net.sourceforge.jgeocoder.us.AddressParserimport org.apache.commons.lang.StringUtilsclass QuickTest extends GroovyTestCase {

	void testGroovy() {
	  def input = '2417 MUZZY DRIVE, New Castle, PA, 16101- 000'
	  def good = '2417 MUZZY DRIVE, New Castle, PA, 16101-0000'
	  println AddressParser.parseAddress(input) 
	  println AddressParser.parseAddress(good) 
	}
	
    void testParser2(){
      if(!new File('src/test/resources/test.txt').exists())
        return
      Writer w = new BufferedWriter(new OutputStreamWriter(
              new FileOutputStream(new File('src/test/resources/out.txt'))))
      new File('src/test/resources/test.txt').eachLine{
        if(StringUtils.isNotBlank(it)){
          def map = AddressParser.parseAddress(it)
          def lst = []
            w.writeLine(it)
            if(map){
              lst<<map.get(AddressParser.AddressComponent.name)
              lst<<map.get(AddressParser.AddressComponent.number)
              lst<<map.get(AddressParser.AddressComponent.predir)
              lst<<map.get(AddressParser.AddressComponent.street)
              lst<<map.get(AddressParser.AddressComponent.type)
              lst<<map.get(AddressParser.AddressComponent.postdir)
              lst<<map.get(AddressParser.AddressComponent.line2)
              lst<<map.get(AddressParser.AddressComponent.city)
              lst<<map.get(AddressParser.AddressComponent.state)
              lst<<map.get(AddressParser.AddressComponent.zip)
            }
            w.writeLine(lst.toString())
            
        }
      }
      w.close()
    }
}
