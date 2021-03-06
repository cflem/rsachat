#include "chatsvr.h"

int main (int argc, char** argv) {
  // ./server [port[ svrlog[ errorlog[ bind_addr]]]]
  int port = 4044;

  char *svrlog, *errlog;
  if (argc > 1) port = atoi(argv[1]);
  if (argc > 2) svrlog = argv[2];
  if (argc > 3) errlog = argv[3];
  
// make a runnable eventually
  startsvr((argc < 5) ? "0.0.0.0" : argv[4], 4044, svrlog, errlog);
}

int find_af (char* addr) {
  int i;
  for (i = 0; addr[i] != 0; i++) {
    if ('0' <= addr[i] && addr[i] <= '9') continue;
    if ('a' <= addr[i] && addr[i] <= 'f') return AF_INET6; // no hex in ipv4
    if ('A' <= addr[i] && addr[i] <= 'F') return AF_INET6;
    if (addr[i] == '.') return AF_INET;
    if (addr[i] == ':') return AF_INET6;
    return IDK_AF;
  }
  return IDK_AF;
}

void startsvr (char* inaddr, int port, char* svrlog, char* errlog) {
  int sockfd;
  struct sockaddr serv_addr;

  table = calloc(TABLE_SIZE, sizeof(link_t*));

  int fam = find_af(inaddr);
  if (fam == IDK_AF) errorquit("Quit: Bad bind address.");
  sockfd = socket(fam, SOCK_STREAM, 0);
  if (sockfd < 0) errorquit("Quit: Failed to open socket.");

  memset((char*) &serv_addr, 0, sizeof(serv_addr));
  if (fam == AF_INET) {
    struct sockaddr_in serv_addr4;
    memset(&serv_addr4, 0, sizeof(serv_addr4));
    serv_addr4.sin_family = AF_INET;
    serv_addr4.sin_port = htons(port);
    if (inet_pton(AF_INET, inaddr, &(serv_addr4.sin_addr)) < 1) errorquit("Quit: Could not resolve bind address.");
    memcpy(&serv_addr, &serv_addr4, sizeof(serv_addr));
  } else {
    struct sockaddr_in6 serv_addr6;
    memset(&serv_addr6, 0, sizeof(serv_addr6));
    serv_addr6.sin6_family = AF_INET6;
    serv_addr6.sin6_port = htons(port);
    if (inet_pton(AF_INET6, inaddr, &(serv_addr6.sin6_addr)) < 1) errorquit("Quit: Could not resolve bind address.");
    memcpy(&serv_addr, &serv_addr6, sizeof(serv_addr));
  }

  if (bind(sockfd, &serv_addr, sizeof(serv_addr)) < 0) errorquit("Quit: Could not bind socket to port.");
  daemon(0, 0);
  if (svrlog == 0) svrlog = "server.log";
  int logfd = open(svrlog, O_WRONLY | O_APPEND | O_CREAT, 0644);
  int errfd;
  if (errlog == 0) errfd = logfd;
  else errfd = open(errlog, O_WRONLY | O_APPEND | O_CREAT, 0644);
  dup2(logfd, fileno(stdout));
  dup2(errfd, fileno(stderr));

  listen(sockfd, 5);

  while (1) {
    struct sockaddr_in cli_addr;
    int clilen = sizeof(cli_addr), pid, newfd;

    newfd = accept(sockfd, (struct sockaddr*) &cli_addr, &clilen);

    pthread_t clithread;
    pthread_create(&clithread, 0, wrap_cloop, (void*)&newfd);
  }
  close(sockfd);
}

void* wrap_cloop (void* args) { 
  int sfd = *((int*)args);
  cliloop(sfd);
  write(sfd, "GET OUT\n", 8);
  shutdown(sfd, SHUT_RDWR);
  printf("Client disconnect.\n");
  return 0;
}

char* keygen (int size) {
  char* k = malloc(size);
  int i;
  for (i = 0; i < size; i++) k[i] = rand() & 0xff;
  return k;
}

