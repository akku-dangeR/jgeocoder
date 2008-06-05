package net.sourceforge.jgeocoder.tiger;

import java.io.Serializable;

import net.sourceforge.jgeocoder.CommonUtils;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

public class JGeocoderConfig implements Serializable{
  private static final long serialVersionUID = 20080604L;
  public static final JGeocoderConfig DEFAULT = new JGeocoderConfig();
  private String _jgeocoderDataHome = 
    CommonUtils.nvl(System.getProperty("jgeocoder.data.home"), "/usr/local/jgeocoder/data");
  public String getJgeocoderDataHome() {
    return _jgeocoderDataHome;
  }
  public void setJgeocoderDataHome(String jgeocoderDataHome) {
    _jgeocoderDataHome = jgeocoderDataHome;
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