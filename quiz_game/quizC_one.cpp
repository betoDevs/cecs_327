#include "question.hh"
#include <string.h>
#include <iostream>
#include <CORBA.h>
#include <time.h>
#include <stdlib.h>

/** Name is defined in the server.cpp */
#define SERVER_NAME		"MyServerName"

using namespace std;

/*
 * Descriptions for the following methods are 
 * below main()
 */
void addQuestion(const char * q, const char * ans, Quiz_ptr service_server);
void displayQuestion(Quiz_ptr service_server, int x);
bool questionValidation(string s);
string numOfQuestions();
void populateQuestions(int numOfQuestions, Quiz_ptr service_server);
int showMenu();
void showQuestions(Quiz_ptr service_server, int num_questions);
void askQuestion(Quiz_ptr service_server, int num_questions);
void removeQuestion(Quiz_ptr service_server, int num_questions);

int main(int argc, char ** argv)
{
	try {
		//------------------------------------------------------------------------
		// Initialize ORB object.
		//------------------------------------------------------------------------
		CORBA::ORB_ptr orb = CORBA::ORB_init(argc, argv);

		//------------------------------------------------------------------------
		// Resolve service
		//------------------------------------------------------------------------
		Quiz_ptr service_server = 0;

		try {

			//------------------------------------------------------------------------
			// Bind ORB object to name service object.
			// (Reference to Name service root context.)
			//------------------------------------------------------------------------
			CORBA::Object_var ns_obj = orb->resolve_initial_references("NameService");

			if (!CORBA::is_nil(ns_obj)) {
				//------------------------------------------------------------------------
				// Bind ORB object to name service object.
				// (Reference to Name service root context.)
				//------------------------------------------------------------------------
				CosNaming::NamingContext_ptr nc = CosNaming::NamingContext::_narrow(ns_obj);
				
				//------------------------------------------------------------------------
				// The "name text" put forth by CORBA server in name service.
				// This same name ("MyServerName") is used by the CORBA server when
				// binding to the name server (CosNaming::Name).
				//------------------------------------------------------------------------
				CosNaming::Name name;
				name.length(1);
				name[0].id = CORBA::string_dup(SERVER_NAME);
				name[0].kind = CORBA::string_dup("");

				//------------------------------------------------------------------------
				// Resolve "name text" identifier to an object reference.
				//------------------------------------------------------------------------
				CORBA::Object_ptr obj = nc->resolve(name);

				if (!CORBA::is_nil(obj)) {
					service_server = Quiz::_narrow(obj);
				}
			}
		} catch (CosNaming::NamingContext::NotFound &) {
			cerr << "Caught corba not found" << endl;
		} catch (CosNaming::NamingContext::InvalidName &) {
			cerr << "Caught corba invalid name" << endl;
		} catch (CosNaming::NamingContext::CannotProceed &) {
			cerr << "Caught corba cannot proceed" << endl;
		}

		//------------------------------------------------------------------------
		// Start the quiz game
		//------------------------------------------------------------------------
		if (!CORBA::is_nil(service_server)) {
			int num_questions, choice, current_questions;

			//get number of questions
			num_questions = stoi(numOfQuestions());

			//populate question and answers
			populateQuestions(num_questions, service_server);
			num_questions=(int)service_server->getAmountOfQuestions();

			//------------------------------------------------------------------------
			// Show the menu
			// 1. Answer Question. 2. Add a question. 3. Remove a question 4. Exit
			//------------------------------------------------------------------------
			cout<<"Welcome to the game. These are the following questions:\n";
			showQuestions(service_server, num_questions);
			choice = showMenu();

			// if user wants to play
			while(choice != 4)
			{
				// if user wants to be quized
				if(choice == 1){
					num_questions=(int)service_server->getAmountOfQuestions();
					if(num_questions>0)
						askQuestion(service_server, num_questions);
					else
						cout<<"No questions left\n";
				}

				// if user wants to add a question
				else if(choice == 2){
					num_questions=(int)service_server->getAmountOfQuestions();
					populateQuestions(1, service_server);
					num_questions=(int)service_server->getAmountOfQuestions();
				}
				// if user wants to remove a question
				else{
					if(num_questions > 0){
						num_questions=(int)service_server->getAmountOfQuestions();
						removeQuestion(service_server, num_questions);
						num_questions=(int)service_server->getAmountOfQuestions();
					}else
						cout<<"\nNo question left, can't remove from empty list.";	
				}

				// ask user what the next choice is
				num_questions=(int)service_server->getAmountOfQuestions();
				cout<<"\nMake a decision";
				choice = showMenu();
			}
		}

		//------------------------------------------------------------------------
		// Destroy OBR
   		//------------------------------------------------------------------------
		orb->destroy();

	} catch (CORBA::UNKNOWN) {
		cerr << "Caught CORBA exception: unknown exception" << endl;
	}
}

// Use the newQuestion() method from the servant class
// and add a question/answer pair
void addQuestion(const char * q, const char * ans, Quiz_ptr service_server)
{
	service_server->newQuestion(q, ans);
}

