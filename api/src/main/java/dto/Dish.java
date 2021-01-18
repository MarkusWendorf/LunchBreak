package dto;

public class Dish {
    private String name;
    private int price;

    public Dish(String name, int price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return name + " (" + String.format("%.2f", price / 100.0) + "€)";
    }
}