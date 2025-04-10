package test.abstract_factory_method;

public class VintageFurniture implements FurnitureFactory {
    @Override
    public Chair createChair() {
        return new VintageChair();
    }

    @Override
    public Table createTable() {
        return new VintageTable();
    }
}
