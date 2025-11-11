<script lang="ts">
	import { browser } from '$app/environment';
	import { MapPin } from '@lucide/svelte';
	import { calculateDistance } from '$lib/utils/geo';

	interface Props {
		latitude: number;
		longitude: number;
		locationName: string;
		userLatitude?: number;
		userLongitude?: number;
		userLocationName?: string;
		height?: string;
	}

	let {
		latitude,
		longitude,
		locationName,
		userLatitude,
		userLongitude,
		userLocationName,
		height = 'h-64'
	}: Props = $props();

	let mapContainer: HTMLElement;
	let map: any = null;
	let isClient = $state(false);

	// Initialize map on client side
	$effect(() => {
		isClient = browser;
		if (browser && mapContainer && !map && latitude != null && longitude != null) {
			import('leaflet').then((L) => {
				// Fix for default markers in Leaflet
				delete (L.Icon.Default.prototype as any)._getIconUrl;
				L.Icon.Default.mergeOptions({
					iconRetinaUrl:
						'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
					iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
					shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png'
				});

				// Initialize map
				map = L.map(mapContainer).setView([latitude, longitude], 13);

				// Add Carto tiles based on theme
				const theme = document.documentElement.getAttribute('data-theme') || 'light';
				const tileUrl =
					theme === 'dark'
						? 'https://cartodb-basemaps-{s}.global.ssl.fastly.net/dark_all/{z}/{x}/{y}.png'
						: 'https://cartodb-basemaps-{s}.global.ssl.fastly.net/light_all/{z}/{x}/{y}.png';
				L.tileLayer(tileUrl, {
					attribution:
						'© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, © <a href="https://carto.com/attributions">CartoDB</a>',
					maxZoom: 19
				}).addTo(map);

				// Add event marker
				const eventMarker = L.marker([latitude, longitude]).addTo(map);
				eventMarker
					.bindPopup(
						`<b>Destination: ${locationName}</b><br/>Lat: ${latitude.toFixed(4)}, Lng: ${longitude.toFixed(4)}`
					)
					.openPopup();

				// eslint-disable-next-line @typescript-eslint/no-explicit-any
				let userMarker: any = null;

				// Add user marker if user location provided
				if (userLatitude && userLongitude) {
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

					userMarker = L.marker([userLatitude, userLongitude], { icon: userIcon }).addTo(map);
					const distance = calculateDistance(userLatitude, userLongitude, latitude, longitude);
					userMarker.bindPopup(
						`<b>🏠 You're Here${userLocationName ? ` (${userLocationName})` : ''}</b><br/>Lat: ${userLatitude.toFixed(4)}, Lng: ${userLongitude.toFixed(4)}<br/>Distance: ${distance.toFixed(1)} km`
					);

					// Add distance line
					L.polyline(
						[
							[userLatitude, userLongitude],
							[latitude, longitude]
						],
						{
							color: 'green',
							weight: 2,
							opacity: 0.7
						}
					).addTo(map);

					// Add circle around user location (500m radius)
					L.circle([userLatitude, userLongitude], {
						color: 'green',
						fillColor: 'green',
						fillOpacity: 0.1,
						radius: 500
					}).addTo(map);
				}

				// Fit map to markers
				const bounds =
					userLatitude && userLongitude
						? [
								[userLatitude, userLongitude],
								[latitude, longitude]
							]
						: [[latitude, longitude]];
				map.fitBounds(bounds, { padding: [20, 20] });
			});
		}

		return () => {
			if (map) {
				map.remove();
				map = null;
			}
		};
	});

	// Update map when coordinates change
	$effect(() => {
		if (isClient && map && latitude != null && longitude != null) {
			map.setView([latitude, longitude], 13);

			// Clear existing markers, polylines, and circles
			map.eachLayer((layer: any) => {
				if (
					layer instanceof (window as any).L.Marker ||
					layer instanceof (window as any).L.Polyline ||
					layer instanceof (window as any).L.Circle
				) {
					map.removeLayer(layer);
				}
			});

			// Add event marker
			const eventMarker = (window as any).L.marker([latitude, longitude]).addTo(map);
			eventMarker
				.bindPopup(
					`<b>📍 Destination: ${locationName}</b><br/>Lat: ${latitude.toFixed(4)}, Lng: ${longitude.toFixed(4)}`
				)
				.openPopup();

			let userMarker: any = null;

			// Add user marker if user location provided
			if (userLatitude && userLongitude) {
				// Create custom icon for user marker (green)
				const userIcon = new (window as any).L.Icon({
					iconUrl:
						'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
					shadowUrl:
						'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
					iconSize: [25, 41],
					iconAnchor: [12, 41],
					popupAnchor: [1, -34],
					shadowSize: [41, 41]
				});

				userMarker = (window as any).L.marker([userLatitude, userLongitude], {
					icon: userIcon
				}).addTo(map);
				const distance = calculateDistance(userLatitude, userLongitude, latitude, longitude);
				userMarker.bindPopup(
					`<b>🏠 You're Here${userLocationName ? ` (${userLocationName})` : ''}</b><br/>Lat: ${userLatitude.toFixed(4)}, Lng: ${userLongitude.toFixed(4)}<br/>Distance: ${distance.toFixed(1)} km`
				);

				// Add distance line
				(window as any).L.polyline(
					[
						[userLatitude, userLongitude],
						[latitude, longitude]
					],
					{
						color: 'green',
						weight: 2,
						opacity: 0.7
					}
				).addTo(map);

				// Add circle around user location (500m radius)
				(window as any).L.circle([userLatitude, userLongitude], {
					color: 'green',
					fillColor: 'green',
					fillOpacity: 0.1,
					radius: 500
				}).addTo(map);
			}

			// Fit map to markers
			const bounds =
				userLatitude && userLongitude
					? [
							[userLatitude, userLongitude],
							[latitude, longitude]
						]
					: [[latitude, longitude]];
			map.fitBounds(bounds, { padding: [20, 20] });
		}
	});
</script>

{#if latitude != null && longitude != null}
	<div
		class="{height} overflow-hidden rounded-lg border border-base-300"
		bind:this={mapContainer}
	></div>
{:else}
	<div
		class="{height} flex items-center justify-center rounded-lg border border-base-300 bg-base-200"
	>
		<div class="text-center text-base-content/50">
			<MapPin class="mx-auto mb-2 h-8 w-8" />
			<p>Location not available</p>
		</div>
	</div>
{/if}

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
