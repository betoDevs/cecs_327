CC1=gcc

CFLAGS = -g -c -Wall
#CFLAGS = -ansi -c -Wall -pedantic
all: Client Server

A = Client.o
B = Server.o

Client: $A
	$(CC1) -o Client $A -lpthread

Client.o: Client.c
	$(CC1) -c Client.c

Server: $B
	$(CC1) -o Server $B -lpthread

Server.o: Server.c
	$(CC1) -c Server.c

clean:
	rm *.o
	rm Server
	rm Client