// Use the getQuestion() method from servant class
// and retrieve the user specified question.
void displayQuestion(Quiz_ptr service_server, int x)
{
	CORBA::Short index = (CORBA::Short) x;
	cout<<service_server->getQuestion(index)<<".\n";
}

// Ensure that the question is not only numbers
// and that it is not empty. 
// This method is used for validating user input question 's'.
bool questionValidation(string s)
{
	if(s.empty())
		return false;
	if(s.find_first_not_of("0123456789") == string::npos)
		return false;
	else
		return true;
}

// Ask and validate that the user enters a number, n >= 0
// return n as a string.
string numOfQuestions()
{
	string n_tmp;
	cout<<"Please enter the number of questions you want to have: "; 
	getline(cin, n_tmp);
	while(n_tmp.find_first_not_of("0123456789") != string::npos || n_tmp.empty()){
		cout<<"Invalid input.\n";
		cin.clear();
		cout<<"Please enter the number of questions you want to have: "; 
		getline(cin, n_tmp);
	}
	return n_tmp;
}

// populate the questions and answers on servant class
// a pointer to the server and the specified number of questions are needed
// validation is performed on the question, via questionValidation() 
// and on the answer, just by determining that it is not empty.
void populateQuestions(int num_questions, Quiz_ptr service_server)
{
	string q, a;
	for(int i = 0; i < num_questions; i++)
	{
		cout<<"Enter the question.\n"; //for question number "<<(i+1)<<endl;
		getline(cin, q);
		cout<<"Enter the answer.\n";
		getline(cin, a);
		if(questionValidation(q) && !a.empty())
			addQuestion(q.c_str(), a.c_str(), service_server);
		else{
			cout<<"Invalid input, try again\n"; 
			i--;
		}
	}
}

// Show the game menu to the user and take in his/her choice
// choice is validated to be in range [1, 4]
// the choice is returned as an int
int showMenu()
{
	string n_tmp;
	cout<<"\n1. Ask me a question\n";
	cout<<"2. Add a new Question\n";
	cout<<"3. Remove a Question\n";
	cout<<"4. Exit Game\n";
	cout<<"Please make a decision: ";

	getline(cin, n_tmp);
	while(n_tmp.find_first_not_of("0123456789") != string::npos || n_tmp.empty() || (stoi(n_tmp)>4 || stoi(n_tmp)<1 )){
		cout<<"Make a valid decision.\n";
		cin.clear();
		cout<<"Please make a decision: ";
		getline(cin, n_tmp);
	}
	return stoi(n_tmp);
}

// Display all the current question and answers to the user
// This is performed when the game starts and after the user
// removes a question.
void showQuestions(Quiz_ptr service_server, int num_questions)
{
	for(int i = 0; i < num_questions; i++)
	{
		cout<<"Q "<<i+1<<".";
		displayQuestion(service_server, i);
	}
}

// a random question qill be asked to the user via the servant's
// method askQuestion(), then an answer from user will be prompted
// and validated. The valid answer is then checked using the servant's
// class method checkAnser(const* char, const* char) and return a 1 
// if answer is correct, 0 otherwise. The result is displayed to user.
void askQuestion(Quiz_ptr service_server, int num_questions)
{
	srand(time(NULL));
	int ran = rand()%num_questions;
	string ans;
	string q = service_server->askQuestion(ran);
	// input validation
	do{
		// ask question
		cout<<"Q "<<ran+1<<": "<<q<<"\nAnswer: ";
		getline(cin, ans);
		if(ans.empty())
			cout<<"Please enter answer\n";
	}while(ans.empty());

	// check answer
	if((int)service_server->checkAnswer(ran, ans.c_str()) == 1)
	{
		cout<<"Correct!\n";
	} else {
		cout<<"Wrong!\n";
	}
}

// remove a specified question from user
// the choice that user inputs is validated to be an in within
// the range [0, num_of_questions]
// After the removal is made via removeQuestion(CORBA::Short index),
// from the servant class, the remaining questions are then displayed
// to user and game continues.
void removeQuestion(Quiz_ptr service_server, int num_questions)
{
	string n_tmp;
	cout<<"Here are the following questions\n";
	showQuestions(service_server, num_questions);
	cout<<"Choose the number of the one you would like to remove: ";

	getline(cin, n_tmp);
	while(n_tmp.find_first_not_of("0123456789") != string::npos || n_tmp.empty() || (stoi(n_tmp)>num_questions || stoi(n_tmp)<1 )){
		cout<<"Make a valid decision.\n";
		cin.clear();
		cout<<"Choose the number of the one you would like to remove: ";
		getline(cin, n_tmp);
	}
	service_server->removeQuestion((CORBA::Short)stoi(n_tmp));
	cout<<"\nQuestion: "<<stoi(n_tmp)<<" has been removed. Questions left: \n";
	showQuestions(service_server, num_questions-1);
}