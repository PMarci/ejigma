### Build and install somewhere already in path

```shell
mvn clean package
./build
install ./ejigma /usr/local/bin
```  

### Example commands

```shell
echo -n "AAAAAA" | ejigma - -rIII -rII -rI -eENIGMA_I -lB
```  

### Debug params

```shell
-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -Xnoagent -Djava.compiler=NONE
# alternatively
mvnDebug clean compile exec:java -Dmaven.surefire.debug="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005 -Xnoagent -Djava.compiler=NONE"
```  
