package dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Menu {
    private String provider;
    private LocalDate date;
    private List<Dish> dishes;

    public Menu(String provider, LocalDate date) {
        this.provider = provider;
        this.date = date;
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

    public LocalDate getDate() {
        return date;
    }

    @Override
    public String toString() {
        return this.dishes.toString();
    }
}