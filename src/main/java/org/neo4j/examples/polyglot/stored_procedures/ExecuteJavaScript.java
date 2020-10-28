package org.neo4j.examples.polyglot.stored_procedures;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

public class ExecuteJavaScript {

	@Context
	public GraphDatabaseService db;

	@Procedure(value = "scripts.execute")
	@Description("scripts.execute(scriptUrl) - Executes the script at the given URL if accessible. Throws things otherwise.")
	public void join(@Name("scriptUrl") String scriptUrl) throws IOException {

		var uri = Files.readString(Path.of(URI.create(scriptUrl)));
		try (var context = org.graalvm.polyglot.Context.newBuilder().allowAllAccess(true).build()) {
			var bindings = context.getPolyglotBindings();
			bindings.putMember("db", db);

			context.eval("js", uri);
		}
	}
}