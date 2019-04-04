#ifndef __MY_QUIZ_GAME_INTERFACE_IMPL_H__
#define __MY_QUIZ_GAME_INTERFACE_IMPL_H__

#include <stdlib.h>
#include <string>
#include <vector>
#include "question.hh"

class MyQuizGameInterface_impl : virtual public POA_Quiz
{
private: 
	std::vector<std::string> questions;
	std::vector<std::string> answers;

public:
	virtual void newQuestion(const char* q, const char* ans);
	virtual char * getRandomQuestion();
	virtual char * getQuestion(CORBA::Short index); 
	virtual char * askQuestion(CORBA::Short index); 
	virtual CORBA::Short checkAnswer(CORBA::Short index, const char* ans);
	virtual void removeQuestion(CORBA::Short index);
	virtual CORBA::Short getAmountOfQuestions();
	int getRan();
};

#endif