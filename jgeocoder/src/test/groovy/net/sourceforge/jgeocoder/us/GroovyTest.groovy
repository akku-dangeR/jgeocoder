package net.sourceforge.jgeocoder.us

import net.sourceforge.jgeocoder.us.RegexLibraryimport java.util.concurrent.ExecutorServiceimport java.util.concurrent.Executorsimport java.util.concurrent.Futureimport java.math.BigIntegerimport java.math.BigInteger
class Test{
  String t
  void helloworld(){
    println "20"
  }
  def String getT(){}
}
class GroovyTest extends GroovyTestCase {
	void testGroovy() {
	  String n = "blah";
	  for(int i=0 ;i<n.length(); i++){
	    println(i)
	  }
	  Test t = new Test()
	  t.helloworld()
	  t.setT("100")
	  String thingy = 'this rain'
	  thingy.eachMatch(/\w+/) { match ->
	    println(match[0].getClass())
	  }
	  ExecutorService exe = Executors.newFixedThreadPool(10)
	  Future f = exe.submit({println 'blah'})
	  f.get()
	}
}
