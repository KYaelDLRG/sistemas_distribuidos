# ============================================================================
# Makefile - Cliente HTTP Asíncrono y Servidores Distribuidos
# ============================================================================

# Detección de ejecutables de Java (Amazon Corretto JDK 21 en Windows o JDK por defecto en Linux)
CORRETTO_BIN = C:/Program Files/Amazon Corretto/jdk21.0.4_7/bin

ifneq ("$(wildcard $(CORRETTO_BIN)/javac.exe)","")
    JAVAC = "$(CORRETTO_BIN)/javac.exe"
    JAVA  = "$(CORRETTO_BIN)/java.exe"
    JAR   = "$(CORRETTO_BIN)/jar.exe"
else
    JAVAC = javac
    JAVA  = java
    JAR   = jar
endif

.PHONY: all compile jar run run-server-8081 run-server-8082 clean

all: compile jar

compile:
	$(JAVAC) networking/WebClient.java Aggregator.java Application.java ServidorHttp.java

jar: compile
	$(JAR) -cfe Application.jar Application Application.class Aggregator.class networking/WebClient.class

run: jar
	$(JAVA) -cp Application.jar Application

run-server-8081: compile
	$(JAVA) ServidorHttp 8081

run-server-8082: compile
	$(JAVA) ServidorHttp 8082

clean:
	rm -f *.class networking/*.class Application.jar 2>/dev/null || del /Q *.class networking\*.class Application.jar 2>nul || exit 0
