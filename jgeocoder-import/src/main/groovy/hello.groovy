import TigerDefinition
import TigerTable
import TigerDefinition
import groovy.sql.Sql
import org.apache.commons.lang.StringUtils

def cleanup = true

def db = Sql.newInstance("jdbc:hsqldb:file:/home/jliang/Desktop/hsqldb/testdb;shutdown=true", 
        "sa", "", 'org.hsqldb.jdbcDriver')

File f = new File(/C:\Users\jliang\workspace\jgeocoder-import\src\test.RT1/)
if(cleanup)
  cleanUp(db)
  
init(db)
processFile(db, f)

db.close()

void cleanUp(def db){
  TigerDefinition.TIGER_TABLES.each{
    try{ db.execute('drop table ' +it.name)}catch(Exception e){ println e.message }
  }  
}

void init(def db){
  TigerDefinition.TIGER_TABLES.each{
    try{ db.execute(it.ddl)}catch(Exception e){ println e.message }
  }
}

void processFile(def db, File f){

  def ext = f.getName().substring(f.getName().lastIndexOf('.')+1)
  if(ext == 'MET') return
  def tableName = 'tiger_'+ext.toLowerCase().substring(2)
  TigerTable table
  TigerDefinition.TIGER_TABLES.each{
    if(it.name == tableName) table = it
  }
  
  def col = [], q = []
  table.columns.each{
    col << it.name 
  }
  col.size().times{
    q << '?'
  }
  def insert = "insert into ${table.name} (${col.join(',')}) values (${q.join(',')});"
  f.eachLine{line ->
    if(StringUtils.isBlank(line)) return
    
    def colval = []
    table.columns.each{ c ->
      def val = line[c.range].trim()
      if(val == ''){colval << null}
      else{
        if(val.startsWith('+')) val = val.substring(1)
        colval << (c.isNum ? Long.parseLong(val) : val)
      }
    }
    db.execute(insert, colval)
  }
  
}

