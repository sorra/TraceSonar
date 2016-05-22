Static analysis on Java class files. Supports Java 8 Lambda!

It's able to trace the call hierarchy until "main(args)", recursion or unreachable libraries.

###Quickstart:
Install JDK 8 and Maven.
Run in shell:
`mvn assembly:assembly && java -jar target/tracesonar-0.1-SNAPSHOT.jar -f target/tracesonar-0.1-SNAPSHOT.jar -q 'sorra/tracesonar/core/GreatMap.*'`
Then it prints the call relations in this project, and generates a "traceback.html" file, showing the call hierarchy of class GreatMap.
> `-f` stands for file paths, `-q` stands for queries such as 'package/class.method' or 'package/class.*'

Read the HTML report:
- Your queries are tagged \<h3\>.
- The starting nodes are in blue.
- The boundary nodes are in green borders.
- The potential calls are in gray.

###Why it's useful?
- Helps developers know the impact before doing big refactoring.

- Helps QA define the regression test scope after such refactoring.

It should be robust enough. Feel free to try it out!

###Limitation:
Now that it can find potential calls (calling a superclass's or interface's method), but the classes folder or jar of those superclasses or interfaces must also be scanned.
