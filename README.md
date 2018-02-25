Static analysis on Java class files. Supports Java 8 Lambda!

It's able to trace the call hierarchy until the program entrance, recursion or unreachable libraries.

### Quickstart:
Install JDK 8 and Maven.
Try the shell script: `./test.sh`
Then it prints the call relations in this project, and generates a "traceback.html" file, showing the call hierarchy of class GreatMap.
> `-f` stands for file paths, `-q` stands for queries such as 'com.example.MyClass#myMethod' or 'com.example.MyClass#*'

Read the HTML report:
- Your queries are tagged \<h3\>.
- The starting nodes are in blue.
- The boundary nodes are in green borders.
- The potential calls are in gray.

### Why it's useful?
- Helps developers know the impact before doing big refactoring.

- Helps QA define the regression test scope after such refactoring.

It should be robust enough. Feel free to try it out!

### Limitation:
Now that it can find potential calls (calling a superclass's or interface's method), but the classes folder or jar of those superclasses or interfaces must also be scanned.
