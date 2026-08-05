import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';

export default defineConfig({
  plugins: [react(), tailwindcss()],
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) return;

          if (id.includes('recharts') || id.includes('victory-vendor') || id.includes('d3-')) {
            return 'charts';
          }
          if (id.includes('@stripe')) {
            return 'stripe';
          }
          if (id.includes('react-router')) {
            return 'router';
          }
          if (id.includes('react-dom') || id.includes('/react/') || id.includes('\\react\\')) {
            return 'react-vendor';
          }
        },
      },
    },
  },
});
