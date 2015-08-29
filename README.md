Static analysis on Java class files.
Supports Java 8 Lambda!

Run the Main class and then it prints the call relations in this project, and generates a "traceback.html" file, showing the callers of class GreatMap.

It's able to trace the method callers until the "main" function, recursion or unreachable libraries.

Why it's useful?
>Helps developers know the impact before doing big refactoring.
>Helps QA define the regression test scope after such refactoring.

Next goal:
Supports JAR, WAR, etc.
