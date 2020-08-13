package dto;

import java.util.ArrayList;
import java.util.List;

public class Menu {
    private String provider;
    private List<Dish> dishes;

    public Menu(String provider) {
        this.provider = provider;
        this.dishes = new ArrayList<>();
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public List<Dish> getDishes() {
        return dishes;
    }

    public void setDishes(List<Dish> dishes) {
        this.dishes = dishes;
    }

    public void addDish(Dish dish) {
        this.dishes.add(dish);
    }

    @Override
    public String toString() {
        return this.dishes.toString();
    }
}