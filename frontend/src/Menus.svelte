<script lang="ts">
  import type { IMenu, MenusByWeek } from "./interfaces/interfaces";
  import dayjs from "dayjs";
  import Dish from "./Dish.svelte";
  import { flip } from "svelte/animate";
  import { sineOut } from "svelte/easing";

  export let menus: MenusByWeek;
  export let dayOfWeek: number;
  export let week: number;

  let menuList: IMenu[] = [];
  $: menuList = menus[week] ? Object.values(menus[week])[dayOfWeek - 1] || [] : [];

</script>

{#if menuList.length > 0}
  {#each menuList as menu, i (menu.provider)}
    <div class="perf" animate:flip={{duration: 350, easing: sineOut }}>
      <div class="relative flex items-center mt-1">
        <h2 class="flex-1 font-bold pr-4 whitespace-nowrap">{menu.provider}</h2>
        <div class="hairline bg-white opacity-25" style="height: 1px" />
        {#if i === 0}
          <span class="text-white pl-4" style="font-size: 0.75rem">
            {dayjs(menu.date).format("DD.MM.YYYY")}
          </span>
        {/if}
      </div>
      
      {#each menu.dishes as dish (dish.name)}
        <div class="flex-1 py-1 px-3">
          <Dish dish={dish}/>
        </div>
      {/each}
    </div>
  {/each}
{:else}
  <p class="p-4 md:p-0">noch keine Daten verfügbar ...</p>
{/if}

<style>
  .hairline {
    height: 1px;
    z-index: -1;
    width: 100%;
  }
</style>
