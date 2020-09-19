<script lang="ts">
  import type { IDish } from "./interfaces/interfaces";

  export let dish: IDish;

  const formatter = new Intl.NumberFormat("de-DE", { style: "currency", currency: "EUR" });
  const price = formatter.format(dish.price / 100);
</script>

<div class="tracking-tight fade-in">{dish.name} ({price})</div>

<style>
</style>
