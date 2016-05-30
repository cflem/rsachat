#ifndef __CRYPTO_H
#define __CRYPTO_H 1
#include <gmp.h>
#include <stdlib.h>

typedef struct {
  mpz_t modulus;
  mpz_t pE;
  int size; // in bytes
} pubkey_t;
typedef unsigned char uchar;

pubkey_t rsa_parse_key (uchar* modulus, int mlen, uchar* pE, int plen);
uchar* rsa_encrypt (pubkey_t key, uchar* data, int length);
uchar* rsa_pad (uchar* data, int length, int padto);
void encrypt (uchar* data, int dlen, uchar* key, int klen);
void decrypt (uchar* data, int dlen, uchar* key, int klen);
#endif
