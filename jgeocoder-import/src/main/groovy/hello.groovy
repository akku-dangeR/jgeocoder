import TigerDefinition
import TigerTable
import TigerDefinition
import groovy.sql.Sql
import org.apache.commons.lang.StringUtils

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
        colval << (c.isNum ? Integer.parseInt(val) : val)
      }
    }
    println colval
    db.execute(insert, colval)
  }
  
}

def db = Sql.newInstance("jdbc:hsqldb:file:/home/jliang/Desktop/hsqldb/testdb;shutdown=true", 
        "sa", "", 'org.hsqldb.jdbcDriver')

TigerDefinition.TIGER_TABLES.each{
   try{ db.execute(it.ddl)}catch(Exception e){ println e.message }
}

File f = new File('/home/jliang/Desktop/TGR42001/test.RT1')
processFile(db, f)

//
//
//String athleteInsert = '''
//    INSERT INTO Athlete (firstname, lastname, dateOfBirth)
//                VALUES (?, ?, ?);
//'''
//db.execute athleteInsert, ['Paul',    'Tergat',     '1969-06-17']
//db.execute athleteInsert, ['Khalid', 'Khannouchi', '1971-12-22']
//db.execute athleteInsert, ['Ronaldo', 'da Costa',   '1970-06-07']
//
//println ' Athlete Info '.center(25,'-')
//def fmt = new java.text.SimpleDateFormat('dd. MMM yyyy (E)',
//                                         Locale.US)
//db.eachRow('SELECT * FROM Athlete'){ athlete ->
//    println athlete.firstname + ' ' + athlete.lastname
//    println 'born on '+ fmt.format(athlete.dateOfBirth)
//    println '-' * 25
//}


db.close()
