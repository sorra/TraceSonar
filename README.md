Static analysis on Java class files. Supports Java 8 Lambda!

It's able to trace the call hierarchy until the program entrance (e.g. main(), servlet or UI controller), recursion or unreachable libraries.

### Quickstart:
Install JDK 8 and Maven.
Try the shell script: `./test.sh`
Then it prints the call relations in this project, and generates a "traceback.html" file, showing the call hierarchy of class GraphStore.

Command line options:
> `-f` stands for file paths to search in, remember to provide all classes folders and jars in this argument
> `-q` stands for queries such as 'com.example.MyClass#myMethod', 'com.example.MyClass#*' or 'com.example.MyClass'

Read the HTML report:
- Your queries are in the title.
- The starting nodes are in blue.
- The boundary nodes are in green borders.
- The potential calls are in gray.

### Why it's useful?
- Helps developers know the impact before doing big refactoring.

- Helps QA define the regression test scope after such refactoring.

It should be robust enough. Feel free to try it out!

### Limitation:
- If extremely slow, try assigning it more memory using JVM argument `-Xmx`.
