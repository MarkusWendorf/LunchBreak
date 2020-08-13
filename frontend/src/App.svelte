<script lang="ts">
  import getDay from "date-fns/getDay";
  import type { Menu } from "./model/Menu";
  import Dish from "./Dish.svelte";
  import LoadingSpinner from "./LoadingSpinner.svelte";
  
  const lunchdata = "https://d1sj41qgcumq4m.cloudfront.net/lunchdata.json";
  const response: Promise<Menu[][]> = fetch(lunchdata).then((res) =>res.json());

  const week = ["Mo", "Di", "Mi", "Do", "Fr"];
  let dayOfWeek: number = getDay(new Date()) - 1; // 0 = monday

  const setDay = (day: string) => {
    dayOfWeek = week.findIndex((v) => v === day);
  };
</script>

<style global>
  @import "tailwindcss/base";
  @import "tailwindcss/components";
  @import "tailwindcss/utilities";

  @font-face {
    font-family: "Expletus Sans";
    font-style: normal;
    font-weight: 400;
    font-display: fallback;
    src: url("/fonts/expletus-sans-v14-latin-regular.eot"); /* IE9 Compat Modes */
    src: local("Expletus Sans"), local("ExpletusSans"),
        url("/fonts/expletus-sans-v14-latin-regular.eot?#iefix") format("embedded-opentype"), /* IE6-IE8 */
        url("/fonts/expletus-sans-v14-latin-regular.woff2") format("woff2"), /* Super Modern Browsers */
        url("/fonts/expletus-sans-v14-latin-regular.woff") format("woff"), /* Modern Browsers */
        url("/fonts/expletus-sans-v14-latin-regular.ttf") format("truetype"), /* Safari, Android, iOS */
        url("/fonts/expletus-sans-v14-latin-regular.svg#ExpletusSans") format("svg"); /* Legacy iOS */
  }

  body {
    background: #253250;
    color: white;
    padding: 0;
    font-family: "Expletus Sans", cursive;
  }

  h1,
  h2,
  h3 {
    color: chartreuse;
    background: #253250;
	  font-variant-caps: all-small-caps;
  }

  .active {
    border: 1px solid rgba(255, 255, 255, 0.75);
  }

  .neo {
    cursor: pointer;
    -webkit-touch-callout: none;
    outline: none;
    border-radius: 50%;
    background: #253250;
    box-shadow: inset 5px 5px 10px #1a2439, inset -5px -5px 10px #304167;
  }

  .hairline {
    height: 1px;
    position: absolute;
    top: 19px;
    z-index: -1;
    width: 100%;
  }
  
</style>

<main class="flex flex-col md:flex-row">

  <div
    class="fixed w-full md:relative md:w-auto bottom-0 flex justify-between px-4 py-4 md:flex-col md:px-6 md:py-6">
    {#each week as day, i}
      <div class="flex-1 py-2" on:click={() => setDay(day)}>
        <div
          class="w-12 h-12 flex items-center justify-center m-auto py-3 text-center neo font-sans"
          class:active={dayOfWeek === i}>
          {day}
        </div>
      </div>
    {/each}
  </div>

  <div class="flex-1 px-4 pt-2 md:pt-10">
    {#await response}
      <LoadingSpinner/>
    {:then menus}
      {#if menus.length !== 0}
        {#each menus[dayOfWeek] as menu}
          <div class="relative flex">
            <h2 class="tracking-tight font-bold pt-2 pb-1 pr-2">{menu.provider}</h2>
            <div class="hairline bg-white opacity-25" style="height: 1px" />
          </div>
          <div class="flex-1">
            {#each menu.dishes as dish}
              <Dish {dish} />
            {/each}
          </div>
        {/each}
      {/if}
    {:catch error}
      <p style="color: red">{error.message}</p>
    {/await}
  </div>

</main>
