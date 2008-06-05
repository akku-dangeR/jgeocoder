package net.sourceforge.jgeocoder.tiger;

import java.io.File;
import java.util.Map;

import net.sourceforge.jgeocoder.AddressComponent;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
@Entity
class CityStateGeo{
  @PrimaryKey
  private Location _location;
  private float _lat;
  private float _lon;
  public Location getLocation() {
    return _location;
  }
  public void setLocation(Location location) {
    _location = location;
  }
  public float getLat() {
    return _lat;
  }
  public void setLat(float lat) {
    _lat = lat;
  }
  public float getLon() {
    return _lon;
  }
  public void setLon(float lon) {
    _lon = lon;
  }
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }
  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }
}

@Entity
class ZipCode{
  @PrimaryKey
  private String _zip;
  @SecondaryKey(relate=Relationship.MANY_TO_ONE)
  private Location _location;
  @SecondaryKey(relate=Relationship.MANY_TO_ONE)
  private String _county;
  private float _lat;
  private float _lon;
  private String _zipClass;
  public String getZip() {
    return _zip;
  }
  public void setZip(String zip) {
    _zip = zip;
  }
  public Location getLocation() {
    return _location;
  }
  public void setLocation(Location location) {
    _location = location;
  }
  public String getCounty() {
    return _county;
  }
  public void setCounty(String county) {
    _county = county;
  }
  public float getLat() {
    return _lat;
  }
  public void setLat(float lat) {
    _lat = lat;
  }
  public float getLon() {
    return _lon;
  }
  public void setLon(float lon) {
    _lon = lon;
  }
  public String getZipClass() {
    return _zipClass;
  }
  public void setZipClass(String zipClass) {
    _zipClass = zipClass;
  }
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }
  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }
  
}
@Persistent
class Location{ 
  @KeyField(1)
  private String _city;
  @KeyField(2)
  private String _state;

  public String getCity() {
    return _city;
  }
  public void setCity(String city) {
    _city = city;
  }
  public String getState() {
    return _state;
  }
  public void setState(String state) {
    _state = state;
  }
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }
  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }
}

class ZipCodeDAO{
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
        m.put(AddressComponent.LAT, String.valueOf(geo.getLat()));
        m.put(AddressComponent.LON, String.valueOf(geo.getLon()));
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
    String zip = m.get(AddressComponent.ZIP);
    if(StringUtils.isBlank(zip)){
      return false;
    }
    try {
      ZipCode zipcode = _zipCodeByZip.get(zip);
      if(zipcode != null){
        m.put(AddressComponent.LAT, String.valueOf(zipcode.getLat()));
        m.put(AddressComponent.LON, String.valueOf(zipcode.getLon()));
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

class ZipCodesDb{
  private Environment _env = null;
  private EntityStore _store = null;
  public Environment getEnv() {
    return _env;
  }
  public EntityStore getStore() {
    return _store;
  }
  
  public void init(File envHome, boolean readOnly, boolean transactional) throws DatabaseException{
    EnvironmentConfig config = new EnvironmentConfig();
    config.setAllowCreate(!readOnly);
    config.setReadOnly(readOnly);
    config.setTransactional(transactional);
    _env = new Environment(envHome, config);
    StoreConfig config2 = new StoreConfig();
    config2.setAllowCreate(!readOnly);
    config2.setReadOnly(readOnly);
    config2.setTransactional(transactional);
    _store = new EntityStore(_env, "ZipCodeEntityStore", config2);
  }
  
  public void shutdown() throws DatabaseException{
    if(_store != null){
      _store.close();
    }
    if(_env != null){
      _env.close();
    }
  }
  
}