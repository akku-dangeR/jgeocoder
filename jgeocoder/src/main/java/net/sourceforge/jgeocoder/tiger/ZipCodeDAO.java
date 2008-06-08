package net.sourceforge.jgeocoder.tiger;

import static net.sourceforge.jgeocoder.AddressComponent.LAT;
import static net.sourceforge.jgeocoder.AddressComponent.LON;
import static net.sourceforge.jgeocoder.AddressComponent.ZIP;

import java.util.Map;

import net.sourceforge.jgeocoder.AddressComponent;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;

/**
 * A zip code database backed by berkeleyDb
 * 
 * @author jliang
 *
 */
public class ZipCodeDAO{
  private static final Log LOGGER = LogFactory.getLog(ZipCodeDAO.class);
  private PrimaryIndex<String, ZipCode> _zipCodeByZip;
  private SecondaryIndex<Location, String, ZipCode> _zipCodeByLocation;
  private PrimaryIndex<Location, CityStateGeo> _cityStateGeoByLocation;
  public ZipCodeDAO(EntityStore store) throws DatabaseException{
    _zipCodeByZip = store.getPrimaryIndex(String.class, ZipCode.class);
    _zipCodeByLocation = store.getSecondaryIndex(_zipCodeByZip, Location.class, "_location");
    _cityStateGeoByLocation = store.getPrimaryIndex(Location.class, CityStateGeo.class);
  }
  public PrimaryIndex<Location, CityStateGeo> getCityStateGeoByLocation() {
    return _cityStateGeoByLocation;
  }
  public SecondaryIndex<Location, String, ZipCode> getZipCodeByLocation() {
    return _zipCodeByLocation;
  }
  public PrimaryIndex<String, ZipCode> getZipCodeByZip() {
    return _zipCodeByZip;
  }
  
  public boolean geocodeByCityState(Map<AddressComponent, String> m){
    String city = m.get(AddressComponent.CITY), state = m.get(AddressComponent.STATE);
    if(StringUtils.isBlank(city)||StringUtils.isBlank(state)){
      return false;
    }
    city = city.replaceAll("\\s+", "");
    try {
      Location loc = new Location();
      loc.setCity(city); loc.setState(state);
      CityStateGeo geo = _cityStateGeoByLocation.get(loc);
      if(geo!= null){
        m.put(LAT, String.valueOf(geo.getLat()));
        m.put(LON, String.valueOf(geo.getLon()));
        return true;
      }
    } catch (DatabaseException e) {
      if(LOGGER.isDebugEnabled()){
        LOGGER.debug("Unable to geocode with city state", e);
      }
      return false;
    }
    return false;
  }
  
  public boolean geocodeByZip(Map<AddressComponent, String> m){
    String zip = m.get(ZIP);
    if(StringUtils.isBlank(zip)){
      return false;
    }
    try {
      ZipCode zipcode = _zipCodeByZip.get(zip);
      if(zipcode != null){
        m.put(LAT, String.valueOf(zipcode.getLat()));
        m.put(LON, String.valueOf(zipcode.getLon()));
        return true;
      }
    } catch (Exception e) {
      if(LOGGER.isDebugEnabled()){
        LOGGER.debug("Unable to geocode with zip", e);
      }
      return false;
    }
    return false;
  }
}
