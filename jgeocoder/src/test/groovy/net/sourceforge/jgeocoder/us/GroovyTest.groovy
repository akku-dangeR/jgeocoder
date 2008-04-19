package net.sourceforge.jgeocoder.us

import net.sourceforge.jgeocoder.us.RegexLibraryimport java.util.concurrent.ExecutorServiceimport java.util.concurrent.Executorsimport java.util.concurrent.Futureimport java.math.BigIntegerimport java.math.BigInteger
import java.util.regex.Patternimport java.util.regex.Matcher
class Person{
  String name
  int age
}

class GroovyTest extends GroovyTestCase {
    def testClosure(Closure c){
      c(1,2 ,3,4,5)
    }
	void testGroovy() {
	  def t = {a, b,c,d,e -> println a+b}
	  println testClosure(t)
	}
}
