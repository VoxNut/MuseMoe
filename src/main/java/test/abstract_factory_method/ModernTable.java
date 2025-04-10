package test.abstract_factory_method;

public class ModernTable implements Table {
    @Override
    public void placeItem() {
        System.out.println("Place item on modern table");
    }
}
