import java.sql.DriverManagerimport java.sql.DriverManagerimport java.sql.DriverManagerimport java.sql.Connectionimport java.sql.Connectionimport java.util.zip.ZipFileimport java.util.zip.ZipEntryimport TigerDefinition
import TigerTable
import groovy.sql.Sql
import org.apache.commons.lang.StringUtils
class Config{  def cleanup = true    def db = null}//this is calling a private method of DriverManager :D //because the maven classloader does not have the driver classConnection conn = DriverManager.getConnection("jdbc:h2:/home/jliang/Desktop/h2db/testdb",     new Properties(), getClass().getClassLoader())
Config config = new Config()
config.db = new Sql(conn)
if(config.cleanup)
  cleanUp(config.db)
  
init(config.db)
def f = new ZipFile('/home/jliang/Desktop/TGR42101.ZIP')def entries = f.entries()
def start = System.currentTimeMillis()while(entries.hasMoreElements()){  processFile(config.db, f, entries.nextElement())}
println "total time = ${(System.currentTimeMillis() - start)/(1000*60)} minutes"

config.db.close()
/////////////////helpers///////////////////////////////void cleanUp(def db){
  println 'dropping all tiger line tables'
  TigerDefinition.TIGER_TABLES.each{
    try{ db.execute('drop table ' +it.name)}catch(Exception e){ println e.message }
  }  
}

void init(def db){
  println 'creating tiger line database schema'
  TigerDefinition.TIGER_TABLES.each{
    try{ db.execute(it.ddl)}catch(Exception e){ println e.message }
  }
}

void processFile(def db, ZipFile f,  ZipEntry entry){  def reader = new BufferedReader(new InputStreamReader(f.getInputStream(entry)))
  println 'importing '+entry.name
  def ext = entry.name.substring(entry.name.lastIndexOf('.')+1)
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
  def insert = "insert into ${table.name} (${col.join(',')}) values (${q.join(',')})"
  def total = 0, commit = 5000, error = 0
  reader.eachLine{line ->
    if(StringUtils.isBlank(line)) return
    
    def colval = []
    table.columns.each{ c ->
      def val = line[c.range].trim()
      if(val == ''){colval << null}
      else{
        if(val.startsWith('+')) val = val.substring(1)
        colval << (c.isNum ? Long.parseLong(val) : val)
      }
    }    try{db.execute insert, colval}catch(Exception e){       println 'Error inserting record: '+e.getMessage()      error++    }
    total++
    if(total%commit == 0){
      println "commit point reached, total = $total"
      db.commit()
    }
      
  }
  db.commit()
  println "inserted $total records to table ${table.name} from ${entry.name}"  if(error > 0) println "there were $error errors"
  
}


