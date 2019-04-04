Description:
	Client and server interact with each other using CORBA. On the client, the user can add questions and answers. After that, the client prints out the menu which the user can interact with the client. There are four action items which are asking a question, adding a question, removing a questions, and exit. 

How to run:
	1. Open 3 terminals, and go to the quiz_game directory
	2. Type "make" to compile the files.
	3. Run the omniserver by copyying and pasting into terminal 1, and hit enter "sudo omniNames -logdir ~/omnilog/ -errlog ~/omnilog/omniNamesError.txt"

	4. On second terminal copy and paste and press enter "./quizS -ORBInitRef NameService=corbaloc::localhost:4333/NameService", this will start the server that communicates to CORBA

	5. On third terminal copy and paste and press enter "./quizC_one -ORBInitRef NameService=corbaloc::localhost:4333/NameService"  you can add questions and it will print out the interactive menu

	6. Ctrl + C on "./quizS" and "./omniNames" terminal to end the program

Authors:
	Alberto Avitia and Quang Le