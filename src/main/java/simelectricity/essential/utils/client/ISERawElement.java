package simelectricity.essential.utils.client;

public interface ISERawElement<T extends ISERawElement> extends ISERawModel<T> {
    @Override
    T clone();
}
