package uk.gov.hmcts.cmc.ccd.mapper;

public interface Mapper<K, V, T> {

    K to(V v, T builder);

    V from(K k , T builder);
}
