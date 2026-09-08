/* Resumo operacional da carga inicial, útil para logs e suporte. */
package br.com.restaurant.edge.service;

import java.time.Instant;

public record SnapshotImportResult(
        Instant snapshotGeneratedAt,
        int received,
        int inserted,
        int updated,
        int ignoredBecauseLocalVersionWasEqualOrNewer
) {
}
