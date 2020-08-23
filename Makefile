JFLAGS = -g
JC = javac
JVM= java
.SUFFIXES: .java .class
.java.class: ; $(JC) $(JFLAGS) $*.java
CLASSES = \
	     pdollar.java \
	     Point.java \
	     PointCloud.java  \

MAIN = pdollar

default: classes

classes: $(CLASSES:.java=.class)

run: $(MAIN).class
	$(JVM) $(MAIN)

clean: $(RM) *.class

	     
