#include "globtab.h"

usr_t* table_lookup (char* username) {
  if (table == 0) return 0;
  int hsh = hash(username);
  link_t* list = table[hsh];
  while (list != 0) {
    usr_t* lookup = (usr_t*) list->object;
    if (strcmp(lookup->uname, username) == 0) return lookup;
    list = list->next;
  }
  return 0;
}

void table_put (char* uname, usr_t* obj) {
  if (table == 0) return;
  int hsh = hash(uname);
  link_t* list = table[hsh];
  if (list == 0) {
    list = calloc(1, sizeof(link_t));
    list->object = (void*) obj;
    table[hsh] = list;
    return;
  }
  while (list->next != 0) list = list->next;
  list->next = calloc(1, sizeof(link_t));
  list->next->object = (void*) obj;
  return;
}

void table_purge (char* uname) {
  if (table == 0) return;
  int hsh = hash(uname);
  link_t* list = table[hsh];
  if (list == 0) return;
  if (strcmp(((usr_t*)list->object)->uname, uname) == 0) {
    release_user((usr_t*)list->object);
    link_t* tmp = list->next;
    free(list);
    table[hsh] = tmp;
  }
  while (list->next != 0) {
    usr_t* found = (usr_t*) list->next->object;
    if (strcmp(found->uname, uname) == 0) {
      release_user(found);
      link_t* tmp = list->next->next;
      list->next = tmp;
      return;
    }
    list = list->next;
  }
}

void release_user (usr_t* obj) {
  if (obj == 0) return;
  if (obj->uname != 0) free(obj->uname);
  if (obj->msgs == 0) {
    free(obj);
    return;
  }
  release_msg_list(obj->msgs);
  free(obj);
}

void release_msg_list (link_t* head) {
  if (head->next != 0) release_msg_list(head->next);
  msg_t* msg = (msg_t*)head->object;
  free(msg->sender);
  free(msg->msg);
  free(msg);
  free(head);
}

void release_table () {
  if (table == 0) return;
  int i;
  for (i = 0; i < TABLE_SIZE; i++) {
    if (table[i] == 0) continue;
    release_usr_list(table[i]);
  }
}
 
void release_usr_list (link_t* head) {
  if (head->next != 0) release_usr_list(head->next);
  release_user((usr_t*) head->object);
  free(head);
}

int hash (char* data) {
  long long base = 1, sum = 0;
  int i;
  for (i = 0; data[i] != 0 && data[i] != 0x0a; i++) {
    sum += data[i]*base;
    sum %= TABLE_SIZE;
    base *= 5;
  }
  return (int) sum;
}
