package net.sourceforge.jgeocoder.tiger;

import java.io.File;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

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
  
  
}
@Entity
class Location{
  @PrimaryKey(sequence="")
  long myPrimaryKey; 
  private String _city;
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
  
}

class ZipCodesDb{
  private Environment _env = null;
  
  public Environment getEnv() {
    return _env;
  }
  
  public void init(File envHome, boolean readOnly) throws DatabaseException{
    EnvironmentConfig config = new EnvironmentConfig();
    config.setAllowCreate(!readOnly);
    config.setReadOnly(readOnly);
    _env = new Environment(envHome, config);
  }
  
  public void shutdown() throws DatabaseException{
    if(_env != null){
      _env.cleanLog(); _env.close();
    }
  }
  
}