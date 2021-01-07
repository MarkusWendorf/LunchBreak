<script lang="ts">
  import LoadingSpinner from "./LoadingSpinner.svelte";
  import Menus from "./Menus.svelte";
  import { getLunchData, getCurrentDay, getCurrentWeek } from "./api";

  const weekDays = ["Mo", "Di", "Mi", "Do", "Fr"];
  let lunchData = getLunchData();
  let selectedDay: number = getCurrentDay(); 
  let selectedWeek: number = getCurrentWeek();
  let currentWeek: number = getCurrentWeek();
  let scrollContainer; 

  const setDay = (day: string) => {
    selectedDay = weekDays.findIndex((v) => v === day) + 1;
    scrollContainer?.scrollTo(0, 0);
  }
  
  const nextOrPrevWeek = () => {
    selectedWeek =  selectedWeek > currentWeek ? currentWeek : currentWeek + 1;
    selectedDay = selectedWeek > currentWeek ? 1 : getCurrentDay();
  };
  
  // auto refresh every half hour
  setInterval(() => {
    selectedDay = getCurrentDay();
    selectedWeek = getCurrentWeek();
    lunchData = getLunchData();
  }, 60000 * 30);
</script>

<main class="flex flex-col md:flex-row md:flex-row-reverse">
  
  {#if selectedDay < 6}
    <div class="flex-1 px-4 pt-2 md:pt-10 overflow-y-scroll" bind:this={scrollContainer}>
      {#await lunchData}
        <LoadingSpinner/>
      {:then menus}
        <Menus dayOfWeek={selectedDay} menus={menus} week={selectedWeek}/>
      {:catch error}
        <p style="color: red">{error.message}</p>
      {/await}
    </div>
  {:else}
    <p>Wochenende</p>
  {/if}

  <div
    class="w-full md:h-0 md:relative md:w-auto bottom-0 flex justify-between px-4 py-2 md:flex-col md:px-6 md:py-6">
    {#each weekDays as day, i}
      <div class="py-2" on:click={() => setDay(day)}>
        <div
          class="w-12 h-12 flex items-center justify-center m-auto py-3 text-center neo font-sans"
          class:active={selectedDay === i + 1}>
          {day}
        </div>
      </div>
    {/each}
    <div class="py-2">
      <div class="w-12 h-12 m-auto py-3 flex justify-center items-center flex-col cursor-pointer" on:click={nextOrPrevWeek}>
        <div>
          {#if selectedWeek > currentWeek }
            <svg class="w-4 h-6" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32" stroke="currentcolor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2">
              <path d="M10 6 L2 16 10 26 M2 16 L30 16" />
            </svg>
          {:else}
            <svg class="w-4 h-6" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32" stroke="currentcolor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2">
              <path d="M22 6 L30 16 22 26 M30 16 L2 16" />
            </svg>
          {/if}
        </div>
        <div class="text-xs">KW{ selectedWeek > currentWeek ? currentWeek : currentWeek + 1 }</div>
      </div>
    </div>
  </div>
</main>

<style global>
  @import "tailwindcss/base";
  @import "tailwindcss/components";
  @import "tailwindcss/utilities";

  @font-face {
    font-family: "Expletus Sans";
    font-style: normal;
    font-weight: 400;
    font-display: block;
    src: url("/fonts/expletus-sans-v14-latin-regular.eot"); /* IE9 Compat Modes */
    src: local("Expletus Sans"), local("ExpletusSans"),
        url("/fonts/expletus-sans-v14-latin-regular.eot?#iefix") format("embedded-opentype"), /* IE6-IE8 */
        url("/fonts/ExpletusSansReduced.woff2") format("woff2"), /* Super Modern Browsers */
        url("/fonts/expletus-sans-v14-latin-regular.woff") format("woff"), /* Modern Browsers */
        url("/fonts/expletus-sans-v14-latin-regular.ttf") format("truetype"), /* Safari, Android, iOS */
        url("/fonts/expletus-sans-v14-latin-regular.svg#ExpletusSans") format("svg"); /* Legacy iOS */
  }

  body {
    background: #253250;
    background: linear-gradient(to right, rgba(37,50,80,1) 0%, rgba(43,58,92,1) 100%);
    color: white;
    padding: 0;
    font-size: 17px;
    font-family: "Expletus Sans", sans-serif;
  }

  html, body, main { height: 100% }

  h1,
  h2,
  h3 {
    color: chartreuse;
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

  .fade-in {
    will-change: opacity;
    backface-visibility: hidden;
    transform: translate3d(0,0,0);
    transform: translateZ(0);
    animation: text-focus-in 350ms cubic-bezier(0.550, 0.085, 0.680, 0.530) both;
    animation-delay: 300ms;
  }

  @keyframes text-focus-in {
    0% {
      opacity: 0;
    }
    100% {
      opacity: 1;
    }
  }
</style>