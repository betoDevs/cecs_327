# Makefile

OMNIORB_HOME=/usr

IDL=$(OMNIORB_HOME)/bin/omniidl
IDLFLAGS=-bcxx

INCLUDES=-I$(OMNIORB_HOME)/include -I$(OMNIORB_HOME)/include/omniORB4

LIBS=-L$(OMNIORB_HOME)/lib -lomnithread -lomniORB4

IDL_FILE = question.idl

.PHONY: all
all: quizS quizC_one

quizS: quizS.o MyQuizGameInterface_impl.o questionSK.o
	$(CXX) -o $@ $^ $(LIBS)

quizS.o: quizS.cpp MyQuizGameInterface_impl.h

MyQuizGameInterface_impl.h: question.hh

quizC_one: quizC_one.o questionSK.o
	$(CXX) -o $@ $^ $(LIBS)

MyQuizGameInterface_impl.o: MyQuizGameInterface_impl.cpp MyQuizGameInterface_impl.h question.hh

questionSK.o: questionSK.cc question.hh

questionSK.cc question.hh: $(IDL_FILE)
	$(IDL) $(IDLFLAGS) $<

.PHONY: clean
clean:
	find . -maxdepth 1 -type f -name "*.bak" -exec rm -f {} \;
	find . -maxdepth 1 -type f -name "*.o" -exec rm -f {} \;
	find . -maxdepth 1 -type f -name "*.stackdump" -exec rm -f {} \;
	find . -maxdepth 1 -type f -name "*.exe" -exec rm -f {} \;
	find . -maxdepth 1 -type f -name "question.hh" -exec rm -f {} \;
	find . -maxdepth 1 -type f -name "questionSK.cc" -exec rm -f {} \;

.cpp.o:
	$(CXX) $(CXXFLAGS) -o $@ -c $< $(INCLUDES)

.cc.o:
	$(CXX) $(CXXFLAGS) -o $@ -c $< $(INCLUDES)