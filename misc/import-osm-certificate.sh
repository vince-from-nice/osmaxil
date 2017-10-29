#/bin/bash

/e/Software/JDK\ 1.8.0.25/bin/keytool.exe -delete -alias wwwopenstreetmaporg -keystore  /e/Software/JDK\ 1.8.0.25/jre/lib/security/cacerts
/e/Software/JDK\ 1.8.0.25/bin/keytool.exe -import -alias wwwopenstreetmaporg -keystore  /e/Software/JDK\ 1.8.0.25/jre/lib/security/cacerts -file /e/Temporary/wwwopenstreetmaporg.crt

