# ConsoleJMXMonitor

Minimaliste console application to show Heap and Thread via JMX.
- Local JMV with PID
- Remote JVM with url

Options : 
- -Djmx.service.host=localhost
- -Djmx.service.port=9990
- -Djmx.service.url=service:jmx:remote+http://localhost:9990"
- -Djmx.service.login=login require only if credentials required
- -Djmx.service.password=password require only if credentials required
- -Djmx.service.pid=PID local JVM pid (jps -l)
