Magic Screenshot Tool (Java 8 + Selenium 3)

This is a complete Maven project that builds a fat `.jar` file working with Java 8.

1. Prerequisites:
   - Java 8 must be installed (check with `java -version`)
   - Chrome installed
   - chromedriver.exe matching Chrome version placed in `drivers/`

2. To build the standalone .jar:
   mvn clean package

3. To run the tool:
   java -jar target/screenshot-tool-1.0.jar

Screenshots will be saved in:
   C:\Users\<your-user>\Documents\Magic_Tool\<YYYY-MM-DD>\

Dependencies are bundled inside the JAR (no downloads required at runtime).
