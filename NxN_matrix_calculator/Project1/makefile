GPP = g++ -O3 -Wall -pthread -std=c++11

UTILS = ../Utilities

A = MultiThread.o
S = Scanner.o
SL = ScanLine.o
U = Utils.o

Aprog: $A $S $(SL) $U
	$(GPP) -o Aprog $A  $S $(SL) $U

MuttiThread.o: MultiThread.cpp
	$(GPP) -c MultiThread.cpp

Scanner.o: $(UTILS)/Scanner.h $(UTILS)/Scanner.cpp
	$(GPP) -c $(UTILS)/Scanner.cpp

ScanLine.o: $(UTILS)/ScanLine.h $(UTILS)/ScanLine.cpp
	$(GPP) -c $(UTILS)/ScanLine.cpp

Utils.o: $(UTILS)/Utils.h $(UTILS)/Utils.cpp
	$(GPP) -c $(UTILS)/Utils.cpp
