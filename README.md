### Debug params

```shell
-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -Xnoagent -Djava.compiler=NONE
```  

### Example commands

```shell
echo -n "AAAAAA" | java -jar -jar ./target/ejigma-0.0.3-jar-with-dependencies.jar - -rIII
-rII -rI -eENIGMA_I -lB
```  