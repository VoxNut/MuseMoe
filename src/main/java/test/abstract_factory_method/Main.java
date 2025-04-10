package test.abstract_factory_method;

public class Main {
    public static void main(String[] args) {
        FurnitureFactory furnitureFactory = new ModernFurniture();

        Chair modernChair = furnitureFactory.createChair();
        modernChair.sit();
    }
}
