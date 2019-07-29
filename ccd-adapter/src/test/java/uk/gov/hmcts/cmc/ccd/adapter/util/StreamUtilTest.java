package uk.gov.hmcts.cmc.ccd.adapter.util;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import uk.gov.hmcts.cmc.ccd.adapter.util.StreamUtil;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class StreamUtilTest {

    @Test
    public void shouldReturnStreamIfCollectionIsNotEmpty() {
        assertThat(StreamUtil.asStream(ImmutableList.of("a", "b"))).contains("a", "b");
    }

    @Test
    public void shouldReturnEmptyStreamIfCollectionIsNull() {
        assertThat(StreamUtil.asStream(null)).isEmpty();
    }

    @Test
    public void shouldReturnEmptyStreamIfCollectionIsEmpty() {
        assertThat(StreamUtil.asStream(Collections.emptyList())).isEmpty();
    }
}
