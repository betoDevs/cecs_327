/* 
 * Project 2. Implement a simple client-host connection over same machine
 * IP address: 127.0.0.1 on Port: 8080
 * Authors: Alberto Avitia and Quang Le
 */
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <pthread.h>

#define MY_SOCK_PATH "127.0.0.1"
#define PORT_NUMBER 8080
int connected_id; // nends to be global in order to disconnect
				  // client when 'ctl+c' is called

// this function will disconnect the client
// then shut down the server
void siginit_handler(int sig_num);
// used to handle error at different 
// different stages of the program
void error(char* message);
// used to read input from client's 'writing' thread 
// and output it on server's terminal
void *reading(void* client_id);
// used to constantly get input from keyboard and 
// send it to clients 'reading' thread
void *writing(void* client_id);

int main(int argc, char *argv[])
{
	// listen_id to initialize socket, connected_id for the new 
	// socket when server accepts client connect()
	// clilen to hold the size of the expected client address 
	// 'n' is used to check for errors when sending/recieving messages
	int listen_id, n, clilen;
	// sockaddr_in is used to bind a socket connection to an address
	// and to know what to accept from client
	struct sockaddr_in server_address, client_address;
	// This buffer will hold the string message to
	// be recieved and sent.
	char buffer[1024];
	// one thread to read from client
	// other to input from stdin and send to client
	pthread_t thread_read, thread_write;

	// Start a socket with TCP protocol
	listen_id = socket(AF_INET, SOCK_STREAM, 0);
	if (listen_id < 0)
		error("connecting socket failed");

	// Clearing structure
	// Filling all chars to be displayed with '0'
	memset(&server_address, '0', sizeof(server_address));
	memset(buffer, '0', sizeof(buffer));


	// using IPv4. the new one is IPv6
	server_address.sin_family = AF_INET;
	// use local machine ip address as the connection address
	server_address.sin_addr.s_addr = inet_addr(MY_SOCK_PATH);
	// port numbers above 2000 are generally available
	server_address.sin_port = htons(PORT_NUMBER);

	// Assigning address to my socket
	// if the vaue return is not '0' then there was an error
	if((bind(listen_id, (struct sockaddr*)&server_address, sizeof(server_address))))
		error("binding failed");
	
	// listen to only one client
	listen(listen_id, 5);

	// initial connection. needs the current socket and
	// the type and size of the client trying to connect
	// this is where server waits for client to connect
	clilen = sizeof(client_address);
	connected_id = accept(listen_id, (struct sockaddr*) &client_address, &clilen);
	if(connected_id < 0)
		error("accepting (first) failed.");
	puts("Connected!\n");

	pthread_create(&thread_read, NULL, reading, (void *)&connected_id);
	pthread_create(&thread_write, NULL, writing, (void *)&connected_id);
	pthread_join(thread_read, NULL);
	pthread_join(thread_write, NULL);
	exit(0);
}

void siginit_handler(int sig_num)
{
	printf("\nThank you for using the chat system. Shutting down server.\n");
	close(connected_id);
	exit(0);
}

void error(char* message)
{
	perror(message);
	exit(0);
}

void *reading(void* client_id)
{
	signal(SIGINT, siginit_handler);
	char buffer[1024];
	int n;
	memset(buffer, '0', sizeof(buffer));
	int* connected_id = (int *)client_id;

	while(1)
	{
		// recieve message, print and
		// check if client disconnected 
		// this will be known when n = 0.
		// if client did not disconnect then
		// clear buffer and repeat.
		n = read(*connected_id, buffer, 1023);
		if( n < 0)
		{
			puts("error reading. Exiting");
			return NULL;
		}
		if (n == 0)
		{
			puts("\nClient disconnected.");
			siginit_handler(-1);
			exit(0);
		}
		printf("Client:\t%s", buffer); 
		memset(buffer, '0', sizeof(buffer));

	}
}

void *writing(void* client_id)
{
	char buffer[1024];
	memset(buffer, '0', sizeof(buffer));
	int n;
	int* connected_id = (int *)client_id;
	while(1)
	{
		// Get input from stdin and store in buffer.
		// send whatever was inputed to client using write()
		// clear buffer and get ready for input again
		fgets(buffer, sizeof(buffer), stdin);
		n = write(*connected_id, buffer, 1023);
		if (n < 0)
		{
			printf("error writing. Exiting");
			return NULL;
		}
		memset(buffer, '0', sizeof(buffer));
	}
}
