package uk.gov.hmcts.cmc.claimstore.repositories.elastic;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class QueryTest {

    @Test
    public void queryBuilderShouldNotBeNull() {
        Assertions.assertThatThrownBy(() -> new Query(null, 10, 0))
            .hasMessage("QueryBuilder cannot be null in search");
    }

    @Test
    public void pageSizeShouldNotBeLessThanOne() {
        Assertions.assertThatThrownBy(() -> new Query(Queries.matchAllQuery(), 0, 0))
            .hasMessage("Page size cant be less than 1");
    }

    @Test
    public void queryToStringToMatch() {
        final String matchAllQueryString = "{"
            + "\"size\": 10,"
            + "\"from\": " + 0 + ","
            + "\"sort\": " + "[\n"
            + "    { \"created_date\" : {\"order\" : \"desc\"}}\n"
            + "    ]" + ","
            + "\"query\": {\n"
            + "  \"match_all\" : {\n"
            + "    \"boost\" : 1.0\n"
            + "  }\n"
            + "}}";

        Query matchAll = new Query(Queries.matchAllQuery(), 10, 0);
        assertEquals(matchAllQueryString, matchAll.toString());
    }

    @Test
    public void queryToStringIncludesPageOffset() {
        final String pagedQuery = "{"
            + "\"size\": 10,"
            + "\"from\": " + 20 + ","
            + "\"sort\": " + "[\n"
            + "    { \"created_date\" : {\"order\" : \"desc\"}}\n"
            + "    ]" + ","
            + "\"query\": {\n"
            + "  \"match_all\" : {\n"
            + "    \"boost\" : 1.0\n"
            + "  }\n"
            + "}}";

        Query page = new Query(Queries.matchAllQuery(), 10, 20);
        assertEquals(pagedQuery, page.toString());
    }

    @Test
    public void termQueryEscapesControlCharacters() {
        String json = Queries.termQuery("a\"b", "x\\y\n\r\tz").toString();

        assertEquals("{\n"
            + "  \"term\" : {\n"
            + "    \"a\\\"b\" : {\n"
            + "      \"value\" : \"x\\\\y\\n\\r\\tz\",\n"
            + "      \"boost\" : 1.0\n"
            + "    }\n"
            + "  }\n"
            + "}", json);
    }

    @Test
    public void rangeQueryWritesExclusiveBoundsAndTrimmedNanos() {
        LocalDateTime lower = LocalDateTime.of(2019, 7, 7, 1, 2, 3, 1_000_000);
        LocalDateTime upper = LocalDateTime.of(2019, 7, 7, 4, 5, 6, 120_000_000);

        String json = Queries.rangeQuery("when").from(lower, false).to(upper, false).toString();

        assertEquals("{\n"
            + "  \"range\" : {\n"
            + "    \"when\" : {\n"
            + "      \"from\" : \"2019-07-07T01:02:03.001Z\",\n"
            + "      \"to\" : \"2019-07-07T04:05:06.120Z\",\n"
            + "      \"include_lower\" : false,\n"
            + "      \"include_upper\" : false,\n"
            + "      \"boost\" : 1.0\n"
            + "    }\n"
            + "  }\n"
            + "}", json);
    }

    @Test
    public void termQueryEqualsMatchesFieldAndValue() {
        LocalDate date = LocalDate.of(2019, 10, 15);
        Queries.TermQuery query = Queries.termQuery("date", date);

        assertEquals(query, query);
        assertEquals(query, Queries.termQuery("date", date));
        assertEquals(query.hashCode(), Queries.termQuery("date", date).hashCode());
        assertNotEquals(query, Queries.termQuery("date", date.plusDays(1)));
        assertNotEquals(query, Queries.matchAllQuery());
    }

    @Test
    public void boolQueryRejectsNullClause() {
        Assertions.assertThatThrownBy(() -> Queries.boolQuery().must(null))
            .isInstanceOf(NullPointerException.class);
        Assertions.assertThatThrownBy(() -> Queries.boolQuery().mustNot(null))
            .isInstanceOf(NullPointerException.class);
    }

}
