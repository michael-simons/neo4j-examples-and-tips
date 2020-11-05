package io.helidon.examples.quickstart.se.api;

import io.helidon.common.GenericType;
import io.helidon.common.http.DataChunk;
import io.helidon.common.http.MediaType;
import io.helidon.common.reactive.Multi;
import io.helidon.examples.quickstart.se.domain.Movie;
import io.helidon.media.common.MessageBodyStreamWriter;
import io.helidon.media.common.MessageBodyWriterContext;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Flow;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

/**
 * Unrelated to Neo4j, streaming JSONB is not yet supported in Helidon.
 * See https://github.com/oracle/helidon/issues/1794
 */
public final class MovieBodyStreamWriter implements MessageBodyStreamWriter<Movie> {

	private static final MediaType APPLICATION_X_NDJSON = MediaType.create("application", "x-ndjson");
	private static final Jsonb JSON_FACTORY = JsonbBuilder.create();
	private static final byte[] NL = "\n".getBytes(StandardCharsets.UTF_8);

	@Override
	public PredicateResult accept(GenericType<?> type, MessageBodyWriterContext context) {
		return PredicateResult.supports(Movie.class, type);
	}

	@Override
	public Flow.Publisher<DataChunk> write(Flow.Publisher<? extends Movie> publisher,
		GenericType<? extends Movie> type, MessageBodyWriterContext context) {

		context.contentType(APPLICATION_X_NDJSON);
		return Multi.defer(() -> publisher)
			.flatMap(m -> Multi.just(
				DataChunk.create(JSON_FACTORY.toJson(m).getBytes(StandardCharsets.UTF_8)),
				DataChunk.create(NL))
			);
	}
}
