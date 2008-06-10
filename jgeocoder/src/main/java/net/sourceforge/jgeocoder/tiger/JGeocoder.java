package net.sourceforge.jgeocoder.tiger;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.jgeocoder.AddressComponent;
import net.sourceforge.jgeocoder.us.AddressParser;
import net.sourceforge.jgeocoder.us.AddressStandardizer;

import com.sleepycat.je.DatabaseException;
//TODO: the address database does not have ST LOUIS, ST JOHN, it only has
//SAINT JOHN, SAINT LOUIS, need to expand ST if zip is missing before searching
public class JGeocoder{
  private ZipCodesDb _zipDb;
  private ZipCodeDAO _zipDao;
//  private TigerLineDao _tigerDao;
  public JGeocoder(){
    this(JGeocoderConfig.DEFAULT);
  }
  
  public Map<AddressComponent, String> geocode(String addrLine){
    Map<AddressComponent, String> m  = AddressParser.parseAddress(addrLine);
    if(m == null) return null;  //FIXME: throw exception instead
    m = AddressStandardizer.normalizeParsedAddress(m);
    if(m == null) return null;
    return geocode(m);
  }

  public Map<AddressComponent, String> geocode(Map<AddressComponent, String> normalizedAddr){
    Map<AddressComponent, String> m = new HashMap<AddressComponent, String>(normalizedAddr);
    if(_zipDao.geocodeByZip(m)){
      return m;
    }else if(_zipDao.geocodeByCityState(m)){
      return m;
    }
    return m;
  }
  
  public JGeocoder(JGeocoderConfig config){
    _zipDb = new ZipCodesDb();
//    _tigerDao = new TigerLineDao(config.getTigerDataSource());
    try {
      _zipDb.init(new File(config.getJgeocoderDataHome()), false, false);
      _zipDao = new ZipCodeDAO(_zipDb.getStore());
    } catch (DatabaseException e) {
      throw new RuntimeException("Unable to create zip db, make sure your system property 'jgeocoder.data.home' is correct"
          +e.getMessage());
    }
    
  }
  
  public void cleanup(){
    if(_zipDb != null){
      try {
        _zipDb.shutdown();
      } catch (DatabaseException e) {
        throw new RuntimeException("Unable to shutdown zip db, "+e.getMessage());
      }
      _zipDb = null;
    }
  }
  
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    cleanup();
  }
}