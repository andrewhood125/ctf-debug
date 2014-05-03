default: clean build run

build:
	javac -cp "*" *.java

run:
	java -cp "*":./ CTFDebug acm.cs.memphis.edu 4444

clean:
	$(RM) *.class *.ctxt
