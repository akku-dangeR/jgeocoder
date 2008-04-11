import TigerDefinition
import TigerTable
import TigerDefinition
import groovy.sql.Sql
import org.apache.commons.lang.StringUtils

def cleanup = true

def db = Sql.newInstance(
        "jdbc:hsqldb:file:/home/jliang/Desktop/hsqldb/testdb;shutdown=true;hsqldb.default_table_type=cached", 
        "sa", "", 'org.hsqldb.jdbcDriver')

File f = new File('/home/jliang/Desktop/TGR42001/')
if(cleanup)
  cleanUp(db)
  
init(db)

def start = System.currentTimeMillis()
f.eachFile{
  processFile(db, it)  
}
println "total time = ${(System.currentTimeMillis() - start)/(1000*60)} minutes"

db.close()

void cleanUp(def db){
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

void processFile(def db, File f){
  println 'importing '+f.getName()
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
  def total = 0, commit = 1000, verbose = false
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
    colval = null
    total++
    if(total%commit == 0){
      println "commit point reached, total = $total"
      db.commit()
    }else if(verbose && total%100 == 0){
      println "$total records inserted"
    }
      
  }
  println "inserted $total records to table ${table.name} from ${f.name}"
  
}

