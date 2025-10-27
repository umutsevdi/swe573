<script lang="ts">
	import { browser } from '$app/environment';

	interface Event {
		id: string;
		title: string;
		latitude: number;
		longitude: number;
		location: string;
	}

	let {
		events,
		height = 'h-96',
		userLat,
		userLng,
		proximity = 50
	}: {
		events: Event[];
		height?: string;
		userLat: number | null;
		userLng: number | null;
		proximity?: number;
	} = $props();

	let mapContainer: HTMLElement;
	let map: any = null;
	let L: any = $state(null);
	let isClient = $state(false);

	// Load Leaflet
	$effect(() => {
		if (browser && !L) {
			import('leaflet').then((leaflet) => {
				L = leaflet;
			});
		}
	});

	// Initialize map on client side
	$effect(() => {
		isClient = browser;
		if (browser && mapContainer && !map && L) {
			const theme = document.documentElement.getAttribute('data-theme') || 'light';

			// Fix for default markers in Leaflet
			delete (L.Icon.Default.prototype as any)._getIconUrl;
			L.Icon.Default.mergeOptions({
				iconRetinaUrl:
					'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
				iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
				shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png'
			});

			// Calculate center
			let centerLat: number, centerLng: number;
			if (events.length > 0) {
				centerLat = events.reduce((sum, e) => sum + e.latitude, 0) / events.length;
				centerLng = events.reduce((sum, e) => sum + e.longitude, 0) / events.length;
			} else if (userLat && userLng) {
				centerLat = userLat;
				centerLng = userLng;
			} else {
				// Default center, e.g., Istanbul or something
				centerLat = 41.0082;
				centerLng = 28.9784;
			}

			// Initialize map
			map = L.map(mapContainer).setView([centerLat, centerLng], 10);

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

			// Add user location marker and proximity circle
			if (userLat && userLng) {
				// Create custom icon for user marker (green)
				const userIcon = new L.Icon({
					iconUrl:
						'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
					shadowUrl:
						'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
					iconSize: [25, 41],
					iconAnchor: [12, 41],
					popupAnchor: [1, -34],
					shadowSize: [41, 41]
				});

				const userMarker = L.marker([userLat, userLng], { icon: userIcon }).addTo(map);
				userMarker.bindPopup("<b>🏠 You're Here</b>");
				markers.push(userMarker);

				// Add circle around user location with proximity radius
				L.circle([userLat, userLng], {
					color: 'green',
					fillColor: 'green',
					fillOpacity: 0.1,
					radius: proximity * 1000
				}).addTo(map);
			}

			// Fit map to all markers
			if (markers.length > 0) {
				const group = new L.featureGroup(markers);
				map.fitBounds(group.getBounds(), { padding: [20, 20] });
			}
		}

		return () => {
			if (map) {
				map.remove();
				map = null;
			}
		};
	});

	// Update map when user location, events, or proximity change
	$effect(() => {
		if (isClient && map && L && events.length > 0) {
			// Clear existing markers, polylines, and circles
			map.eachLayer((layer: any) => {
				if (layer instanceof L.Marker || layer instanceof L.Polyline || layer instanceof L.Circle) {
					map.removeLayer(layer);
				}
			});

			// Re-add markers for each event
			const markers: any[] = [];
			events.forEach((event) => {
				const marker = L.marker([event.latitude, event.longitude]).addTo(map);
				marker.bindPopup(`<b>${event.title}</b><br/>${event.location}`);
				markers.push(marker);
			});

			// Re-add user location marker and proximity circle
			if (userLat && userLng) {
				// Create custom icon for user marker (green)
				const userIcon = new L.Icon({
					iconUrl:
						'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
					shadowUrl:
						'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
					iconSize: [25, 41],
					iconAnchor: [12, 41],
					popupAnchor: [1, -34],
					shadowSize: [41, 41]
				});

				const userMarker = L.marker([userLat, userLng], { icon: userIcon }).addTo(map);
				userMarker.bindPopup("<b>🏠 You're Here</b>");
				markers.push(userMarker);

				// Add circle around user location with proximity radius
				L.circle([userLat, userLng], {
					color: 'green',
					fillColor: 'green',
					fillOpacity: 0.1,
					radius: proximity * 1000
				}).addTo(map);
			}

			// Fit map to all markers
			if (markers.length > 0) {
				const group = new L.featureGroup(markers);
				map.fitBounds(group.getBounds(), { padding: [20, 20] });
			}
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
