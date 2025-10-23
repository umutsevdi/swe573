<script lang="ts">
	import { onMount, onDestroy } from 'svelte';
	import 'leaflet/dist/leaflet.css';

	interface Event {
		id: string;
		title: string;
		latitude: number;
		longitude: number;
		location: string;
	}

	export let events: Event[];
	export let height: string = 'h-96';
	export let userLat: number | null = null;
	export let userLng: number | null = null;

	let mapContainer: HTMLElement;
	let map: any = null;
	let isClient = false;

	// Initialize map on client side
	onMount(() => {
		if (mapContainer && !map && events.length > 0) {
			import('leaflet').then((L: any) => {
				const theme = document.documentElement.getAttribute('data-theme') || 'light';

				// Fix for default markers in Leaflet
				// eslint-disable-next-line @typescript-eslint/no-explicit-any
				delete (L.Icon.Default.prototype as any)._getIconUrl;
				L.Icon.Default.mergeOptions({
					iconRetinaUrl:
						'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
					iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
					shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png'
				});

				// Calculate center
				const avgLat = events.reduce((sum, e) => sum + e.latitude, 0) / events.length;
				const avgLng = events.reduce((sum, e) => sum + e.longitude, 0) / events.length;

				// Initialize map
				map = L.map(mapContainer).setView([avgLat, avgLng], 10);

				// Add Carto tiles based on theme
				const tileUrl =
					theme === 'dark'
						? 'https://cartodb-basemaps-{s}.global.ssl.fastly.net/dark_all/{z}/{x}/{y}.png'
						: 'https://cartodb-basemaps-{s}.global.ssl.fastly.net/light_all/{z}/{x}/{y}.png';
				L.tileLayer(tileUrl, {
					attribution:
						'© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, © <a href="https://carto.com/attributions">CartoDB</a>',
					maxZoom: 19
				}).addTo(map);

				// Add markers for each event
				const markers: any[] = [];
				events.forEach((event) => {
					const marker = L.marker([event.latitude, event.longitude]).addTo(map);
					marker.bindPopup(`<b>${event.title}</b><br/>${event.location}`);
					markers.push(marker);
				});

				// Add user location marker
				if (userLat && userLng) {
					const userMarker = L.circleMarker([userLat, userLng], { color: 'blue', radius: 8 }).addTo(
						map
					);
					userMarker.bindPopup('Your Location');
					markers.push(userMarker);
				}

				// Fit map to all markers
				if (markers.length > 0) {
					const group = new L.featureGroup(markers);
					map.fitBounds(group.getBounds(), { padding: [20, 20] });
				}
			});
		}
	});

	onDestroy(() => {
		if (map) {
			map.remove();
			map = null;
		}
	});
</script>

<div
	class="{height} overflow-hidden rounded-lg border border-base-300"
	bind:this={mapContainer}
></div>

<style>
	/* Ensure Leaflet controls are visible on dark themes */
	:global(.leaflet-control-container .leaflet-control) {
		background-color: rgba(255, 255, 255, 0.9);
		color: #333;
	}

	:global(.leaflet-popup-content-wrapper) {
		background-color: rgba(255, 255, 255, 0.95);
		color: #333;
		border-radius: 8px;
	}

	:global(.leaflet-popup-tip) {
		background-color: rgba(255, 255, 255, 0.95);
	}
</style>
