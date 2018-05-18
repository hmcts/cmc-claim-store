package uk.gov.hmcts.cmc.domain.statementofmeans;

public class Dependant {

    private final Children children;
    private final Integer maintainedChildren;

    public Dependant(Children children, Integer maintainedChildren) {
        this.children = children;
        this.maintainedChildren = maintainedChildren;
    }
}
