import groovy.sql.Sql
def db = Sql.newInstance("jdbc:hsqldb:file:/home/jliang/Desktop/hsqldb/testdb;shutdown=true", 

db.close()