package org.neo4j.examples.polyglot.stored_procedures;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;

public class ExecuteJavaScriptTest {

	private static Neo4j embeddedDatabaseServer;

	@BeforeAll
	static void initializeNeo4j() {
		embeddedDatabaseServer = Neo4jBuilders.newInProcessBuilder()
			.withDisabledServer()
			.withConfig(GraphDatabaseSettings.procedure_unrestricted, List.of("scripts.execute"))
			.withProcedure(ExecuteJavaScript.class)
			.withFixture(
				"CREATE (YouveGotMail:Movie {title:\"You've Got Mail\", released:1998, tagline:'At odds in life... in love on-line.'})\n"
					+ "CREATE (ParkerP:Person {name:'Parker Posey', born:1968})\n"
					+ "CREATE (DaveC:Person {name:'Dave Chappelle', born:1973})\n"
					+ "CREATE (SteveZ:Person {name:'Steve Zahn', born:1967})\n"
					+ "CREATE (TomH:Person {name:'Tom Hanks', born:1956})\n"
					+ "CREATE (NoraE:Person {name:'Nora Ephron', born:1941})\n"
					+ "CREATE (MegR:Person {name:'Meg Ryan', born:1961})\n"
					+ "CREATE (GregK:Person {name:'Greg Kinnear', born:1963})\n"
					+ "CREATE\n"
					+ "(TomH)-[:ACTED_IN {roles:['Joe Fox']}]->(YouveGotMail),\n"
					+ "(MegR)-[:ACTED_IN {roles:['Kathleen Kelly']}]->(YouveGotMail),\n"
					+ "(GregK)-[:ACTED_IN {roles:['Frank Navasky']}]->(YouveGotMail),\n"
					+ "(ParkerP)-[:ACTED_IN {roles:['Patricia Eden']}]->(YouveGotMail),\n"
					+ "(DaveC)-[:ACTED_IN {roles:['Kevin Jackson']}]->(YouveGotMail),\n"
					+ "(SteveZ)-[:ACTED_IN {roles:['George Pappas']}]->(YouveGotMail),\n"
					+ "(NoraE)-[:DIRECTED]->(YouveGotMail)")
			.build();
	}

	@Test
	void joinsStrings() {

		PrintStream realSysOut = System.out;
		ByteArrayOutputStream sysOutBuffer = new ByteArrayOutputStream();

		try {
			System.setOut(new PrintStream(sysOutBuffer));
			embeddedDatabaseServer
				.defaultDatabaseService()
				.executeTransactionally("CALL scripts.execute($scriptUrl)",
					Map.of("scriptUrl", getClass().getResource("/findConnections.js").toString()));
		} finally {
			System.setOut(realSysOut);
		}

		assertThat(sysOutBuffer.toString(StandardCharsets.UTF_8).lines())
			.containsExactlyInAnyOrder(
				"Parker Posey",
				"Greg Kinnear",
				"Dave Chappelle",
				"Steve Zahn",
				"Meg Ryan"
			);
	}
}