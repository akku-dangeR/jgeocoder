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

  def bad ="""
PSC-9, BOX 2169, APO, AE, 09123
FIRST FEDERAL PLAZA, SUITE 204, NEW CASTLE, PA, 16101
C/O DR ABBOS J ALI, DEPT OF MANAGEMENT, UNIVERSITY OF SHARJAH, PO BOX 2722
 81Pennington Court, Delanco, NJ, 08075
33-1 SOI PRASARTSUK, YENAKAT ROAD, BANGKOK 10120
5959 CHERRYWOOD PL, MISSISSAUGA, ONTARIO
PINDAROU 16, 10673 ATHENS
FOXWOOD APARTMENTS, KENT HALL APARTMENT 11, Newark, DE, 19711-5926
N7384 HIGH PRAIRIE LANE, New Glarus, WI, 53574- 000
BERWYN BAPTIST ROAD, ARBORDEAU APARTMENTS, GEVREY 18, DEVON, PA, 19333
C/O LUCILLE MAFFEI, P O BOX 97, BIRCHRUNVL, PA, 19421-0097
5980Richmond Hwy, APT 909, Alexandria, VA, 22303
78 EASTGLEN CRES, TORONTO
N102W15865 FLINTLOCK TRAIL, Germantown, WI, 53022
1721 HICKORY RIDGE DRIVE, Waxhaw, NC, 28173- 000
RR1, BOX 60A, Youngsville, PA, 16371
KOERBELS HILL, JEANNETTE, PA, 15644
FOXCROFT SQUARE APTS, APT 205, JENKINTOWN, PA, 19046
1601 Cherry St, ste 1900n, ExcelleRx, Inc, Philadelphia
UNIVERSITY OF PITTSBURGH, SCHOOL OF PHARMACY, 1106 SALK HALL, PITTSBURGH, PA, 15261
1581Hollywood Ave, LANGHORNE, PA, 19047
100 SECOND STREET, NE, #A380, Minneapolis
131COLUMBUS CIRCLE, Clarks Summit, PA, 18411
1I4 DEERFIELD ROAD, LIGONIER, PA, 15658
161Newport Way, Huntsville, TX, 77320
64 CHELFORD ROAD
LANDMARK APARTMENTS, BLDG 2 APARTMENT 1011, 1920 FRONTAGE ROAD, CHERRY HILL, NJ, 08034
Fairway Coat, Blairsville, PA, 15717
800 COTTMAN AVENUE, APARTMENT 256, BUILDING B-1, Philadelphia, PA, 19111-3056
UNIVERSITY OF NORTH CAROLINA, DIVISION OF NEPHROLOGY CB 7155, 348 MCNIDEN BLDG, Chapel Hill, NC, 27599-7155
THE LAKEWOOD COURTYARD, APARTMENT 219, 52 MADISON AVENUE, Lakewood, NJ, 08701
DEPT OF CLINICAL PHARMACY, UNIVERSITY OF TENNESSEE, 910 MADISON SUITE 327, 26 S. DUNLAP ST 202 FEURT, MEMPHIS, TN
N51W17175 MAPLE CREST, MENOMONEE FALLS, WI, 53051
20 BURBANK COURT, MONCTON NB E1C 8Y7,  EIC8Y7
WALSH ROAD, PITTSBURGH, PA, 15205
rd1 box222, latrobe, PA, 15650
22 VENDOME, KIRKLAND, QUEBEC   H9J 3W4
C/O DENISE MCCARTHY, P O BOX 186, BALA CYNWYD, PA, 19004
2 FORSTER AVENUE, THOROLD ONTARIO  L2V4H4
2960 RUE DES CHENES, APT 102, SHERBROOKE QC J1L-1Y1
RR1, BOX 1347, Gouldsboro, PA, 18424
RR4, BOX888, ALTOONA, PA
RR3, BOX 192M, Dallas, PA
C/O HENRY KATRA, PO BOX 217, 2734 FLOWING SPRINGS RD, BIRCHRUNVILLE, PA, 19421
RR7, BOX 7386, MOSCOW, PA
308 SALERNO STREET
W73N923 LONDON COURT, CEDARBURG, WI, 53012
RR1, BOX 371, PAXINOS, PA, 17860
LRMC, CMR 402 BOX 1188, APO, AE, 09180
HEALTHCARE PHARMACY, MAIN STREET - BROUMMANA
3225 CHEMIN DU LAC, VAL D'OR, QUEBEC(PQ)  J9P 6G3
58 FALKIRK DRIVE, HAMILTON, ONTARIO L9B 1S4
3001 Veazey Terrace, NW, Unit 1005, Washington, D.C.
P O BOX 4328, CURACAO, ANTILLES, Netherlands Antilles
138 OLIVER AVENUE, SELKIRK MANITOBA, R1A OC3
DEPT OF PHARMACY, NATL UNIV OF SINGAPORE, 18 SCIENCE DRIVE 4, SINGAPORE 117543
LAFAYETTE AT VALLEY FORGE, APT A610, 901 PARKVIEW DRIVE, King of Prussia, PA
7720 STENTON AVENUE, BUILDING A, APT 303, Philadelphia, PA
APDO 71-61000, CIUDAD COLON 6100
5222 AUTUMN HARVEST WAY, BURLINGTON ONTARIO, L7I 7J5, CANADA, 00000
SULLIVAN COUNTY MEDICAL CTR PHARMACY, P O BOX V, Laporte, PA, 18626
8202HARPERS CROSSING, Langhorne, PA, 19047
2417 MUZZY DRIVE, New Castle, PA, 16101- 000
HORTY, SPRINGER & MATTERN, 4614 FIFTH AVE, PITTSBURGH, PA
WOODBROOK HOUSE, APARTMENT B-106, 3405 WEST CHESTER PIKE, Newtown Square, PA, 19073
PSC 7, BOX 333, APO, AE, 09104
COUNTRY CLUB ROAD, BOX 244, CRESSON, PA, 16630
PACIS HALL, PO BOX 700, IMMACULATA, PA, 19345-0700
TSUEN WAN ADVENTIST HOSP, TSUEN KING CIRCUIT, TSUEN WAN
391Gum Bush Road, Townsend, DE, 19734-9767
3302 CENTENNIAL STATION, DRIVE, APT 302, WARMINSTER, PA, 18974
6111SPRINGFORD DR, APT O10, Harrisburg, PA, 17111
82A BAKER ST, LONDON, WIU 6AA
454 LEINSTER STREET APT 12, WOODSTOCK ON,   N4S 7G3
WASHINGTON STATE UNIVERSITY, COLLEGE OF PHARMACY, Pullman, WA, 99164
136 HILLVIEW AVENUE, NUMBER 09 10, 669598
3237 TOPEKA DR, MISSISSAUGA ONTARIO, L5M7V1
% BARBARA JOHNS, R D 1 BOX 335, SUNBURY, PA, 17801
    """
}
