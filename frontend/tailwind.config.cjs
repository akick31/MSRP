/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        msrp: {
          bg: 'var(--msrp-bg)',
          card: 'var(--msrp-card)',
          accent: 'var(--msrp-accent)',
          green: 'var(--msrp-green)',
          yellow: 'var(--msrp-yellow)',
          red: 'var(--msrp-red)',
          muted: 'var(--msrp-muted)',
          border: 'var(--msrp-border)',
          text: 'var(--msrp-text)',
        },
      },
    },
  },
  plugins: [],
};
