package uk.gov.hmcts.cmc.claimstore.repositories.elastic;

import org.elasticsearch.index.query.QueryBuilder;

import java.util.Objects;

public class Query {

    private final QueryBuilder queryBuilder;
    private final int pageSize;

    public Query(QueryBuilder queryBuilder, int pageSize) {
        Objects.requireNonNull(queryBuilder, "QueryBuilder cannot be null in search");
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size cant be less than 1");
        }
        this.queryBuilder = queryBuilder;
        this.pageSize = pageSize;
    }

    @Override
    public String toString() {
        return "{"
            + "\"size\": " + pageSize + ","
            + "\"query\": "
            + queryBuilder.toString()
            + '}';
    }
}
