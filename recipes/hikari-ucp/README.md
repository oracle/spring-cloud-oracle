# Hikari to UCP Open Rewrite Recipe 

## Build the Open Rewrite Recipe

The repo is using maven. To build the recipe run the following command:

```shell
mvn install
```

## To use the Recipe

 In the repository you want to test your recipe against, update the pom.xml to include the following:

 ```xml
 <project>
    <build>
        <plugins>
            <plugin>
                <groupId>org.openrewrite.maven</groupId>
                <artifactId>rewrite-maven-plugin</artifactId>
                <version>6.2.2</version> <!-- Verify the version, March 2025 -->
                <configuration>
                    <activeRecipes>
                        <recipe>ConvertHikariToUCP </recipe>
                    </activeRecipes>
                </configuration>
                <dependencies>
                    <dependency>
                        <groupId>java.oracle.com.cloud.recipes</groupId>
                        <artifactId>hikariucp</artifactId>
                        <version>0.0.1-SNAPSHOT</version>
                    </dependency>
                </dependencies>
            </plugin>
        </plugins>
    </build>
</project>
```