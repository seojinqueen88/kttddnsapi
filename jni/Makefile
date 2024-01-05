MAKEFLAGS += --no-builtin-rules
.suffixes:
.PHONY: clean

JAVA_HOME ?= $(shell readlink -e "$$(dirname "$$(readlink -e "$$(which javac)")")"/..)
JAVAC = javac
JAVAH = javah

CFLAGS  += -Iinclude -I"$(JAVA_HOME)/include" -I"$(JAVA_HOME)/include/linux" \
           -I. -fPIC
LDFLAGS += -shared -LlibcmakeInstantPasswd -lcmakeInstantPasswd

OBJS = $(patsubst src/%.c,build/%.o,$(wildcard src/*.c))

# By default:
#  * Compile libexample
#  * Compile the Java class
#  * Generate the associated header file
#  * Compile the native implementation
all: cmakeInstantPasswd/cmakeInstantPasswd.so # build/JNIExample.class build/libJNIExample.so

# Run the resulting program
#  LD_LIBRARY_PATH is adjusted so that the loader finds libexample.so.
#  This is needed because library is not installed on the system.
run: all
	LD_LIBRARY_PATH=libcmakeInstantPasswd java -classpath build -Djava.library.path=build JNIExample

# The header file is needed for the succesful compilation of the native code.
./c_encryption.o: ./com_kttddnsapi_util_InstancePW_GEN_UTIL.h

# Compile the example library.
cmakeInstantPasswd/cmakeInstantPasswd.so:
	$(MAKE) -C cmakeInstantPasswd

# Create a class file from Java source code.
build/%.class: src/%.java
	mkdir -p build
	$(JAVAC) -d $(dir $@) $<

# Generate a header file from a Java class.
include/%.h: build/%.class
	mkdir -p include
	$(JAVAH) -d $(dir $@) -classpath $(dir $<) $(notdir $(basename $<))

# Compile C code
build/%.o: src/%.c
	mkdir -p build
	$(CC) -c $(CFLAGS) -o $@ $<

# Link the library used by JNI
 cmakeInstantPasswd.so: $(OBJS)
	$(CC) $(LDFLAGS) -o $@ $(OBJS)

clean:
	$(MAKE) -C cmakeInstantPasswd $@
	-rm -rf build
	-rm -rf include