all:
	gcc *.c -l gmp -pthread -o server
tgz:
	tar -czf server.tgz *.c *.h makefile
clean:
	rm -f *.o *.tgz server
