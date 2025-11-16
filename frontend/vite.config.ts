import tailwindcss from '@tailwindcss/vite';
import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vite';

const hosts = (process.env.ALLOWED_ORIGINS || 'localhost:3000').split(',');
hosts.forEach((host) => console.log('AllowedOrigin:', host));
export default defineConfig({
	plugins: [tailwindcss(), sveltekit()],
	preview: {
		allowedHosts: hosts
	}
});
