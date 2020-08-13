<script lang="ts">
    import type { Dish } from "./model/Dish";

    export let dish: Dish;

    const formatter = new Intl.NumberFormat("de-DE", { style: "currency", currency: "EUR" });
    const price = formatter.format(dish.price / 100);
</script>

<div class="py-1 px-3 tracking-tight">{dish.name} ({price})</div>

<style>

</style>
