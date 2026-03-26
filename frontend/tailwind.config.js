/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        msrp: {
          bg: '#0f172a',
          card: '#1e293b',
          accent: '#38bdf8',
          green: '#22c55e',
          yellow: '#eab308',
          red: '#ef4444',
          muted: '#94a3b8',
          border: '#334155',
        },
      },
    },
  },
  plugins: [],
};
