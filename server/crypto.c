#include "crypto.h"

pubkey_t rsa_parse_key (uchar* mod, int mlen, uchar* pE, int plen) {
  pubkey_t key;
  mpz_init(key.modulus);
  mpz_init(key.pE);
  mpz_import(key.modulus, mlen, 1, sizeof(uchar), 1, 0, mod);
  mpz_import(key.pE, plen, 1, sizeof(uchar), 1, 0, pE);
  key.size = mlen;
  return key;
}

uchar* rsa_pad (uchar* data, int length, int blocksize) {
  int i;
  uchar* buffer = malloc(blocksize);
  for (i = length+3; i >= 4; i--) {
    buffer[i] = data[i-4];
  }
  *((int*)buffer) = htonl(length);
  for (i = length+4; i < blocksize; i++) buffer[i] = rand()&0xff;
  return buffer;
}

uchar* rsa_encrypt (pubkey_t key, uchar* data, int dlen) {
  uchar* buff = rsa_pad(data, dlen, key.size-1);
  mpz_t msg, cipher;
  mpz_init(msg);
  mpz_init(cipher);
  mpz_import(msg, key.size-1, 1, sizeof(uchar), 1, 0, buff);
  mpz_powm(cipher, msg, key.pE, key.modulus);
  mpz_export(buff, 0, 1, sizeof(uchar), 1, 0, cipher);
  return buff;
}

void encrypt (uchar* data, int dlen, uchar* key, int klen) {
  int i;
  for (i = 0; i < dlen; i++) {
    data[i] = data[i] ^ key[i%klen];
  }
}

void decrypt (uchar* d, int dl, uchar* k, int kl) {
  encrypt(d, dl, k, kl);
}
