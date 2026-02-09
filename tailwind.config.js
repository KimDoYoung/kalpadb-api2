/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/main/resources/templates/**/*.html",
    "./src/main/resources/static/js/**/*.js"
  ],
  theme: {
    extend: {
      // Removed manual color overrides to let DaisyUI handle themes
    },
  },
  plugins: [require("daisyui")],
  daisyui: {
    themes: [
      {
        "kalpa-light": {
          "primary": "oklch(55% 0.3 240)",
          "primary-content": "oklch(100% 0 0)",
          "secondary": "oklch(60% 0.15 142)",
          "secondary-content": "oklch(100% 0 0)",
          "accent": "oklch(70% 0.2 60)",
          "accent-content": "oklch(20% 0 0)",
          "neutral": "oklch(50% 0.1 240)",
          "neutral-content": "oklch(98% 0 0)",
          "base-100": "oklch(98% 0.02 240)",
          "base-200": "oklch(93% 0.02 240)",
          "base-300": "oklch(88% 0.02 240)",
          "base-content": "oklch(20% 0.02 240)",
          "info": "oklch(60% 0.2 240)",
          "success": "oklch(65% 0.2 142)",
          "warning": "oklch(65% 0.25 60)",
          "error": "oklch(60% 0.25 10)",
        },
      },
      {
        "kalpa-dark": {
          "primary": "oklch(70% 0.25 240)",
          "primary-content": "oklch(20% 0 0)",
          "secondary": "oklch(75% 0.15 142)",
          "secondary-content": "oklch(20% 0 0)",
          "accent": "oklch(80% 0.2 60)",
          "accent-content": "oklch(20% 0 0)",
          "neutral": "oklch(30% 0.1 240)",
          "neutral-content": "oklch(98% 0 0)",
          "base-100": "oklch(20% 0.02 240)",
          "base-200": "oklch(25% 0.02 240)",
          "base-300": "oklch(30% 0.02 240)",
          "base-content": "oklch(95% 0.02 240)",
          "info": "oklch(70% 0.2 240)",
          "success": "oklch(75% 0.2 142)",
          "warning": "oklch(75% 0.25 60)",
          "error": "oklch(70% 0.25 10)",
        }
      },
      "light",
      "dark",
      "corporate",
      "dracula",
    ],
    darkMode: "class",
    logs: false,
  },
}
