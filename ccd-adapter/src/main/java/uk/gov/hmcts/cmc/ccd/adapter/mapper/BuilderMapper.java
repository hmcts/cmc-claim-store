package uk.gov.hmcts.cmc.ccd.adapter.mapper;

public interface BuilderMapper<K, V, B> {

    void to(V v, B builder);

    V from(K k);
}
