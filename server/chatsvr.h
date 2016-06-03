#ifndef __CHATSVR_H
#define __CHATSVR_H 1
#include "socket.h"
#include "globtab.h"
#include "crypto.h"

#include <pthread.h>

int main (int argc, char** argv);
void* wrap_cloop (void* args); // for pthreads
void cliloop (int sockfd);
int find_af (char* addr);
void startsvr (char* inaddr, int port, char* svrlog, char* errlog);
char* keygen (int size);
void addmsg(char* to, char* from, char* msg);
int waitformsg (int sockfd, char* buff, int len, int waitfor);
void dispatch_msg (msg_t* msg, int sockfd, uchar* authkey, int aklen);
void errorwarn (char* err);
void errorquit (char* err);
void seq ();
#endif
