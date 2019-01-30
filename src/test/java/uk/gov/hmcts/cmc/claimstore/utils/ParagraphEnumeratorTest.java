package uk.gov.hmcts.cmc.claimstore.utils;

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ParagraphEnumeratorTest {
    private final static String PARAGRAPH_1 = "Lorem ipsum dolor sit amet";
    private final static String PARAGRAPH_2 = "Consectetur adipiscing alit";

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void testSplitNullInput() {
        ParagraphEnumerator.split(null);
    }

    @Test
    public void testSplitEmptyInput() {
        List<String> result = ParagraphEnumerator.split("");
        assertThat(result)
            .isNotNull()
            .isEmpty();
    }

    @Test
    public void testSplitSingleParagraph() {
        List<String> result = ParagraphEnumerator.split(PARAGRAPH_1);
        assertThat(result)
            .containsExactly(PARAGRAPH_1);
    }

    @Test
    public void testSplitMultiParagraphByCarriageReturnOnly() {
        List<String> result = ParagraphEnumerator.split(String.format("%s\r%s", PARAGRAPH_1, PARAGRAPH_2));
        assertThat(result)
            .containsSequence(PARAGRAPH_1, PARAGRAPH_2);
    }

    @Test
    public void testSplitMultiParagraphByLineFeedOnly() {
        List<String> result = ParagraphEnumerator.split(String.format("%s\n%s", PARAGRAPH_1, PARAGRAPH_2));
        assertThat(result)
            .containsSequence(PARAGRAPH_1, PARAGRAPH_2);
    }

    @Test
    public void testSplitMultiParagraphByCarriageReturnLineFeed() {
        List<String> result = ParagraphEnumerator.split(String.format("%s\r\n%s", PARAGRAPH_1, PARAGRAPH_2));
        assertThat(result)
            .containsSequence(PARAGRAPH_1, PARAGRAPH_2);
    }

    @Test
    public void testTrailingSeparatorsIgnored() {
        List<String> result = ParagraphEnumerator.split(String.format("%s\r\n%s\r\n", PARAGRAPH_1, PARAGRAPH_2));
        assertThat(result)
            .containsSequence(PARAGRAPH_1, PARAGRAPH_2);
    }

    @Test
    public void testLeadingSeparatorsIgnored() {
        List<String> result = ParagraphEnumerator.split(String.format("\r\n%s\r\n%s", PARAGRAPH_1, PARAGRAPH_2));
        assertThat(result)
            .containsSequence(PARAGRAPH_1, PARAGRAPH_2);
    }

    @Test
    public void testEmptyParagraphIgnored() {
        List<String> result = ParagraphEnumerator.split(String.format("%s\r\n \r\n%s", PARAGRAPH_1, PARAGRAPH_2));
        assertThat(result)
            .containsSequence(PARAGRAPH_1, PARAGRAPH_2);
    }
}
