#ifndef __CHATSVR_H
#define __CHATSVR_H 1
#include "socket.h"
#include "globtab.h"
#include "crypto.h"

#include <pthread.h>

int main (int argc, char** argv);
void* wrap_cloop (void* args); // for pthreads
void cliloop (int sockfd);
void startsvr (unsigned long inaddr, int port); // Should default to INADDR_ANY, 4045
char* keygen (int size);
void addmsg(char* to, char* from, char* msg);
int waitformsg (int sockfd, char* buff, int len, int waitfor);
void dispatch_msg (msg_t* msg, int sockfd, uchar* authkey, int aklen);
void errorwarn (char* err);
void errorquit (char* err);
void seq ();
#endif
