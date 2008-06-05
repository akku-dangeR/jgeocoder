package net.sourceforge.jgeocoder.tiger;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.jgeocoder.us.AddressParser;
import net.sourceforge.jgeocoder.us.AddressStandardizer;
import net.sourceforge.jgeocoder.us.AddressParser.AddressComponent;

import com.sleepycat.je.DatabaseException;

public class JGeocoder{
  private ZipCodesDb _zipDb;
  private ZipCodeDAO _zipDao;
  
  public static void main(String[] args) {
    JGeocoder geocoder = new JGeocoder();
    System.out.println(geocoder.geocode("622 mcclellan street, philadelphia, pa 19406"));
    geocoder.cleanup();
  }
  
  public JGeocoder(){
    this(JGeocoderConfig.DEFAULT);
  }
  
  public Map<AddressComponent, String> geocode(String addrLine){
    Map<AddressComponent, String> m  = AddressParser.parseAddress(addrLine);
    m = AddressStandardizer.normalizeParsedAddress(m);
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
    try {
      _zipDb.init(new File(config.getZipDbHome()), false, false);
      _zipDao = new ZipCodeDAO(_zipDb.getStore());
    } catch (DatabaseException e) {
      throw new RuntimeException("Unable to create zip db, "+e.getMessage());
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