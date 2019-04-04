#include "MyQuizGameInterface_impl.h"
#include <iostream>
#include <string.h>
#include <stdlib.h>
#include <time.h>

using namespace std;

// method from idl
// newQuestion puts a new question/answer pair 
// in their respective vectors.
void MyQuizGameInterface_impl::newQuestion(const char* q, const char* ans)
{
	this->questions.push_back(q);
	this->answers.push_back(ans);
}

// method from idl
// from a random element position a 
// string which contains the question
// and answer, is returned
char * MyQuizGameInterface_impl::getRandomQuestion()
{
	int x = getRan();
	std::string tmp = "Question: ";
	tmp.append(this->questions[x]);
	tmp.append(", Answer: ");
	tmp.append(this->answers[x]);
	char * s = CORBA::string_alloc(1024);
	strncpy(s, tmp.c_str(), 1024);
	return s;
}

// simple method that returns a random number [0, questions.size()]
int MyQuizGameInterface_impl::getRan()
{
	srand(time(NULL));
	return rand() % this->questions.size();
}

// returns the question at a specified index
// a string is built from the question and answer information and then
// it is copied to a char*, which is then returned
// parameter CORBA::Short index is the idl equivalent to c++ int32,
// this is why casting on first line below is allowed.
char * MyQuizGameInterface_impl::getQuestion(CORBA::Short index)
{
	int place = (int)index;
	std::string tmp = "Question: ";
	tmp.append(this->questions[place]);
	tmp.append(", Answer: ");
	tmp.append(this->answers[place]);
	char * s = CORBA::string_alloc(1024);
	strncpy(s, tmp.c_str(), 1024);
	return s;
}

// Returns just a question at the random specified index
char * MyQuizGameInterface_impl::askQuestion(CORBA::Short index)
{
	int place = (int)index;
	char * s = CORBA::string_alloc(1024);
	strncpy(s, this->questions[place].c_str(), 1024);
	return s;
}

// Checks a user answer to the correct answer at the spot CORBA::Short index
// if the answers match a 1 is returned, 0 otherwise.
CORBA::Short MyQuizGameInterface_impl::checkAnswer(CORBA::Short index, const char* ans)
{
	int place = (int)index;
	if(strcmp(this->answers[place].c_str(), ans) == 0){
		return (CORBA::Short) 1;
	} else
		return (CORBA::Short) 0;

}

// Deletes a question and answer at a specified index
void MyQuizGameInterface_impl::removeQuestion(CORBA::Short index)
{
	int t=(int)index;
	t--;
	if(t < this->questions.size())
	{
		this->questions.erase(this->questions.begin() +t);
		this->answers.erase(this->answers.begin()+t);
	}
}

CORBA::Short MyQuizGameInterface_impl::getAmountOfQuestions()
{
	if(this->questions.size() == 0)
		return (CORBA::Short)0;
	else
		return (CORBA::Short)this->questions.size();
}
