JFLAGS = 
JC = javac
JVM = java

.SUFFIXES: .java .class

.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	Test.java\
	Client.java\
	Connection.java\
	Server.java\
	ServerClientTCP.java\
	ServerClientUDP.java

default: classes 

classes: $(CLASSES:.java=.class)

clean:
	$(rm -f) *.class