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
