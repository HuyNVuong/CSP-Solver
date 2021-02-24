all:
	mkdir -p bin
	find -name "*.java" > sources.txt
	javac --release 15 --enable-preview -d bin -sourcepath src @sources.txt
	jar -J-Xmx256m cfm csp.jar src/main/java/META-INF/MANIFEST.MF -C bin .
clean:
	rm -rf bin/* csp.jar
