# Screenshot Tool (Java)

This Java tool:
- Reads `URLs.csv` from `~/Documents/Magic_Tool/`
- Loads `ReportTemplate.docx` from `/template`
- Takes headless screenshots of URLs using Selenium
- Replaces `$URLn` and `$IMGn` in the template
- Saves the final report in a dated subfolder under `Magic_Tool`

## Build

```bash
mvn clean package
```

## Run

```bash
java -jar target/screenshot-tool.jar
```

Make sure `URLs.csv` and `ReportTemplate.docx` are in the correct locations.
