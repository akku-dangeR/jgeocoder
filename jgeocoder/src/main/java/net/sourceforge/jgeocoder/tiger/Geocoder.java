package net.sourceforge.jgeocoder.tiger;

class Geo{
  public float lat, lon;
  public int zip;
  public String tlid;
  public Geo(float lat, float lon, int zip, String tlid) {
    this.lat = lat;
    this.lon = lon;
    this.zip = zip;
    this.tlid = tlid;
  }
  
}

class Distance{
  public float totalLat=0f, totalLon=0f;
}

public class Geocoder {
  static Geo geocode(int streetnum, String tlid, int fraddr, int fraddl, int toaddr, int toaddl, 
    int zipL, int zipR, float tolat, float tolong, float frlong, float frlat,  
    float long1, float lat1, float long2, float lat2, float long3, float lat3, float long4, float lat4,
    float long5, float lat5, float long6, float lat6, float long7, float lat7, float long8, float lat8,
    float long9, float lat9, float long10, float lat10 ){
    
    Distance distance = getDistance(tolat, tolong, frlong, frlat, long1, lat1, long2, lat2, long3, lat3, long4, lat4, long5, lat5, long6, lat6, long7, lat7, long8, lat8, long9, lat9, long10, lat10);
    int addrStart, addrEnd, zip;
    if(!isLeft(streetnum, fraddr, fraddl, toaddr, toaddl) && (fraddr%2 == streetnum%2)){
      addrStart = fraddr; addrEnd = toaddr; zip = zipR;
    }else{
      addrStart = fraddl; addrEnd = toaddl; zip = zipL;
    }
    
    int dec1 = addrEnd - streetnum, dec2 = addrEnd - addrStart;
    float rel = dec1*1f / dec2;
    if(rel == 1f){ //return start
      return new Geo(frlat, frlong, zip, tlid);
    }
    if(rel == 0f){ //return end
      return new Geo(tolat, tolong, zip, tlid);
    }
    //straight line estimate
    float lat = frlat + (rel * distance.totalLat), lon = frlong + (rel * distance.totalLon);
    
    ////
    
    float tempEndLat = frlat + distance.totalLat, tempEndLon = frlong + distance.totalLon;
    return null;
  }
  
  private static boolean isLeft(int streetnum, int fraddr, int fraddl, int toaddr, int toaddl){
    return fraddr == -1 || (!between(streetnum, fraddr, toaddr) && !between(streetnum, toaddr, fraddr)); 
  }
  
  private static boolean between(int num, int start, int end){
    return num >= start && num <= end;
  }
  
  private static Distance getDistance(float tolat, float tolong, float frlong, float frlat,  
      float long1, float lat1, float long2, float lat2, float long3, float lat3, float long4, float lat4,
      float long5, float lat5, float long6, float lat6, float long7, float lat7, float long8, float lat8,
      float long9, float lat9, float long10, float lat10){
    Distance ret = new Distance();
    float lastLat = frlat, lastLon = frlong;
    if(lat1 != 0f){
      ret.totalLat += Math.abs(lat1 - lastLat);
      ret.totalLon += Math.abs(long1 - lastLon);
      lastLat = lat1;
      lastLon = long1;
    }
    if(lat2 != 0f){
      ret.totalLat += Math.abs(lat2 - lastLat);
      ret.totalLon += Math.abs(long2 - lastLon);
      lastLat = lat2;
      lastLon = long2;
    }
    
    if(lat3 != 0f){
      ret.totalLat += Math.abs(lat3 - lastLat);
      ret.totalLon += Math.abs(long3 - lastLon);
      lastLat = lat3;
      lastLon = long3;
    }
    
    if(lat4 != 0f){
      ret.totalLat += Math.abs(lat4 - lastLat);
      ret.totalLon += Math.abs(long4 - lastLon);
      lastLat = lat4;
      lastLon = long4;
    }
    
    if(lat5 != 0f){
      ret.totalLat += Math.abs(lat5 - lastLat);
      ret.totalLon += Math.abs(long5 - lastLon);
      lastLat = lat5;
      lastLon = long5;
    }
    
    if(lat6 != 0f){
      ret.totalLat += Math.abs(lat6 - lastLat);
      ret.totalLon += Math.abs(long6 - lastLon);
      lastLat = lat6;
      lastLon = long6;
    }
    
    if(lat7 != 0f){
      ret.totalLat += Math.abs(lat7 - lastLat);
      ret.totalLon += Math.abs(long7 - lastLon);
      lastLat = lat7;
      lastLon = long7;
    }
    
    if(lat8 != 0f){
      ret.totalLat += Math.abs(lat8 - lastLat);
      ret.totalLon += Math.abs(long8 - lastLon);
      lastLat = lat8;
      lastLon = long8;
    }
    
    if(lat9 != 0f){
      ret.totalLat += Math.abs(lat9 - lastLat);
      ret.totalLon += Math.abs(long9 - lastLon);
      lastLat = lat9;
      lastLon = long9;
    }
    
    if(lat10 != 0f){
      ret.totalLat += Math.abs(lat10 - lastLat);
      ret.totalLon += Math.abs(long10 - lastLon);
      lastLat = lat10;
      lastLon = long10;
    }
    
    ret.totalLat += Math.abs(tolat - lastLat);
    ret.totalLon += Math.abs(tolong - lastLon);
    
    return ret;
  }
    
}
