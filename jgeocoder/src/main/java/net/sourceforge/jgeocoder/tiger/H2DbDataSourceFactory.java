package net.sourceforge.jgeocoder.tiger;

import javax.sql.DataSource;

import net.sourceforge.jgeocoder.CommonUtils;

import org.h2.jdbcx.JdbcDataSource;

public class H2DbDataSourceFactory{
  private H2DbDataSourceFactory(){}
  private static String _tigerUrl =
    CommonUtils.nvl(System.getProperty("jgeocoder.tiger.url"), "/usr/local/jgeocoder/tiger/tiger");

  public static DataSource getH2DbDataSource(){
    return getH2DbDataSource(_tigerUrl);
  }
  
  public static DataSource getH2DbDataSource(String tigerUrl){
    JdbcDataSource ds =  new JdbcDataSource();
    ds.setURL(tigerUrl);
    return ds;
  }
}