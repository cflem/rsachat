#ifndef _GLOBTAB_H
#define _GLOBTAB_H 1
#define TABLE_SIZE 1021
#include "socket.h"
#include "crypto.h"

typedef struct {
  char* sender;
  char* msg;
} msg_t;

typedef struct link {
  void* object;
  struct link* next;
} link_t;

typedef struct {
  char* uname;
  pubkey_t key;
  link_t* msgs; 
} usr_t;

typedef struct {
  unsigned char* key;
  usr_t user;
} sess_t;

link_t** table; // HashTable of Username -> User Object
usr_t* table_lookup (char* username); // Use the table
void table_put (char* uname, usr_t* obj);
void table_purge (char* uname);
void release_user (usr_t* obj);
void release_msg_list (link_t* head);
void release_usr_list (link_t* head);
void release_table ();
int hash (char* data);
#endif
