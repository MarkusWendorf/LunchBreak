const production = !process.env.ROLLUP_WATCH;

const purgecss = require("@fullhuman/postcss-purgecss")({
  content: ["./src/**/*.svelte"],
  css: ["./public/global.css"],
  defaultExtractor: (content) => content.match(/[A-Za-z0-9-_:/]+/g) || [],
});

module.exports = {
  plugins: [
    require("tailwindcss"),
    ...(production ? [purgecss] : [])
  ],
};
