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
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>
#include <netdb.h>
#include <pthread.h>

#define MY_SOCK_PATH "127.0.0.1"
#define PORT_NUMBER 8080
int listen_id; // nends to be global in order to let
			   // server know when 'ctl+c' is called on this client

// all four are mirror the server's functions
// with same name
void siginit_handler(int sig_num);
void error(char* message);
void *reading(void* client_id);
void *writing(void* client_id);


int main(int argc, char* argv[])
{
	// same as server
	int n;
	// server_address is used to hold the information
	// needed to know to what kind of server you
	// will be connecting to
	struct sockaddr_in server_address;
	// same as server
	char buffer[1024];
	pthread_t thread_read, thread_write;

	listen_id = socket(AF_INET, SOCK_STREAM, 0);
	if(listen_id < 0)
		error("listen failed.");

	// same as server
	memset(buffer, '0', sizeof(buffer));

	// same as server
	server_address.sin_family = AF_INET;
	server_address.sin_port = htons(PORT_NUMBER);
	server_address.sin_addr.s_addr = inet_addr(MY_SOCK_PATH);

	// connect to server
	if(connect(listen_id, (struct sockaddr*)&server_address, sizeof(server_address)) < 0)
		error("connect");
	printf("Connection established\nIP:%s Port:%d\n\nSend first message.\n", MY_SOCK_PATH, PORT_NUMBER);

	pthread_create(&thread_read, NULL, reading, (void *)&listen_id);
	pthread_create(&thread_write, NULL, writing, (void *)&listen_id);
	pthread_join(thread_read, NULL);
	pthread_join(thread_write, NULL);
	exit(0);
}

void siginit_handler(int sig_num)
{
	printf("\nThank you for using the chat system. Shutting down client.\n");
	close(listen_id);
	exit(0);
}
void error(char* message)
{
	perror(message);
	exit(0);
}

void *reading(void* client_id)
{
	char buffer[1024];
	int n;
	memset(buffer, '0', sizeof(buffer));
	int* connected_id = (int *)client_id;
	while(1)
	{
		// recieve message, print and
		// check if server disconnected 
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
			puts("\nServer disconnected.");
			siginit_handler(-1);
			exit(0);
		}
		printf("Server:\t%s", buffer);
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
		// send whatever was inputed to server using write()
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
