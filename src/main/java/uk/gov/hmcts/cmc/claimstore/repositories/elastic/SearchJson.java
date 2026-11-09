package uk.gov.hmcts.cmc.claimstore.repositories.elastic;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Pretty-prints JSON in the form produced by Elasticsearch 7.17 XContent.
 */
final class SearchJson {

    private final StringBuilder out = new StringBuilder();
    private int indent;

    static String render(SearchClause clause) {
        SearchJson json = new SearchJson();
        clause.write(json);
        return json.toString();
    }

    void startObject() {
        out.append('{');
        indent++;
        out.append('\n');
    }

    void endObject() {
        out.append('\n');
        indent--;
        pad();
        out.append('}');
    }

    void fieldName(String name) {
        pad();
        out.append('"').append(escape(name)).append("\" : ");
    }

    void raw(String json) {
        out.append(json);
    }

    void string(String value) {
        out.append('"').append(escape(value)).append('"');
    }

    void value(Object value) {
        if (value == null) {
            out.append("null");
            return;
        }
        string(text(value));
    }

    void array(List<SearchClause> clauses) {
        out.append('[');
        indent++;
        out.append('\n');
        for (int index = 0; index < clauses.size(); index++) {
            if (index > 0) {
                out.append(",\n");
            }
            pad();
            clauses.get(index).write(this);
        }
        out.append('\n');
        indent--;
        pad();
        out.append(']');
    }

    void comma() {
        out.append(",\n");
    }

    @Override
    public String toString() {
        return out.toString();
    }

    private void pad() {
        out.append("  ".repeat(indent));
    }

    private static String text(Object value) {
        if (value instanceof LocalDateTime dateTime) {
            return formatDateTime(dateTime);
        }
        return value.toString();
    }

    /**
     * Matches Elasticsearch {@code strict_date_optional_time_nanos} for a {@link LocalDateTime}
     * treated as UTC: at least three fractional digits, trailing zeros removed after that, then {@code Z}.
     */
    private static String formatDateTime(LocalDateTime dateTime) {
        return String.format(
            "%04d-%02d-%02dT%02d:%02d:%02d.%sZ",
            dateTime.getYear(),
            dateTime.getMonthValue(),
            dateTime.getDayOfMonth(),
            dateTime.getHour(),
            dateTime.getMinute(),
            dateTime.getSecond(),
            fractionalSeconds(dateTime.getNano())
        );
    }

    private static String fractionalSeconds(int nano) {
        if (nano == 0) {
            return "000";
        }
        String digits = String.format("%09d", nano);
        int end = digits.length();
        while (end > 3 && digits.charAt(end - 1) == '0') {
            end--;
        }
        return digits.substring(0, end);
    }

    private static String escape(String value) {
        StringBuilder escaped = new StringBuilder(value.length());
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            switch (current) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    escaped.append(current);
                    break;
            }
        }
        return escaped.toString();
    }
}
