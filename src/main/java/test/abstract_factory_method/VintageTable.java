package test.abstract_factory_method;

public class VintageTable implements Table {
    @Override
    public void placeItem() {
        System.out.println("Placing item on vintage table!");
    }
}