void cliloop (int sockfd) {
  // authenticate, send prompt
  char buffer[257];
  char* authkey;
  usr_t* usrobj;
  int len, aklen;
  memset(buffer, 0, 257);
  if (write(sockfd, "GIVE ME A COOKIE", 17) < 0) return;
  if ((len = waitformsg(sockfd, buffer, 256, 7)) < 0) return;
  if (strncmp(buffer, "EXISTS ", 7) == 0) {
    usrobj = table_lookup(&buffer[7]);
    if (usrobj == 0) return;
    int smax = usrobj->key.size - 5;
    authkey = keygen(smax);
    aklen = smax;
    uchar* encd = rsa_encrypt(usrobj->key, authkey, smax);
    if (write(sockfd, encd, smax+5) < 0) return;
    free(encd);
    memset(buffer, 0, 257);
    if ((len = waitformsg(sockfd, buffer, 256, aklen)) < 0) return;
    if (strncmp(authkey, buffer, len) != 0) return;
    if (write(sockfd, "OK", 2) < 0) return;
  } else if (strncmp(buffer, "CREATE ", 7) == 0) {
    if (table_lookup(&buffer[7]) != 0) return;
    usrobj = malloc(sizeof(usr_t));
    usrobj->uname = strdup(&buffer[7]);
    usrobj->msgs = 0;
    if (write(sockfd, "SEND", 4) < 0) return;
    memset(buffer, 0, 257);
    if ((len = waitformsg(sockfd, buffer, 256, 256)) < 0) return;
    if (write(sockfd, "GOT", 3) < 0) return;
    char pEbuffer[17];
    memset(pEbuffer, 0, 17);
    if ((len = waitformsg(sockfd, pEbuffer, 16, 16)) < 0) return;
    usrobj->key = rsa_parse_key(buffer, 256, pEbuffer, 16);
    table_put(usrobj->uname, usrobj);
    aklen = usrobj->key.size - 5;
    authkey = keygen(aklen);
    int i;
    unsigned char* encd = rsa_encrypt(usrobj->key, authkey, aklen);
    if (write(sockfd, encd, aklen+5) < 0) return;
    free(encd);
    if ((len = waitformsg(sockfd, buffer, 256, aklen)) < 0) return;
    if (strncmp(buffer, authkey, aklen) != 0) return;
    if (write(sockfd, "OK", 2) < 0) return;
  } else return; // returning ends the connection
  
  // deliver unread messages and start protocol
  memset(buffer, 0, 257);
  if ((len = waitformsg(sockfd, buffer, 256, 5)) < 0) return;
  if (strncmp(buffer, "READY", 5) != 0) return;
  memset(buffer, 0, 257);

  while (usrobj->msgs != 0) {
    msg_t* msg = (msg_t*) usrobj->msgs->object;
    dispatch_msg(msg, sockfd, authkey, aklen);
    free(msg->sender);
    free(msg->msg);
    free(msg);
    link_t* tmp = usrobj->msgs->next;
    free(usrobj->msgs);
    usrobj->msgs = tmp;
  }

  
  fcntl(sockfd, F_SETFL, O_NONBLOCK);
  int pos = 0;
  while (1) {
    len = read(sockfd, &buffer[pos], 256-pos);
    if (len < 0)  {
      if (errno == EAGAIN || errno == EWOULDBLOCK) len = 0;
      else return;
    }
    pos += len;
    if (pos >= 3) {
      if (strncmp(buffer, "DIE", 3) == 0) {printf("ded\n"); return;}
    }
    if (pos >= 8) {
      bigvig_decrypt(buffer, len, authkey, aklen);
      int usrlen = ntohl(*((int*)buffer));
      if (usrlen+8 > 256) return;
      char uname[usrlen+1];
      strncpy(uname, &buffer[4], usrlen);
      uname[usrlen] = 0;
      int msglen = ntohl(*((int*)&buffer[4+usrlen]));
      if (msglen+usrlen+8 > 256) return;
      char* msg = malloc(msglen+1);
      strncpy(msg, &buffer[8+usrlen], msglen);
      msg[msglen] = 0;
      addmsg(uname, strdup(usrobj->uname), msg);
      pos = 0;
    }
    while (usrobj->msgs != 0) {
      msg_t* msg = (msg_t*) usrobj->msgs->object;
      dispatch_msg(msg, sockfd, authkey, aklen);
      free(msg->sender);
      free(msg->msg);
      free(msg);
      link_t* tmp = usrobj->msgs->next;
      free(usrobj->msgs);
      usrobj->msgs = tmp;
    }
  }
}

int waitformsg (int sockfd, char* buff, int len, int waitfor) {
  // socket was failing to block, here's a workaround
  int n = 0, pos = 0;
  while (pos < waitfor && n >= 0) {
    n = read(sockfd, &buff[pos], len-pos);
    pos += n;
    if (pos >= 3) {
      if (strncmp(buff, "DIE", 3) == 0) {printf("Ded\n"); return -1;} // client disconnect
    }
  }
  return pos;
}

void addmsg (char* to, char* from, char* msg) {
  usr_t* guestobj = table_lookup(to);
  if (guestobj == 0) return;
  msg_t* mesg = malloc(sizeof(msg_t));
  mesg->sender = from;
  mesg->msg = msg;
  if (guestobj->msgs == 0) {
    guestobj->msgs = calloc(1, sizeof(link_t));
    guestobj->msgs->object = mesg;
    return;
  }
  while (guestobj->msgs->next != 0);
  guestobj->msgs->next = calloc(1, sizeof(link_t));
  guestobj->msgs->next->object = mesg;
}

void dispatch_msg (msg_t* msg, int sockfd, unsigned char* authkey, int aklen) {
  char buffer[512];
  int slen = strlen(msg->sender);
  *((int*)buffer) = htonl(slen);
  if (slen+4 > 512) return;
  strncpy(&buffer[4], msg->sender, slen);
  int mlen = strlen(msg->msg);
  *((int*)(&buffer[4+slen])) = htonl(mlen);
  if (slen+mlen+4 > 512) return;
  strncpy(&buffer[8+slen], msg->msg, mlen);
  bigvig_encrypt(buffer, 512, authkey, aklen);
  if (write(sockfd, buffer, 512) < 0) return;
}

void errorwarn (char* err) {
  fprintf(stderr, "%s\n", err);
}

void errorquit (char* err) {
  errorwarn(err);
  exit(1);
}

void seq () {
  errorquit("Socket error. Exiting.");
}
