## Build and run

This is a super insecure Neo4j extension that provides one stored procedure:

| name              | signature                                     | description                                                                                               |
|-------------------|-------------------------------------------------|-----------------------------------------------------------------------------------------------------------|
| `scripts.execute` | `scripts.execute(scriptUrl :: STRING?) :: VOID` | scripts.execute(scriptUrl) - Executes the script at the given URL if accessible. Throws things otherwise. |

To run this in the most efficient way, we expect Neo4j to be run on GraalVM for Java 11 and have the necessary dependencies
in the provided scope

```
<dependency>
    <groupId>org.graalvm.sdk</groupId>
    <artifactId>graal-sdk</artifactId>
    <version>${graalvm.version}</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>org.graalvm.js</groupId>
    <artifactId>js</artifactId>
    <version>${graalvm.version}</version>
    <scope>provided</scope>
</dependency>
```

The extension itself can be build on a plain JDK 11:

```
mvn clean package
```

is enough.

A script like this

```
const collectors = Java.type('java.util.stream.Collectors')

function findConnections(to) { 
    const query = `
        MATCH (:Person {name:$name})-[:ACTED_IN]->(m)<-[:ACTED_IN]-(coActor)
        RETURN DISTINCT coActor`
    
    const db = Polyglot.import("db")
    const tx = db.beginTx()
    
    const names = tx.execute(query, {name: to})
        .stream()
        .map(r => r.get('coActor').getProperty('name'))
        .collect(collectors.toList())    
    tx.close()
    
    return names
} 

names = findConnections('Tom Hanks')
names.forEach(name => console.log(name))
```

Can be run as 

```
CALL scripts.execute('file:///path/to/script.js')
```

The important piece in the listing above is the following access to a function provided by GraalVMs polyglot context: 

```
const db = Polyglot.import("db")
```

it retrieves the Neo4j `org.neo4j.graphdb.GraphDatabaseService`.

This is a proxy to the Java object of the given class and can be used to execute Cypher or find Nodes and relationships directly.
Let your imagination go wild what you can do without apart from printing out stuff.


