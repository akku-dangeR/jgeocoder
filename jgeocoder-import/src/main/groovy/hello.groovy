import groovy.sql.Sql
def db = Sql.newInstance("jdbc:hsqldb:file:/home/jliang/Desktop/hsqldb/testdb;shutdown=true",         "sa", "", 'org.hsqldb.jdbcDriver')
db.execute '''    CREATE TABLE Athlete (        firstname   VARCHAR(64),        lastname    VARCHAR(64),        dateOfBirth DATE    );'''String athleteInsert = '''    INSERT INTO Athlete (firstname, lastname, dateOfBirth)                VALUES (?, ?, ?);'''db.execute athleteInsert, ['Paul',    'Tergat',     '1969-06-17']db.execute athleteInsert, ['Khalid', 'Khannouchi', '1971-12-22']db.execute athleteInsert, ['Ronaldo', 'da Costa',   '1970-06-07']println ' Athlete Info '.center(25,'-')def fmt = new java.text.SimpleDateFormat('dd. MMM yyyy (E)',                                         Locale.US)db.eachRow('SELECT * FROM Athlete'){ athlete ->    println athlete.firstname + ' ' + athlete.lastname    println 'born on '+ fmt.format(athlete.dateOfBirth)    println '-' * 25}