package uk.gov.hmcts.cmc.claimstore.repositories.elastic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Builds the small set of CCD search clauses this service sends. The JSON matches
 * Elasticsearch 7.17 {@code QueryBuilder#toString()} for those clauses.
 */
public final class Queries {

    private Queries() {
    }

    public static BoolQuery boolQuery() {
        return new BoolQuery();
    }

    public static TermQuery termQuery(String field, Object value) {
        return new TermQuery(field, value);
    }

    public static MatchQuery matchQuery(String field, Object value) {
        return new MatchQuery(field, value);
    }

    public static RangeQuery rangeQuery(String field) {
        return new RangeQuery(field);
    }

    public static ExistsQuery existsQuery(String field) {
        return new ExistsQuery(field);
    }

    public static MatchAllQuery matchAllQuery() {
        return new MatchAllQuery();
    }

    private static void writeBoosted(SearchJson json, String type, String field, Consumer<SearchJson> body) {
        json.startObject();
        json.fieldName(type);
        json.startObject();
        json.fieldName(field);
        json.startObject();
        body.accept(json);
        json.comma();
        json.boost();
        json.endObject();
        json.endObject();
        json.endObject();
    }

    abstract static class JsonClause implements SearchClause {

        @Override
        public final String toString() {
            return SearchJson.render(this);
        }
    }

    public static final class BoolQuery extends JsonClause {

        private final List<SearchClause> must = new ArrayList<>();
        private final List<SearchClause> mustNot = new ArrayList<>();

        public BoolQuery must(SearchClause clause) {
            must.add(Objects.requireNonNull(clause));
            return this;
        }

        public BoolQuery mustNot(SearchClause clause) {
            mustNot.add(Objects.requireNonNull(clause));
            return this;
        }

        @Override
        public void write(SearchJson json) {
            json.startObject();
            json.fieldName("bool");
            json.startObject();
            writeClauses(json);
            json.fieldName("adjust_pure_negative");
            json.bool(true);
            json.comma();
            json.boost();
            json.endObject();
            json.endObject();
        }

        private void writeClauses(SearchJson json) {
            boolean wroteClause = false;
            if (!must.isEmpty()) {
                json.fieldName("must");
                json.array(must);
                wroteClause = true;
            }
            if (!mustNot.isEmpty()) {
                if (wroteClause) {
                    json.comma();
                }
                json.fieldName("must_not");
                json.array(mustNot);
                wroteClause = true;
            }
            if (wroteClause) {
                json.comma();
            }
        }
    }

    public static final class TermQuery extends JsonClause {

        private final String field;
        private final Object value;

        private TermQuery(String field, Object value) {
            this.field = Objects.requireNonNull(field);
            this.value = value;
        }

        @Override
        public void write(SearchJson json) {
            writeBoosted(json, "term", field, inner -> {
                inner.fieldName("value");
                inner.value(value);
            });
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof TermQuery termQuery)) {
                return false;
            }
            return field.equals(termQuery.field) && Objects.equals(value, termQuery.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(field, value);
        }
    }

    public static final class MatchQuery extends JsonClause {

        private final String field;
        private final Object value;

        private MatchQuery(String field, Object value) {
            this.field = Objects.requireNonNull(field);
            this.value = value;
        }

        @Override
        public void write(SearchJson json) {
            writeBoosted(json, "match", field, inner -> {
                inner.fieldName("query");
                inner.value(value);
                inner.comma();
                inner.fieldName("operator");
                inner.string("OR");
                inner.comma();
                inner.fieldName("prefix_length");
                inner.raw("0");
                inner.comma();
                inner.fieldName("max_expansions");
                inner.raw("50");
                inner.comma();
                inner.fieldName("fuzzy_transpositions");
                inner.bool(true);
                inner.comma();
                inner.fieldName("lenient");
                inner.bool(false);
                inner.comma();
                inner.fieldName("zero_terms_query");
                inner.string("NONE");
                inner.comma();
                inner.fieldName("auto_generate_synonyms_phrase_query");
                inner.bool(true);
            });
        }
    }

    public static final class RangeQuery extends JsonClause {

        private final String field;
        private Object from;
        private Object to;
        private boolean includeLower = true;
        private boolean includeUpper = true;

        private RangeQuery(String field) {
            this.field = Objects.requireNonNull(field);
        }

        public RangeQuery from(Object lower, boolean includeLowerBound) {
            this.from = lower;
            this.includeLower = includeLowerBound;
            return this;
        }

        public RangeQuery to(Object upper, boolean includeUpperBound) {
            this.to = upper;
            this.includeUpper = includeUpperBound;
            return this;
        }

        public RangeQuery gte(Object lower) {
            return from(lower, true);
        }

        public RangeQuery lte(Object upper) {
            return to(upper, true);
        }

        @Override
        public void write(SearchJson json) {
            writeBoosted(json, "range", field, inner -> {
                inner.fieldName("from");
                inner.value(from);
                inner.comma();
                inner.fieldName("to");
                inner.value(to);
                inner.comma();
                inner.fieldName("include_lower");
                inner.bool(includeLower);
                inner.comma();
                inner.fieldName("include_upper");
                inner.bool(includeUpper);
            });
        }
    }

    public static final class ExistsQuery extends JsonClause {

        private final String field;

        private ExistsQuery(String field) {
            this.field = Objects.requireNonNull(field);
        }

        @Override
        public void write(SearchJson json) {
            json.startObject();
            json.fieldName("exists");
            json.startObject();
            json.fieldName("field");
            json.string(field);
            json.comma();
            json.boost();
            json.endObject();
            json.endObject();
        }
    }

    public static final class MatchAllQuery extends JsonClause {

        private MatchAllQuery() {
        }

        @Override
        public void write(SearchJson json) {
            json.startObject();
            json.fieldName("match_all");
            json.startObject();
            json.boost();
            json.endObject();
            json.endObject();
        }
    }
}
