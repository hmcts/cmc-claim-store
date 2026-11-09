package uk.gov.hmcts.cmc.claimstore.repositories.elastic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public static final class BoolQuery implements SearchClause {

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
            json.fieldName("adjust_pure_negative");
            json.raw("true");
            json.comma();
            json.fieldName("boost");
            json.raw("1.0");
            json.endObject();
            json.endObject();
        }

        @Override
        public String toString() {
            return SearchJson.render(this);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof BoolQuery boolQuery)) {
                return false;
            }
            return must.equals(boolQuery.must) && mustNot.equals(boolQuery.mustNot);
        }

        @Override
        public int hashCode() {
            return Objects.hash(must, mustNot);
        }
    }

    public static final class TermQuery implements SearchClause {

        private final String field;
        private final Object value;

        private TermQuery(String field, Object value) {
            this.field = Objects.requireNonNull(field);
            this.value = value;
        }

        @Override
        public void write(SearchJson json) {
            json.startObject();
            json.fieldName("term");
            json.startObject();
            json.fieldName(field);
            json.startObject();
            json.fieldName("value");
            json.value(value);
            json.comma();
            json.fieldName("boost");
            json.raw("1.0");
            json.endObject();
            json.endObject();
            json.endObject();
        }

        @Override
        public String toString() {
            return SearchJson.render(this);
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

    public static final class MatchQuery implements SearchClause {

        private final String field;
        private final Object value;

        private MatchQuery(String field, Object value) {
            this.field = Objects.requireNonNull(field);
            this.value = value;
        }

        @Override
        public void write(SearchJson json) {
            json.startObject();
            json.fieldName("match");
            json.startObject();
            json.fieldName(field);
            json.startObject();
            json.fieldName("query");
            json.value(value);
            json.comma();
            json.fieldName("operator");
            json.string("OR");
            json.comma();
            json.fieldName("prefix_length");
            json.raw("0");
            json.comma();
            json.fieldName("max_expansions");
            json.raw("50");
            json.comma();
            json.fieldName("fuzzy_transpositions");
            json.raw("true");
            json.comma();
            json.fieldName("lenient");
            json.raw("false");
            json.comma();
            json.fieldName("zero_terms_query");
            json.string("NONE");
            json.comma();
            json.fieldName("auto_generate_synonyms_phrase_query");
            json.raw("true");
            json.comma();
            json.fieldName("boost");
            json.raw("1.0");
            json.endObject();
            json.endObject();
            json.endObject();
        }

        @Override
        public String toString() {
            return SearchJson.render(this);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof MatchQuery matchQuery)) {
                return false;
            }
            return field.equals(matchQuery.field) && Objects.equals(value, matchQuery.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(field, value);
        }
    }

    public static final class RangeQuery implements SearchClause {

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
            json.startObject();
            json.fieldName("range");
            json.startObject();
            json.fieldName(field);
            json.startObject();
            json.fieldName("from");
            json.value(from);
            json.comma();
            json.fieldName("to");
            json.value(to);
            json.comma();
            json.fieldName("include_lower");
            json.raw(includeLower ? "true" : "false");
            json.comma();
            json.fieldName("include_upper");
            json.raw(includeUpper ? "true" : "false");
            json.comma();
            json.fieldName("boost");
            json.raw("1.0");
            json.endObject();
            json.endObject();
            json.endObject();
        }

        @Override
        public String toString() {
            return SearchJson.render(this);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof RangeQuery rangeQuery)) {
                return false;
            }
            return field.equals(rangeQuery.field)
                && Objects.equals(from, rangeQuery.from)
                && Objects.equals(to, rangeQuery.to)
                && includeLower == rangeQuery.includeLower
                && includeUpper == rangeQuery.includeUpper;
        }

        @Override
        public int hashCode() {
            return Objects.hash(field, from, to, includeLower, includeUpper);
        }
    }

    public static final class ExistsQuery implements SearchClause {

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
            json.fieldName("boost");
            json.raw("1.0");
            json.endObject();
            json.endObject();
        }

        @Override
        public String toString() {
            return SearchJson.render(this);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof ExistsQuery existsQuery)) {
                return false;
            }
            return field.equals(existsQuery.field);
        }

        @Override
        public int hashCode() {
            return field.hashCode();
        }
    }

    public static final class MatchAllQuery implements SearchClause {

        private MatchAllQuery() {
        }

        @Override
        public void write(SearchJson json) {
            json.startObject();
            json.fieldName("match_all");
            json.startObject();
            json.fieldName("boost");
            json.raw("1.0");
            json.endObject();
            json.endObject();
        }

        @Override
        public String toString() {
            return SearchJson.render(this);
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof MatchAllQuery;
        }

        @Override
        public int hashCode() {
            return MatchAllQuery.class.hashCode();
        }
    }
}
