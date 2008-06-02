import java.util.zip.ZipFile
def zip = new ZipFile(Thread.currentThread().getContextClassLoader().getResource(/zip_codes.zip/).getFile())def f = zip.entries().nextElement()def r = new BufferedReader(new InputStreamReader(zip.getInputStream(f)))println "Processing $zip ..."def i =0;
r.eachLine{
  println i++;
}