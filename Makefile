all: src/yak/server/Proto.java test

src/yak/server/Proto.java : protocol.txt   compile-proto.tcl
	tclsh compile-proto.tcl  yak.server   < protocol.txt   > src/yak/server/Proto.java

test:
	java -classpath ./bin/classes/  yak.server.Tests
