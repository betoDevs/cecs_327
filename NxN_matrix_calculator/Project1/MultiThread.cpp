/*
	This program is to be compiled inside the Project1 folder
	of the HM1 folder. Compile with make and run with ./Aprog file1 file2
	Authors: Quang Le, Alberto Avitia
*/
#include<string>
#include<iostream>
#include<fstream>
#include<vector>
#include<cstdlib>
#include<pthread.h>
#include<unistd.h>
#include<cmath>
#include<bits/stdc++.h>

using namespace std;

#include "../Utilities/Utils.h"
#include "../Utilities/Scanner.h"
#include "../Utilities/ScanLine.h"

#define LENGTH 3
#define NUM_THREADS LENGTH*LENGTH

// Create both square matrices, A and B, for input
// RESULT will hold the multilplication values
// Global variables are used so it's easier to use threads
// to write to them
int A[LENGTH][LENGTH];
int B[LENGTH][LENGTH];
int RESULT[LENGTH][LENGTH];

// perform matrix multiplication for each thread
void *Multiply(void * t);
// read in first and second matrix
void readA(string path);
void readB(string path);
// assign each thread on which elements to perfom
// multiplication
void multiply_matrix(); 

int main(int argc, char* argv[])
{
	if(argc != 3)
	{
		cout<<"Input required is: program_name, file_of_first_matrix, file_of_second_matrix"<<endl;
		exit(-1);
	}

	string path_a, path_b;
	path_a = argv[1];
	path_b = argv[2];
	readA(path_a);
	readB(path_b);
	multiply_matrix();
	cout<<endl;

	return 0;
}

/*
	Each thread will be initialized to this function,
	perform the multplication, store the value in the designated
	element of RESULT matrix, and exit.

	variables row and col are used to hold the row of matrix A and
	column of matrix B that will be multiplied together for the
	RESULT[row][column] element. They are also used in case the values pointed
	to by p are changed.
*/
void *Multiply(void * t) 
{
   int sum = 0;
   int *p = (int *) t;
   int row = *(p);
   int col = *(p+1);
   for (int i = 0; i < LENGTH; i++)
   {
   		sum += A[row][i] * B[col][i];
   }
   RESULT[row][col] = sum;
   
   /* 
   	  Uncomenting the code below will print out the resulting matrix of
   	  AB and format the output to match a matrix look
   */
   /* 
	   ofstream inFile;
	   inFile.open("numbers_results.txt",ios::app);
	   if(*(p) == (LENGTH-1) && *(p+1) == (LENGTH-1))
	   {
	   		inFile<<sum;	
	   }
	   else if(*(p+1) == (LENGTH-1))
	   {
	   		inFile<<sum<<endl;
	   }
	   else
	   {
	   		inFile<<sum<<" ";
	   }
	     inFile.close();
   */
   pthread_exit(NULL);
}

/*
   Read in first matrix and stores in global variable A. It is 
   assumed that the input are in row and column form separated by spaces,
   and contain a '\n' at the end of each row.

   @path parameter refers to the file where the first matrix is located.

   Variable ss from std::stringstream is used to store a complete row that was
   read in by getline() in the previous line. ss will allow for the the use of
   ' ', white space, delimeters in order to correctly place each integet in it's
   respected place.
*/
void readA(string path)
{
	string c;
	ifstream inFile;
	inFile.open(path);
	for(int i = 0; i < LENGTH; i++)
	{
		getline(inFile,c);
		stringstream ss(c);
		for(int j = 0; j < LENGTH; j++)
		{
			getline(ss, c, ' ');
			A[i][j] = stoi(c);	
		}
	}
	inFile.close();
}

/*
   Read in second matrix as a transpose
   and stores in global variable B. This allows for
   easier manipulation when performing matrix multiplication. It is 
   assumed that the input are in row and column form separated by spaces,
   and contain a '\n' at the end of each row.

   @path parameter refers to the file where the second matrix is located

   Variable ss from std::stringstream is used to store a complete row that was
   read in by getline() in the previous line. ss will allow for the the use of
   ' ', white space, delimeters in order to correctly place each integet in it's
   respected place.
*/
void readB(string path)
{
	string c;
	ifstream inFile;
	inFile.open(path);
	for(int i = 0; i < LENGTH; i++)
	{
		getline(inFile,c);
		stringstream ss(c);
		for(int j = 0; j < LENGTH; j++)
		{
			getline(ss, c, ' ');
			B[j][i] = stoi(c);
		}
	}
	inFile.close();
}

/*
	Controlls which thread will perform which multiplication, 
	and place the result in the designated spot given by pointer p.

	After the resulting matrix is obtained then it is outputted to
	console.

 */
void multiply_matrix()
{
	int rc;
	int k = 0;
	int position[2] = {0,0};
	pthread_t threads[NUM_THREADS];
	//rc = pthread_create(&threads[k], NULL, Multiply, (void *)&position);

	// assign a thread per element to be calculated
	for(int i = 0; i < LENGTH; i++)
	{
		for(int j = 0; j < LENGTH; j++)
		{
			position[0] = i; position[1] = j;
			rc = pthread_create(&threads[k], NULL, Multiply, (void *)&position);
			k++;
			if (rc){
				cout<<"Error: unable to create thread, "<<rc<<endl;
				exit(-1);
			}
			// sleep for 120 nanoseconds
			usleep(120);
		}
	}
	// display result on console
	for(int i = 0; i < LENGTH; i++)
	{
		for(int j = 0; j < LENGTH; j++)
		{
			cout<<RESULT[i][j]<<" ";
		}
		cout<<endl;
	}
}
